package com.nativetravel.service.impl;

import com.nativetravel.dto.FlightDto;
import com.nativetravel.dto.FlightFilterRequest;
import com.nativetravel.entity.Flight;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.FlightMapper;
import com.nativetravel.repository.FlightRepository;
import com.nativetravel.service.FlightService;
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

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления авиарейсами.
 */
@Service
@RequiredArgsConstructor
public class FlightServiceImpl implements FlightService {

    private static final Logger log = LoggerFactory.getLogger(FlightServiceImpl.class);

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;

    /**
     * Поиск авиарейсов по параметрам с пагинацией.
     *
     * @param filter критерии фильтрации
     * @return страница рейсов
     */
    @Override
    @Transactional(readOnly = true)
    public Page<FlightDto> getFlights(FlightFilterRequest filter) {
        log.debug("Fetching flights with filter: {}", filter);

        Pageable pageable = PageRequest.of(
                filter.getPageNumber(), filter.getPageSize(), Sort.by(Sort.Direction.ASC, "departureTime")
        );

        Specification<Flight> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(filter.departureCity())) {
                predicates.add(cb.equal(cb.lower(root.get("departureCity")), filter.departureCity().trim().toLowerCase()));
            }

            if (StringUtils.hasText(filter.arrivalCity())) {
                predicates.add(cb.equal(cb.lower(root.get("arrivalCity")), filter.arrivalCity().trim().toLowerCase()));
            }

            if (filter.departureDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("departureTime"), filter.departureDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                ));
                predicates.add(cb.lessThan(
                        root.get("departureTime"), filter.departureDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                ));
            }

            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            predicates.add(cb.greaterThan(root.get("availableSeats"), 0));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return flightRepository.findAll(spec, pageable).map(flightMapper::toDto);
    }

    /**
     * Получение DTO рейса по идентификатору.
     *
     * @param id идентификатор рейса
     * @return DTO рейса
     */
    @Override
    @Transactional(readOnly = true)
    public FlightDto getFlightById(Long id) {
        Flight flight = getFlightEntity(id);
        return flightMapper.toDto(flight);
    }

    /**
     * Получение сущности рейса для оформления заказа.
     *
     * @param id идентификатор рейса
     * @return JPA-сущность авиарейса
     */
    @Override
    @Transactional(readOnly = true)
    public Flight getFlightEntity(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Рейс с id " + id + " не найден"));
    }

    /**
     * Список доступных городов вылета.
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getDepartureCities() {
        return flightRepository.findDistinctDepartureCities();
    }

    /**
     * Список доступных городов прилета.
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> getArrivalCities() {
        return flightRepository.findDistinctArrivalCities();
    }
}
