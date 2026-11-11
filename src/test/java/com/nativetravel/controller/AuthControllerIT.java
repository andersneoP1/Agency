package com.nativetravel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.dto.LoginRequest;
import com.nativetravel.dto.PasswordRecoveryRequest;
import com.nativetravel.dto.PasswordResetConfirmRequest;
import com.nativetravel.dto.RegisterRequest;
import com.nativetravel.entity.PasswordResetCode;
import com.nativetravel.entity.User;
import com.nativetravel.repository.PasswordResetCodeRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.util.CookieUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Autowired
    private com.nativetravel.util.RateLimitService rateLimitService;

    @BeforeEach
    void cleanUp() {
        rateLimitService.reset();
        passwordResetCodeRepository.deleteAll();
        userRepository.findAll().stream()
                .filter(u -> u.getEmail().endsWith("@example.com"))
                .forEach(userRepository::delete);
    }

    @Test
    @DisplayName("IT: Регистрация нового пользователя возвращает 201 и устанавливает HttpOnly cookies")
    void register_success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Дмитрий", "Кузнецов", "dmitry@example.com", "Password123!", "+79998887766", null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("dmitry@example.com"))
                .andExpect(jsonPath("$.user.referralCode", notNullValue()))
                .andExpect(header().exists("Set-Cookie"));

        assertTrue(userRepository.existsByEmail("dmitry@example.com"));
    }

    @Test
    @DisplayName("IT: Регистрация с некорректным паролем (<8 символов) возвращает 400 ProblemDetail")
    void register_invalidPassword_returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Дмитрий", "Кузнецов", "dmitry2@example.com", "short", "+79998887766", null
        );

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Ошибка валидации"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @Test
    @DisplayName("IT: Вход с корректными данными возвращает 200, токен и cookies")
    void login_success() throws Exception {
        // Сначала регистрируем
        RegisterRequest registerReq = new RegisterRequest(
                "Елена", "Попова", "elena@example.com", "SecurePass123!", null, null
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Выполняем вход
        LoginRequest loginReq = new LoginRequest("elena@example.com", "SecurePass123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.email").value("elena@example.com"))
                .andExpect(cookie().exists(CookieUtils.ACCESS_TOKEN_COOKIE))
                .andExpect(cookie().exists(CookieUtils.REFRESH_TOKEN_COOKIE));
    }

    @Test
    @DisplayName("IT: Вход с неверным паролем возвращает 401 ProblemDetail")
    void login_wrongPassword_returns401() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "Елена", "Попова", "elena2@example.com", "SecurePass123!", null, null
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        LoginRequest loginReq = new LoginRequest("elena2@example.com", "WrongPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Неверные учетные данные"));
    }

    @Test
    @DisplayName("IT: Полный сценарий сброса пароля: запрос кода -> подтверждение -> вход с новым паролем")
    void passwordRecovery_fullFlow() throws Exception {
        RegisterRequest registerReq = new RegisterRequest(
                "Сергей", "Волков", "sergey@example.com", "OldPassword123!", null, null
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // 1. Запрос восстановления
        PasswordRecoveryRequest recoveryReq = new PasswordRecoveryRequest("sergey@example.com");
        mockMvc.perform(post("/api/v1/auth/recovery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoveryReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", notNullValue()));

        User user = userRepository.findByEmail("sergey@example.com").orElseThrow();
        PasswordResetCode code = passwordResetCodeRepository.findFirstByUserOrderByCreatedAtDesc(user).orElseThrow();
        assertNotNull(code.getCode());

        // 2. Подтверждение с новым паролем
        PasswordResetConfirmRequest confirmReq = new PasswordResetConfirmRequest(
                "sergey@example.com", code.getCode(), "NewBrandPassword123!"
        );
        mockMvc.perform(post("/api/v1/auth/recovery/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk());

        // 3. Вход со старым паролем падает
        LoginRequest oldLogin = new LoginRequest("sergey@example.com", "OldPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLogin)))
                .andExpect(status().isUnauthorized());

        // 4. Вход с новым паролем успешен
        LoginRequest newLogin = new LoginRequest("sergey@example.com", "NewBrandPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLogin)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT: Выход из системы очищает cookie")
    void logout_clearsCookies() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(CookieUtils.ACCESS_TOKEN_COOKIE, 0))
                .andExpect(cookie().maxAge(CookieUtils.REFRESH_TOKEN_COOKIE, 0));
    }

    @Test
    @DisplayName("IT: Web-выход через POST /logout очищает cookie и перенаправляет на главную")
    void webLogout_post_clearsCookiesAndRedirects() throws Exception {
        mockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/?logout=true")))
                .andExpect(cookie().maxAge(CookieUtils.ACCESS_TOKEN_COOKIE, 0))
                .andExpect(cookie().maxAge(CookieUtils.REFRESH_TOKEN_COOKIE, 0));
    }

    @Test
    @DisplayName("IT: Web-выход через GET /logout очищает cookie и перенаправляет на главную")
    void webLogout_get_clearsCookiesAndRedirects() throws Exception {
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/?logout=true")))
                .andExpect(cookie().maxAge(CookieUtils.ACCESS_TOKEN_COOKIE, 0))
                .andExpect(cookie().maxAge(CookieUtils.REFRESH_TOKEN_COOKIE, 0));
    }

    @Test
    @DisplayName("IT: Запрос /api/v1/auth/me без токена возвращает 401 или 403")
    void getMe_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
