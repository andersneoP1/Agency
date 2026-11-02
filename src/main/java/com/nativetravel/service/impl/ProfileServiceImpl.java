package com.nativetravel.service.impl;

import com.nativetravel.dto.ProfileUpdateRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.User;
import com.nativetravel.entity.ViewedTour;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.mapper.UserMapper;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.repository.ViewedTourRepository;
import com.nativetravel.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса управления личным кабинетом пользователя.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ViewedTourRepository viewedTourRepository;
    private final UserMapper userMapper;
    private final TourMapper tourMapper;

    private static final String UPLOAD_DIR = "uploads/avatars";

    @Override
    @Transactional(readOnly = true)
    public UserDto getProfile(String userEmail) {
        User user = getUser(userEmail);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    public UserDto updateProfile(ProfileUpdateRequest request, String userEmail) {
        User user = getUser(userEmail);

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }

        User updated = userRepository.save(user);
        log.info("Profile updated for user: {}", userEmail);
        return userMapper.toDto(updated);
    }

    @Override
    @Transactional
    public String uploadAvatar(MultipartFile file, String userEmail) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Файл изображения не может быть пустым");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp"))) {
            throw new BadRequestException("Поддерживаются только форматы JPEG, PNG и WEBP");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Размер файла не должен превышать 5 МБ");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String ext = contentType.substring(contentType.lastIndexOf('/') + 1);
            if ("jpeg".equalsIgnoreCase(ext)) ext = "jpg";
            String filename = "avatar-" + UUID.randomUUID() + "." + ext;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String avatarUrl = "/" + UPLOAD_DIR + "/" + filename;
            User user = getUser(userEmail);
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            log.info("Avatar uploaded for user {}: {}", userEmail, avatarUrl);
            return avatarUrl;
        } catch (IOException e) {
            log.error("Failed to store avatar file for user: {}", userEmail, e);
            throw new RuntimeException("Не удалось сохранить файл аватара", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TourDto> getViewedTours(String userEmail) {
        User user = getUser(userEmail);
        return viewedTourRepository.findByUserOrderByViewedAtDesc(user, PageRequest.of(0, 6)).stream()
                .map(ViewedTour::getTour)
                .map(tourMapper::toDto)
                .toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь " + email + " не найден"));
    }
}

