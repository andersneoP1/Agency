package com.nativetravel.controller.rest;

import com.nativetravel.dto.AuthResponse;
import com.nativetravel.dto.LoginRequest;
import com.nativetravel.dto.MessageResponse;
import com.nativetravel.dto.PasswordRecoveryRequest;
import com.nativetravel.dto.PasswordResetConfirmRequest;
import com.nativetravel.dto.RegisterRequest;
import com.nativetravel.dto.TokenRefreshRequest;
import com.nativetravel.dto.UserDto;
import com.nativetravel.service.AuthService;
import com.nativetravel.service.UserService;
import com.nativetravel.util.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для операций аутентификации, регистрации и восстановления доступа.
 */
@Tag(name = "Auth - Аутентификация и пользователи", description = "Эндпоинты регистрации, входа, обновления токенов и сброса пароля")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(summary = "Регистрация нового пользователя", description = "Создает новый аккаунт пользователя, генерирует токены и устанавливает HttpOnly cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Успешная регистрация", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "Превышен лимит запросов", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request);
        setAuthCookies(response, authResponse.user().email(), authResponse.accessToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя по email и паролю, возвращает JWT и сохраняет cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "Превышен лимит запросов", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request);
        setAuthCookies(response, authResponse.user().email(), authResponse.accessToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Обновление access-токена", description = "Генерирует новый access-токен по refresh-токену из cookie или тела запроса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Недействительный refresh-токен", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) TokenRefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String token = (request != null && request.refreshToken() != null && !request.refreshToken().isBlank())
                ? request.refreshToken()
                : CookieUtils.getCookieValue(httpRequest, CookieUtils.REFRESH_TOKEN_COOKIE).orElse(null);

        AuthResponse authResponse = authService.refreshToken(token);
        ResponseCookie accessCookie = CookieUtils.createAccessCookie(authResponse.accessToken(), authResponse.expiresIn());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Выход из системы", description = "Удаляет авторизационные HttpOnly cookies")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный выход", content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {
        ResponseCookie delAccess = CookieUtils.deleteAccessCookie();
        ResponseCookie delRefresh = CookieUtils.deleteRefreshCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, delAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, delRefresh.toString());
        return ResponseEntity.ok(MessageResponse.of("Успешный выход из системы"));
    }

    @Operation(summary = "Запрос кода восстановления пароля", description = "Отправляет 6-значный одноразовый код на указанный email адрес")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код подтверждения отправлен (если аккаунт существует)", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный email", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "Превышен лимит запросов", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/recovery")
    public ResponseEntity<MessageResponse> requestRecovery(@Valid @RequestBody PasswordRecoveryRequest request) {
        authService.requestPasswordRecovery(request);
        return ResponseEntity.ok(MessageResponse.of("Если пользователь с таким адресом зарегистрирован, код подтверждения отправлен на email"));
    }

    @Operation(summary = "Подтверждение сброса пароля", description = "Проверяет 6-значный код и устанавливает новый пароль")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменен", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный или просроченный код", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/recovery/confirm")
    public ResponseEntity<MessageResponse> confirmRecovery(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(MessageResponse.of("Пароль успешно изменен. Теперь вы можете войти в систему."));
    }

    @Operation(summary = "Данные текущего пользователя", description = "Возвращает профиль текущего аутентифицированного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль пользователя", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserDto());
    }

    private void setAuthCookies(HttpServletResponse response, String email, String accessToken) {
        String refreshToken = authService.generateRefreshToken(email);
        ResponseCookie accessCookie = CookieUtils.createAccessCookie(accessToken, 900);
        ResponseCookie refreshCookie = CookieUtils.createRefreshCookie(refreshToken, 604800);
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
