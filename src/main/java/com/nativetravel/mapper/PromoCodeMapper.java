package com.nativetravel.mapper;

import com.nativetravel.dto.PromoCodeDto;
import com.nativetravel.entity.PromoCode;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для сущности PromoCode.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromoCodeMapper {

    PromoCodeDto toDto(PromoCode promoCode);

    PromoCode toEntity(PromoCodeDto dto);
}

