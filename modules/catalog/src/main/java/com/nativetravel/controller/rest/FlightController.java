package com.nativetravel.controller.rest;

import com.nativetravel.dto.FlightDto;
import com.nativetravel.dto.FlightFilterRequest;
import com.nativetravel.service.FlightService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST контроллер поиска и получения авиабилетов.
 */
@Tag(name = "Flights - Авиабилеты", description = "Операции поиска авиарейсов, расписания и направлений")
@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @Operation(summary = "Поиск авиабилетов с фильтрацией и пагинацией", description = "Возвращает страницу авиарейсов по городам вылета и прилета, датам и ценовому диапазону")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный поиск рейсов"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<Page<FlightDto>> getFlights(@ModelAttribute FlightFilterRequest filter) {
        return ResponseEntity.ok(flightService.getFlights(filter));
    }

    @Operation(summary = "Получение информации о рейсе по ID", description = "Возвращает детали конкретного авиарейса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Рейс найден", content = @Content(schema = @Schema(implementation = FlightDto.class))),
            @ApiResponse(responseCode = "404", description = "Рейс не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlightDto> getFlightById(
            @Parameter(description = "ID авиарейса", example = "1") @PathVariable Long id
    ) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @Operation(summary = "Список доступных городов вылета", description = "Возвращает список городов отправления для селектора формы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список городов вылета", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/departure-cities")
    public ResponseEntity<List<String>> getDepartureCities() {
        return ResponseEntity.ok(flightService.getDepartureCities());
    }

    @Operation(summary = "Список доступных городов прилета", description = "Возвращает список городов назначения для селектора формы")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список городов прилета", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/arrival-cities")
    public ResponseEntity<List<String>> getArrivalCities() {
        return ResponseEntity.ok(flightService.getArrivalCities());
    }
}

