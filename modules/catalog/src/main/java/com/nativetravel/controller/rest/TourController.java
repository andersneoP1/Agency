package com.nativetravel.controller.rest;

import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.TourFilterRequest;
import com.nativetravel.service.ComparisonService;
import com.nativetravel.service.TourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * REST контроллер каталога туров: поиск, фильтрация, детали, горящие туры и сравнение.
 */
@Tag(name = "Tours - Туры и каталог", description = "Операции поиска туров, получения деталей, горящих предложений и сравнения")
@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
public class TourController {

    private final TourService tourService;
    private final ComparisonService comparisonService;

    @Operation(summary = "Поиск туров с фильтрацией и пагинацией", description = "Возвращает страницу туров по стране, городу, диапазону цен, звездности отеля и датам")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение каталога туров"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<Page<TourDto>> getTours(@ModelAttribute TourFilterRequest filter) {
        return ResponseEntity.ok(tourService.getTours(filter));
    }

    @Operation(summary = "Получение детальной информации о туре по ID", description = "Возвращает информацию о туре, отеле и фиксирует просмотр тура, если пользователь авторизован")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тур найден", content = @Content(schema = @Schema(implementation = TourDto.class))),
            @ApiResponse(responseCode = "404", description = "Тур не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TourDto> getTourById(
            @Parameter(description = "ID тура", example = "1") @PathVariable Long id,
            Principal principal
    ) {
        String email = principal != null ? principal.getName() : null;
        tourService.recordTourView(id, email);
        return ResponseEntity.ok(tourService.getTourById(id));
    }

    @Operation(summary = "Получение горящих туров", description = "Возвращает список актуальных горящих предложений со скидками")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список горящих туров", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TourDto.class)))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/hot-offers")
    public ResponseEntity<List<TourDto>> getHotOffers(
            @RequestParam(defaultValue = "6") int limit
    ) {
        return ResponseEntity.ok(tourService.getHotOffers(limit));
    }

    @Operation(summary = "Список доступных стран для отдыха", description = "Возвращает уникальные страны из активных туров для фильтра")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список стран"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/countries")
    public ResponseEntity<List<String>> getCountries() {
        return ResponseEntity.ok(tourService.getAvailableCountries());
    }

    @Operation(summary = "Сравнение выбранных туров", description = "Сравнивает до 4 туров по цене, звездам, длительности и услугам")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные для сравнения", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TourDto.class)))),
            @ApiResponse(responseCode = "400", description = "Передано более 4 туров или невалидный ID", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/compare")
    public ResponseEntity<List<TourDto>> compareTours(
            @Parameter(description = "Список ID туров для сравнения (через запятую)", example = "1,2,3")
            @RequestParam List<Long> ids
    ) {
        return ResponseEntity.ok(comparisonService.compareTours(ids));
    }
}

