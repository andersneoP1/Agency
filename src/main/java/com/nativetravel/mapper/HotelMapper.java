package com.nativetravel.mapper;

import com.nativetravel.dto.HotelDto;
import com.nativetravel.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для сущности Hotel.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface HotelMapper {

    HotelDto toDto(Hotel hotel);

    Hotel toEntity(HotelDto dto);
}
