package com.nativetravel.mapper;

import com.nativetravel.dto.CartItemDto;
import com.nativetravel.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для элементов корзины.
 */
@Mapper(componentModel = "spring", uses = {TourMapper.class, FlightMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "subtotalPrice", expression = "java(cartItem.getItemPrice())")
    CartItemDto toDto(CartItem cartItem);
}

