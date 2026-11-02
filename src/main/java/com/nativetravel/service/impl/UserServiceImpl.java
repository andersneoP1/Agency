package com.nativetravel.service.impl;

import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.User;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.mapper.UserMapper;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Реализация сервиса управления пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String REFERRAL_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int REFERRAL_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Поиск пользователя по email.
     *
     * @param email адрес электронной почты
     * @return найденная сущность пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + email + " не найден"));
    }

    /**
     * Поиск пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденная сущность пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + id + " не найден"));
    }

    /**
     * Получение DTO текущего аутентифицированного пользователя.
     *
     * @return данные текущего пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserDto() {
        User user = getCurrentUserEntity();
        return userMapper.toDto(user);
    }

    /**
     * Получение сущности текущего аутентифицированного пользователя.
     *
     * @return сущность текущего пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Требуется авторизация в системе");
        }
        return findByEmail(authentication.getName());
    }

    /**
     * Генерация уникального реферального кода.
     *
     * @return уникальная строка реферального кода
     */
    @Override
    public String generateUniqueReferralCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder("NT-");
            for (int i = 0; i < REFERRAL_LENGTH; i++) {
                int index = random.nextInt(REFERRAL_CHARS.length());
                sb.append(REFERRAL_CHARS.charAt(index));
            }
            code = sb.toString();
        } while (userRepository.existsByReferralCode(code));
        return code;
    }
}
