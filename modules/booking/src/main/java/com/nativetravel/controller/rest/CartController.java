package com.nativetravel.controller.rest;

import com.nativetravel.dto.AddToCartRequest;
import com.nativetravel.dto.CartItemDto;
import com.nativetravel.dto.CartResponse;
import com.nativetravel.dto.MessageResponse;
import com.nativetravel.exception.UnauthorizedException;
import com.nativetravel.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * REST контроллер корзины покупок пользователя.
 */
@Tag(name = "Cart - Корзина", description = "Управление корзиной покупок: просмотр, добавление, удаление позиций")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Получить содержимое корзины", description = "Возвращает список позиций в корзине и итоговую сумму")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Корзина успешно получена", content = @Content(schema = @Schema(implementation = CartResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        validateAuth(principal);
        return ResponseEntity.ok(cartService.getCart(principal.getName()));
    }

    @Operation(summary = "Добавить тур или авиабилет в корзину", description = "Добавляет указанную позицию в корзину текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Позиция добавлена в корзину", content = @Content(schema = @Schema(implementation = CartItemDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Тур или рейс не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/items")
    public ResponseEntity<CartItemDto> addItem(
            @Valid @RequestBody AddToCartRequest request,
            Principal principal
    ) {
        validateAuth(principal);
        CartItemDto created = cartService.addItem(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Удалить позицию из корзины", description = "Удаляет элемент корзины по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Позиция успешно удалена", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Элемент корзины не найден", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/items/{id}")
    public ResponseEntity<MessageResponse> removeItem(
            @Parameter(description = "ID элемента корзины", example = "1") @PathVariable Long id,
            Principal principal
    ) {
        validateAuth(principal);
        cartService.removeItem(id, principal.getName());
        return ResponseEntity.ok(MessageResponse.of("Позиция успешно удалена из корзины"));
    }

    @Operation(summary = "Очистить всю корзину", description = "Удаляет все элементы из корзины текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Корзина очищена", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping
    public ResponseEntity<MessageResponse> clearCart(Principal principal) {
        validateAuth(principal);
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok(MessageResponse.of("Корзина успешно очищена"));
    }

    private void validateAuth(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("Требуется авторизация в системе");
        }
    }
}

