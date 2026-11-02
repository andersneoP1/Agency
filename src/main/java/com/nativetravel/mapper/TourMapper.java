package com.nativetravel.mapper;

import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.Tour;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для сущности Tour.
 */
@Mapper(componentModel = "spring", uses = { HotelMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TourMapper {

    TourDto toDto(Tour tour);

    Tour toEntity(TourDto dto);
}
