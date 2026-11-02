package com.nativetravel.controller.rest;

import com.nativetravel.dto.SurveyRequest;
import com.nativetravel.dto.SurveyResponse;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST контроллер туристического опросника и рекомендательной системы.
 */
@Tag(name = "Surveys - Опрос и рекомендации", description = "Прохождение опроса предпочтений и получение персональных рекомендаций")
@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {

    private final SurveyService surveyService;

    @Operation(summary = "Отправить ответы на опрос предпочтений", description = "Анализирует предпочтения и возвращает подходящий тур")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Опрос обработан, сформирована рекомендация", content = @Content(schema = @Schema(implementation = SurveyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<SurveyResponse> submitSurvey(
            @Valid @RequestBody SurveyRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new UnauthorizedException("Требуется авторизация в системе");
        }
        return ResponseEntity.ok(surveyService.submitSurvey(request, principal.getName()));
    }

    @Operation(summary = "Получить текущую рекомендацию тура", description = "Возвращает рекомендацию из последнего пройденного опроса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Рекомендация получена", content = @Content(schema = @Schema(implementation = SurveyResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/latest")
    public ResponseEntity<SurveyResponse> getLatest(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Требуется авторизация в системе");
        }
        return ResponseEntity.ok(surveyService.getLatestRecommendation(principal.getName()));
    }
}
