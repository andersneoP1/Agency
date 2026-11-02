package com.nativetravel.mapper;

import com.nativetravel.dto.ReviewDto;
import com.nativetravel.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct маппер для сущности Review.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "tourId", source = "tour.id")
    @Mapping(target = "tourTitle", source = "tour.title")
    @Mapping(target = "authorName", expression = "java(review.getUser() != null ? review.getUser().getFullName() : \"Аноним\")")
    ReviewDto toDto(Review review);
}
