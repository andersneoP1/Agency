package com.nativetravel.controller.web;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.CartResponse;
import com.nativetravel.dto.FlightDto;
import com.nativetravel.dto.TourDto;
import com.nativetravel.service.BookingService;
import com.nativetravel.service.CartService;
import com.nativetravel.service.FlightService;
import com.nativetravel.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

/**
 * Веб-контроллер процесса покупки: корзина, оформление, оплата и успешное подтверждение.
 */
@Controller
@RequiredArgsConstructor
public class BookingWebController {

    private final CartService cartService;
    private final BookingService bookingService;
    private final TourService tourService;
    private final FlightService flightService;

    @GetMapping("/cart")
    public String cart(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/cart";
        }
        CartResponse cart = cartService.getCart(principal.getName());
        model.addAttribute("pageTitle", "Корзина | Native Travel");
        model.addAttribute("activePage", "cart");
        model.addAttribute("cart", cart);
        return "pages/booking/cart";
    }

    @GetMapping("/booking")
    public String checkout(
            @RequestParam(required = false) Long tourId,
            @RequestParam(required = false) Long flightId,
            Model model,
            Principal principal
    ) {
        if (principal == null) {
            String redirectUrl = "/booking" + (tourId != null ? "?tourId=" + tourId : (flightId != null ? "?flightId=" + flightId : ""));
            return "redirect:/login?redirect=" + redirectUrl;
        }

        TourDto tour = null;
        FlightDto flight = null;

        if (tourId != null) {
            tour = tourService.getTourById(tourId);
        }
        if (flightId != null) {
            flight = flightService.getFlightById(flightId);
        }

        model.addAttribute("pageTitle", "Оформление заказа | Native Travel");
        model.addAttribute("activePage", "booking");
        model.addAttribute("tour", tour);
        model.addAttribute("flight", flight);
        return "pages/booking/checkout";
    }

    @GetMapping("/payment")
    public String payment(
            @RequestParam Long bookingId,
            Model model,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/login?redirect=/payment?bookingId=" + bookingId;
        }

        BookingDto booking = bookingService.getBookingById(bookingId, principal.getName());
        model.addAttribute("pageTitle", "Оплата заказа #" + booking.bookingNumber() + " | Native Travel");
        model.addAttribute("activePage", "payment");
        model.addAttribute("booking", booking);
        return "pages/booking/payment";
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(
            @RequestParam Long bookingId,
            Model model,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/login";
        }

        BookingDto booking = bookingService.getBookingById(bookingId, principal.getName());
        model.addAttribute("pageTitle", "Заказ успешно оплачен | Native Travel");
        model.addAttribute("activePage", "payment-success");
        model.addAttribute("booking", booking);
        return "pages/booking/success";
    }
}

