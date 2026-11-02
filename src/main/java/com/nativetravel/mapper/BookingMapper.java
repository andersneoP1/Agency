package com.nativetravel.mapper;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для сущности Booking.
 */
@Mapper(componentModel = "spring", uses = {TourMapper.class, FlightMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    @Mapping(target = "qrCodeBase64", expression = "java(booking.getQrCode() != null ? booking.getQrCode().getQrData() : null)")
    BookingDto toDto(Booking booking);
}
