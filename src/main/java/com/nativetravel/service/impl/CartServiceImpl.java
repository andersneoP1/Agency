package com.nativetravel.service.impl;

import com.nativetravel.dto.AddToCartRequest;
import com.nativetravel.dto.CartItemDto;
import com.nativetravel.dto.CartResponse;
import com.nativetravel.entity.CartItem;
import com.nativetravel.entity.Flight;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.CartMapper;
import com.nativetravel.repository.CartItemRepository;
import com.nativetravel.repository.FlightRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Реализация сервиса управления корзиной покупок.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final FlightRepository flightRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        User user = getUser(userEmail);
        List<CartItem> items = cartItemRepository.findByUserIdWithDetails(user.getId());

        List<CartItemDto> itemDtos = items.stream()
                .map(cartMapper::toDto)
                .toList();

        BigDecimal totalPrice = items.stream()
                .map(CartItem::getItemPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(itemDtos, items.size(), totalPrice);
    }

    @Override
    @Transactional
    public CartItemDto addItem(AddToCartRequest request, String userEmail) {
        if (request.tourId() == null && request.flightId() == null) {
            throw new BadRequestException("Необходимо указать ID тура или авиабилета для добавления в корзину");
        }

        User user = getUser(userEmail);
        CartItem cartItem;

        if (request.tourId() != null) {
            Tour tour = tourRepository.findById(request.tourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Тур с id " + request.tourId() + " не найден"));

            cartItem = cartItemRepository.findByUserIdAndTourId(user.getId(), tour.getId())
                    .map(existing -> {
                        existing.setQuantity(existing.getQuantity() + request.getSafeQuantity());
                        return existing;
                    })
                    .orElseGet(() -> CartItem.builder()
                            .user(user)
                            .tour(tour)
                            .quantity(request.getSafeQuantity())
                            .build());
        } else {
            Flight flight = flightRepository.findById(request.flightId())
                    .orElseThrow(() -> new ResourceNotFoundException("Рейс с id " + request.flightId() + " не найден"));

            cartItem = cartItemRepository.findByUserIdAndFlightId(user.getId(), flight.getId())
                    .map(existing -> {
                        existing.setQuantity(existing.getQuantity() + request.getSafeQuantity());
                        return existing;
                    })
                    .orElseGet(() -> CartItem.builder()
                            .user(user)
                            .flight(flight)
                            .quantity(request.getSafeQuantity())
                            .build());
        }

        CartItem saved = cartItemRepository.save(cartItem);
        log.info("User {} added item to cart (tourId: {}, flightId: {})", userEmail, request.tourId(), request.flightId());
        return cartMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void removeItem(Long cartItemId, String userEmail) {
        User user = getUser(userEmail);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Элемент корзины не найден"));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Нельзя удалять чужие позиции из корзины");
        }

        cartItemRepository.delete(item);
        log.info("User {} removed item {} from cart", userEmail, cartItemId);
    }

    @Override
    @Transactional
    public void clearCart(String userEmail) {
        User user = getUser(userEmail);
        cartItemRepository.deleteByUserId(user.getId());
        log.info("Cart cleared for user {}", userEmail);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с email " + email + " не найден"));
    }
}
