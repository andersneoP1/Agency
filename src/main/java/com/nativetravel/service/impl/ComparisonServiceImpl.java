package com.nativetravel.service.impl;

import com.nativetravel.dto.TourDto;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.service.ComparisonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Реализация сервиса сравнения туристических туров.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ComparisonServiceImpl implements ComparisonService {

    private static final int MAX_COMPARE_LIMIT = 4;

    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TourDto> compareTours(List<Long> tourIds) {
        if (tourIds == null || tourIds.isEmpty()) {
            return Collections.emptyList();
        }

        if (tourIds.size() > MAX_COMPARE_LIMIT) {
            throw new BadRequestException("Для сравнения можно выбрать не более " + MAX_COMPARE_LIMIT + " туров одновременно");
        }

        return tourRepository.findAllById(tourIds).stream()
                .map(tourMapper::toDto)
                .toList();
    }
}

