package com.nativetravel.service;

import com.nativetravel.dto.AuthResponse;
import com.nativetravel.dto.LoginRequest;
import com.nativetravel.dto.PasswordRecoveryRequest;
import com.nativetravel.dto.PasswordResetConfirmRequest;
import com.nativetravel.dto.RegisterRequest;
import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.PasswordResetCode;
import com.nativetravel.entity.Role;
import com.nativetravel.entity.RoleType;
import com.nativetravel.entity.User;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ConflictException;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.mapper.UserMapper;
import com.nativetravel.repository.PasswordResetCodeRepository;
import com.nativetravel.repository.RoleRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.security.JwtTokenProvider;
import com.nativetravel.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private Role userRole;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        userRole = new Role(1L, RoleType.ROLE_USER);
        testUser = User.builder()
                .id(1L)
                .email("alex@example.com")
                .passwordHash("hashedSecret123")
                .firstName("Алексей")
                .lastName("Смирнов")
                .referralCode("NT-TEST1234")
                .roles(Set.of(userRole))
                .enabled(true)
                .build();

        testUserDto = new UserDto(
                1L, "alex@example.com", "Алексей", "Смирнов",
                null, null, "NT-TEST1234", Set.of("ROLE_USER"), true, Instant.now()
        );
    }

    @Test
    @DisplayName("Регистрация: успешное создание нового пользователя и возврат токенов")
    void register_success() {
        RegisterRequest request = new RegisterRequest(
                "Алексей", "Смирнов", "alex@example.com", "Secret123!", null, null
        );

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userService.generateUniqueReferralCode()).thenReturn("NT-TEST1234");
        when(passwordEncoder.encode("Secret123!")).thenReturn("hashedSecret123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateAccessToken(any())).thenReturn("mock-access-token");
        when(tokenProvider.getAccessExpirationMs()).thenReturn(900000L);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals("alex@example.com", response.user().email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация: ошибка при попытке зарегистрировать существующий email")
    void register_duplicateEmail_throwsConflict() {
        RegisterRequest request = new RegisterRequest(
                "Алексей", "Смирнов", "alex@example.com", "Secret123!", null, null
        );

        when(userRepository.existsByEmail("alex@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Вход: успешная аутентификация при корректных учетных данных")
    void login_success() {
        LoginRequest request = new LoginRequest("alex@example.com", "Secret123!");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Secret123!", "hashedSecret123")).thenReturn(true);
        when(tokenProvider.generateAccessToken(any())).thenReturn("mock-access-token");
        when(tokenProvider.getAccessExpirationMs()).thenReturn(900000L);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-access-token", response.accessToken());
        assertEquals("alex@example.com", response.user().email());
    }

    @Test
    @DisplayName("Вход: ошибка BadCredentialsException при неверном пароле")
    void login_wrongPassword_throwsBadCredentials() {
        LoginRequest request = new LoginRequest("alex@example.com", "WrongPassword");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPassword", "hashedSecret123")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Вход: ошибка UnauthorizedException для заблокированного аккаунта")
    void login_disabledUser_throwsUnauthorized() {
        testUser.setEnabled(false);
        LoginRequest request = new LoginRequest("alex@example.com", "Secret123!");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Secret123!", "hashedSecret123")).thenReturn(true);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Обновление токена: успешная генерация нового access-токена")
    void refreshToken_valid_returnsNewAccessToken() {
        String refreshToken = "valid-refresh-token";

        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getEmailFromToken(refreshToken)).thenReturn("alex@example.com");
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateAccessToken(any())).thenReturn("new-access-token");
        when(tokenProvider.getAccessExpirationMs()).thenReturn(900000L);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
    }

    @Test
    @DisplayName("Обновление токена: ошибка при передаче невалидного refresh-токена")
    void refreshToken_invalid_throwsUnauthorized() {
        String invalidToken = "bad-token";

        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(invalidToken));
    }

    @Test
    @DisplayName("Восстановление пароля: генерация кода и отправка письма на email")
    void requestPasswordRecovery_generatesCodeAndSendsEmail() {
        PasswordRecoveryRequest request = new PasswordRecoveryRequest("alex@example.com");
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));

        authService.requestPasswordRecovery(request);

        ArgumentCaptor<PasswordResetCode> captor = ArgumentCaptor.forClass(PasswordResetCode.class);
        verify(passwordResetCodeRepository).save(captor.capture());
        PasswordResetCode savedCode = captor.getValue();

        assertNotNull(savedCode.getCode());
        assertEquals(6, savedCode.getCode().length());
        assertEquals(testUser, savedCode.getUser());
        verify(emailService).sendPasswordResetCode(eq("alex@example.com"), anyString());
    }

    @Test
    @DisplayName("Подтверждение сброса пароля: успешная смена пароля при валидном коде")
    void confirmPasswordReset_validCode_updatesPassword() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "alex@example.com", "123456", "NewSecret123!"
        );

        PasswordResetCode resetCode = PasswordResetCode.builder()
                .user(testUser)
                .code("123456")
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetCodeRepository.findByUserAndCodeAndUsedFalse(testUser, "123456"))
                .thenReturn(Optional.of(resetCode));
        when(passwordEncoder.encode("NewSecret123!")).thenReturn("newHashedPassword");

        authService.confirmPasswordReset(request);

        assertTrue(resetCode.isUsed());
        assertEquals("newHashedPassword", testUser.getPasswordHash());
        verify(userRepository).save(testUser);
        verify(passwordResetCodeRepository).save(resetCode);
    }

    @Test
    @DisplayName("Подтверждение сброса пароля: ошибка BadRequestException при истекшем сроке кода")
    void confirmPasswordReset_expiredCode_throwsBadRequest() {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "alex@example.com", "123456", "NewSecret123!"
        );

        PasswordResetCode expiredCode = PasswordResetCode.builder()
                .user(testUser)
                .code("123456")
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .used(false)
                .build();

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(testUser));
        when(passwordResetCodeRepository.findByUserAndCodeAndUsedFalse(testUser, "123456"))
                .thenReturn(Optional.of(expiredCode));

        assertThrows(BadRequestException.class, () -> authService.confirmPasswordReset(request));
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
