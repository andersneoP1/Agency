package com.nativetravel.service.impl;

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
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.mapper.UserMapper;
import com.nativetravel.repository.PasswordResetCodeRepository;
import com.nativetravel.repository.RoleRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.security.CustomUserDetails;
import com.nativetravel.security.JwtTokenProvider;
import com.nativetravel.service.AuthService;
import com.nativetravel.service.EmailService;
import com.nativetravel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Реализация сервиса аутентификации и восстановления доступа.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final EmailService emailService;
    private final UserMapper userMapper;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Регистрация нового пользователя.
     *
     * @param request данные регистрации
     * @return AuthResponse с JWT access-токеном и данными пользователя
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Пользователь с адресом " + request.email() + " уже зарегистрирован");
        }

        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_USER)));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        String referralCode = userService.generateUniqueReferralCode();

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone() != null ? request.phone().trim() : null)
                .referralCode(referralCode)
                .enabled(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        UserDto userDto = userMapper.toDto(savedUser);

        return AuthResponse.of(accessToken, tokenProvider.getAccessExpirationMs() / 1000, userDto);
    }

    /**
     * Аутентификация пользователя по email и паролю.
     *
     * @param request данные для входа
     * @return AuthResponse с JWT access-токеном и данными пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("User login attempt: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Учетная запись заблокирована или отключена");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = tokenProvider.generateAccessToken(userDetails);
        UserDto userDto = userMapper.toDto(user);

        return AuthResponse.of(accessToken, tokenProvider.getAccessExpirationMs() / 1000, userDto);
    }

    /**
     * Обновление access-токена по валидному refresh-токену.
     *
     * @param refreshToken refresh JWT-токен
     * @return AuthResponse с новым access-токеном
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Недействительный или истекший refresh-токен");
        }

        String email = tokenProvider.getEmailFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Пользователь не найден"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Учетная запись заблокирована");
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        UserDto userDto = userMapper.toDto(user);

        return AuthResponse.of(newAccessToken, tokenProvider.getAccessExpirationMs() / 1000, userDto);
    }

    /**
     * Запрос на восстановление пароля (отправка 6-значного кода на email).
     *
     * @param request запрос с email адресом
     */
    @Override
    @Transactional
    public void requestPasswordRecovery(PasswordRecoveryRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("Password recovery requested for: {}", email);

        userRepository.findByEmail(email).ifPresent(user -> {
            String code = String.format("%06d", secureRandom.nextInt(1_000_000));
            Instant expiresAt = Instant.now().plus(Duration.ofMinutes(15));

            PasswordResetCode resetCode = PasswordResetCode.builder()
                    .user(user)
                    .code(code)
                    .expiresAt(expiresAt)
                    .used(false)
                    .build();

            passwordResetCodeRepository.save(resetCode);
            emailService.sendPasswordResetCode(user.getEmail(), code);
        });
    }

    /**
     * Подтверждение смены пароля по коду.
     *
     * @param request запрос с email, 6-значным кодом и новым паролем
     */
    @Override
    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("Confirming password reset for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + email + " не найден"));

        PasswordResetCode resetCode = passwordResetCodeRepository
                .findByUserAndCodeAndUsedFalse(user, request.code())
                .orElseThrow(() -> new BadRequestException("Неверный или уже использованный код подтверждения"));

        if (resetCode.isExpired()) {
            throw new BadRequestException("Срок действия кода подтверждения истек (15 минут)");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetCode.setUsed(true);

        userRepository.save(user);
        passwordResetCodeRepository.save(resetCode);
        log.info("Password successfully reset for: {}", email);
    }

    /**
     * Генерация refresh-токена для пользователя.
     *
     * @param email адрес электронной почты
     * @return строка JWT refresh-токена
     */
    @Override
    public String generateRefreshToken(String email) {
        return tokenProvider.generateRefreshToken(email);
    }
}
