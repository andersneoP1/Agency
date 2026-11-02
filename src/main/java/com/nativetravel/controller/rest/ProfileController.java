package com.nativetravel.controller.rest;

import com.nativetravel.dto.MessageResponse;
import com.nativetravel.dto.ProfileUpdateRequest;
import com.nativetravel.dto.ReferralDto;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.ProfileService;
import com.nativetravel.service.ReferralService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

/**
 * REST контроллер личного кабинета пользователя.
 */
@Tag(name = "Profile - Личный кабинет", description = "Управление профилем пользователя, загрузка аватара, история просмотров и реферальная программа")
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ReferralService referralService;

    @Operation(summary = "Получить данные профиля", description = "Возвращает информацию о текущем авторизованном пользователе")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные профиля получены", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<UserDto> getProfile(Principal principal) {
        validateAuth(principal);
        return ResponseEntity.ok(profileService.getProfile(principal.getName()));
    }

    @Operation(summary = "Обновить личные данные", description = "Обновляет имя, фамилию и телефон пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации данных", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping
    public ResponseEntity<UserDto> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Principal principal
    ) {
        validateAuth(principal);
        return ResponseEntity.ok(profileService.updateProfile(request, principal.getName()));
    }

    @Operation(summary = "Загрузить аватар профиля", description = "Загружает файл изображения (JPEG, PNG, WEBP до 5 МБ)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Аватар успешно загружен", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неподдерживаемый формат или слишком большой размер", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) {
        validateAuth(principal);
        String avatarUrl = profileService.uploadAvatar(file, principal.getName());
        return ResponseEntity.ok(MessageResponse.of(avatarUrl));
    }

    @Operation(summary = "Получить историю просмотренных туров", description = "Возвращает последние просмотренные туры пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список просмотренных туров"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/viewed-tours")
    public ResponseEntity<List<TourDto>> getViewedTours(Principal principal) {
        validateAuth(principal);
        return ResponseEntity.ok(profileService.getViewedTours(principal.getName()));
    }

    @Operation(summary = "Получить реферальную ссылку и статистику", description = "Возвращает персональный код, ссылку для приглашения друзей и список приглашенных")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Реферальная информация получена", content = @Content(schema = @Schema(implementation = ReferralDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/referral")
    public ResponseEntity<ReferralDto> getReferralInfo(Principal principal) {
        validateAuth(principal);
        return ResponseEntity.ok(referralService.getReferralInfo(principal.getName()));
    }

    private void validateAuth(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Требуется авторизация в системе");
        }
    }
}
