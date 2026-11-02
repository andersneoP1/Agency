package com.nativetravel.service;

import com.nativetravel.dto.AddToCartRequest;
import com.nativetravel.dto.CartItemDto;
import com.nativetravel.dto.CartResponse;
import com.nativetravel.entity.CartItem;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.mapper.CartMapper;
import com.nativetravel.repository.CartItemRepository;
import com.nativetravel.repository.FlightRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование CartService")
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User sampleUser;
    private Tour sampleTour;
    private CartItem sampleCartItem;
    private CartItemDto sampleCartDto;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder().id(1L).email("user@example.com").build();
        sampleTour = Tour.builder().id(1L).title("Тур").price(BigDecimal.valueOf(50000)).build();
        sampleCartItem = CartItem.builder().id(10L).user(sampleUser).tour(sampleTour).quantity(1).build();
        sampleCartDto = new CartItemDto(10L, null, null, 1, BigDecimal.valueOf(50000), null);
    }

    @Test
    @DisplayName("Получение корзины возвращает список элементов и суммарную стоимость")
    void getCart_returnsCartResponse() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(cartItemRepository.findByUserIdWithDetails(1L)).thenReturn(List.of(sampleCartItem));
        when(cartMapper.toDto(sampleCartItem)).thenReturn(sampleCartDto);

        CartResponse cart = cartService.getCart("user@example.com");

        assertThat(cart).isNotNull();
        assertThat(cart.totalItems()).isEqualTo(1);
        assertThat(cart.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(50000));
    }

    @Test
    @DisplayName("Добавление тура в корзину сохраняет позицию")
    void addItem_tour_savesCartItem() {
        AddToCartRequest request = new AddToCartRequest(1L, null, 1);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(tourRepository.findById(1L)).thenReturn(Optional.of(sampleTour));
        when(cartItemRepository.findByUserIdAndTourId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sampleCartItem);
        when(cartMapper.toDto(sampleCartItem)).thenReturn(sampleCartDto);

        CartItemDto result = cartService.addItem(request, "user@example.com");

        assertThat(result).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    @DisplayName("Удаление элемента корзины вызывает delete")
    void removeItem_deletesItem() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(sampleCartItem));

        cartService.removeItem(10L, "user@example.com");

        verify(cartItemRepository).delete(sampleCartItem);
    }

    @Test
    @DisplayName("Очистка корзины вызывает deleteByUserId")
    void clearCart_deletesAllUserItems() {
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(sampleUser));

        cartService.clearCart("user@example.com");

        verify(cartItemRepository).deleteByUserId(1L);
    }
}

