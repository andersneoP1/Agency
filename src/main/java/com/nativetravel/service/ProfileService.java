package com.nativetravel.service;

import com.nativetravel.dto.ProfileUpdateRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Сервис управления личным кабинетом, обновлением данных и загрузкой аватара.
 */
public interface ProfileService {

    /**
     * Получение актуального профиля пользователя.
     *
     * @param userEmail email пользователя
     * @return DTO пользователя
     */
    UserDto getProfile(String userEmail);

    /**
     * Обновление ФИО и телефона пользователя.
     *
     * @param request обновленные данные
     * @param userEmail email пользователя
     * @return обновленный профиль
     */
    UserDto updateProfile(ProfileUpdateRequest request, String userEmail);

    /**
     * Загрузка и сохранение файла аватара пользователя.
     *
     * @param file загружаемый файл изображения
     * @param userEmail email пользователя
     * @return URL загруженного аватара
     */
    String uploadAvatar(MultipartFile file, String userEmail);

    /**
     * Получение списка недавно просмотренных туров пользователя.
     *
     * @param userEmail email пользователя
     * @return список DTO туров
     */
    List<TourDto> getViewedTours(String userEmail);
}
