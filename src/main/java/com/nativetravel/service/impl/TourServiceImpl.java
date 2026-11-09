package com.nativetravel.service.impl;

import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.TourFilterRequest;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.entity.ViewedTour;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.repository.ViewedTourRepository;
import com.nativetravel.service.TourService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления каталогом туров.
 */
@Service
@RequiredArgsConstructor
public class TourServiceImpl implements TourService {

    private static final Logger log = LoggerFactory.getLogger(TourServiceImpl.class);

    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final ViewedTourRepository viewedTourRepository;
    private final TourMapper tourMapper;

    /**
     * Поиск туров по гибким фильтрам с пагинацией и сортировкой.
     *
     * @param filter критерии фильтрации
     * @return страница найденных туров
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TourDto> getTours(TourFilterRequest filter) {
        log.debug("Fetching tours with filter: {}", filter);
        if (filter == null) {
            filter = new TourFilterRequest(null, null, null, null, null, null, null, null, null, 0, 20);
        }

        Sort.Direction direction = filter.isAscending() ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, filter.getSortField());
        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(), sort);

        Specification<Tour> spec = buildSpecification(filter);
        return tourRepository.findAll(spec, pageable).map(tourMapper::toDto);
    }

    /**
     * Получение DTO тура по идентификатору.
     *
     * @param id идентификатор тура
     * @return DTO тура
     */
    @Override
    @Transactional(readOnly = true)
    public TourDto getTourById(Long id) {
        Tour tour = getTourEntity(id);
        return tourMapper.toDto(tour);
    }

    /**
     * Получение сущности тура для внутренних сервисов.
     *
     * @param id идентификатор тура
     * @return JPA-сущность тура
     */
    @Override
    @Transactional(readOnly = true)
    public Tour getTourEntity(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Тур с id " + id + " не найден"));
    }

    /**
     * Получение списка горящих предложений.
     *
     * @param limit максимальное количество
     * @return список горящих туров
     */
    @Override
    @Transactional(readOnly = true)
    public List<TourDto> getHotOffers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "price"));
        return tourRepository.findByHotOfferTrue(pageable).map(tourMapper::toDto).getContent();
    }

    /**
     * Получение списка всех доступных стран для селектора поиска.
     *
     * @return список названий стран
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableCountries() {
        return tourRepository.findDistinctCountries();
    }

    /**
     * Фиксация факта просмотра тура авторизованным пользователем.
     *
     * @param tourId ID тура
     * @param userEmail email пользователя (если авторизован)
     */
    @Override
    @Transactional
    public void recordTourView(Long tourId, String userEmail) {
        if (!StringUtils.hasText(userEmail) || "anonymousUser".equalsIgnoreCase(userEmail)) {
            return;
        }

        userRepository.findByEmail(userEmail).ifPresent(user -> {
            tourRepository.findById(tourId).ifPresent(tour -> {
                ViewedTour viewed = viewedTourRepository.findByUserAndTour(user, tour)
                        .orElseGet(() -> ViewedTour.builder()
                                .user(user)
                                .tour(tour)
                                .viewedAt(Instant.now())
                                .build());
                viewed.setViewedAt(Instant.now());
                viewedTourRepository.save(viewed);
            });
        });
    }

    private Specification<Tour> buildSpecification(TourFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.country())) {
                predicates.add(cb.equal(cb.lower(root.get("country")), filter.country().trim().toLowerCase()));
            }

            if (StringUtils.hasText(filter.city())) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + filter.city().trim().toLowerCase() + "%"));
            }

            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }

            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            if (filter.startDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), filter.startDate()));
            }

            if (filter.minStars() != null && filter.minStars() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.join("hotel").get("stars"), filter.minStars()));
            }

            if (Boolean.TRUE.equals(filter.hotOfferOnly())) {
                predicates.add(cb.isTrue(root.get("hotOffer")));
            }

            // Только туры с доступными слотами
            predicates.add(cb.greaterThan(root.get("availableSlots"), 0));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

