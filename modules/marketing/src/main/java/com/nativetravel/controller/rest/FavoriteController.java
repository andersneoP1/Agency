package com.nativetravel.controller.rest;

import com.nativetravel.dto.MessageResponse;
import com.nativetravel.dto.TourDto;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST контроллер избранных туров пользователя.
 */
@Tag(name = "Favorites - Избранное", description = "Управление списком избранных туров авторизованного пользователя")
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "Добавить тур в избранное", description = "Добавляет тур в персональный список избранного")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тур успешно добавлен в избранное", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Тур не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/tours/{tourId}")
    public ResponseEntity<MessageResponse> addFavorite(
            @Parameter(description = "ID тура", example = "1") @PathVariable Long tourId,
            Principal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Для добавления в избранное необходимо войти в систему");
        }
        favoriteService.addFavorite(tourId, principal.getName());
        return ResponseEntity.ok(MessageResponse.of("Тур успешно добавлен в избранное"));
    }

    @Operation(summary = "Удалить тур из избранного", description = "Удаляет тур из персонального списка избранного")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тур удален из избранного", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/tours/{tourId}")
    public ResponseEntity<MessageResponse> removeFavorite(
            @Parameter(description = "ID тура", example = "1") @PathVariable Long tourId,
            Principal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Для управления избранным необходимо войти в систему");
        }
        favoriteService.removeFavorite(tourId, principal.getName());
        return ResponseEntity.ok(MessageResponse.of("Тур удален из избранного"));
    }

    @Operation(summary = "Получить список избранных туров", description = "Возвращает страницу сохраненных туров авторизованного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница избранных туров"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<Page<TourDto>> getFavorites(
            @PageableDefault(size = 9) Pageable pageable,
            Principal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Для просмотра избранного необходимо войти в систему");
        }
        return ResponseEntity.ok(favoriteService.getUserFavorites(principal.getName(), pageable));
    }
}
