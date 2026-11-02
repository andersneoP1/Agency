package com.nativetravel.controller.rest;

import com.nativetravel.dto.CreateReviewRequest;
import com.nativetravel.dto.ReviewDto;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * REST контроллер отзывов к турам.
 */
@Tag(name = "Reviews - Отзывы", description = "Просмотр и добавление отзывов путешественников к турам")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Получить отзывы к туру", description = "Возвращает список всех одобренных отзывов к указанному туру")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список отзывов получен", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewDto.class)))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/tours/{tourId}")
    public ResponseEntity<List<ReviewDto>> getTourReviews(
            @Parameter(description = "ID тура", example = "1") @PathVariable Long tourId
    ) {
        return ResponseEntity.ok(reviewService.getTourReviews(tourId));
    }

    @Operation(summary = "Оставить отзыв о туре", description = "Добавляет новый отзыв с оценкой от 1 до 5 звезд (один отзыв на тур от одного пользователя)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Отзыв успешно добавлен", content = @Content(schema = @Schema(implementation = ReviewDto.class))),
            @ApiResponse(responseCode = "400", description = "Пользователь уже оставлял отзыв или ошибка валидации", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Тур не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<ReviewDto> addReview(
            @Valid @RequestBody CreateReviewRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Для написания отзыва необходимо войти в систему");
        }
        ReviewDto created = reviewService.addReview(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
