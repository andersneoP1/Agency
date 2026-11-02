package com.nativetravel.mapper;

import com.nativetravel.dto.FlightDto;
import com.nativetravel.entity.Flight;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для сущности Flight.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlightMapper {

    FlightDto toDto(Flight flight);

    Flight toEntity(FlightDto dto);
}
