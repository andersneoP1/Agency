package com.nativetravel.service.impl;

import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.FavoriteTour;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.FavoriteTourRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Реализация сервиса избранного.
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteTourRepository favoriteTourRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    /**
     * Добавление тура в избранное.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя
     */
    @Override
    @Transactional
    public void addFavorite(Long tourId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Тур не найден"));

        if (!favoriteTourRepository.existsByUserAndTour(user, tour)) {
            FavoriteTour fav = FavoriteTour.builder()
                    .user(user)
                    .tour(tour)
                    .build();
            favoriteTourRepository.save(fav);
        }
    }

    /**
     * Удаление тура из избранного.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя
     */
    @Override
    @Transactional
    public void removeFavorite(Long tourId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Тур не найден"));

        favoriteTourRepository.deleteByUserAndTour(user, tour);
    }

    /**
     * Проверка, находится ли тур в избранном.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя
     * @return true, если тур в избранном
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long tourId, String userEmail) {
        if (userEmail == null || "anonymousUser".equalsIgnoreCase(userEmail)) {
            return false;
        }
        return userRepository.findByEmail(userEmail)
                .flatMap(user -> tourRepository.findById(tourId)
                        .map(tour -> favoriteTourRepository.existsByUserAndTour(user, tour)))
                .orElse(false);
    }

    /**
     * Получение списка всех избранных туров пользователя.
     *
     * @param userEmail email пользователя
     * @param pageable параметры пагинации
     * @return страница избранных туров
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TourDto> getUserFavorites(String userEmail, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
        return favoriteTourRepository.findByUser(user, pageable)
                .map(FavoriteTour::getTour)
                .map(tourMapper::toDto);
    }
}

