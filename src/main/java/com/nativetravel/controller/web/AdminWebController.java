package com.nativetravel.controller.web;

import com.nativetravel.dto.TourDto;
import com.nativetravel.service.AdminService;
import com.nativetravel.service.ReviewService;
import com.nativetravel.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Веб-контроллер панели администратора (HTML представления).
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminWebController {

    private final AdminService adminService;
    private final TourService tourService;
    private final ReviewService reviewService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Дашборд администратора | Native Travel");
        model.addAttribute("activeAdminTab", "dashboard");
        model.addAttribute("stats", adminService.getDashboardStats());
        return "pages/admin/dashboard";
    }

    @GetMapping("/tours")
    public String tours(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("pageTitle", "Управление турами | Панель администратора");
        model.addAttribute("activeAdminTab", "tours");
        model.addAttribute("toursPage", tourService.getTours(null));
        return "pages/admin/tours";
    }

    @GetMapping("/tours/new")
    public String newTourForm(Model model) {
        model.addAttribute("pageTitle", "Добавить новый тур | Native Travel");
        model.addAttribute("activeAdminTab", "tours");
        model.addAttribute("hotels", adminService.getAllHotels());
        model.addAttribute("tour", null);
        return "pages/admin/tour-form";
    }

    @GetMapping("/tours/edit/{id}")
    public String editTourForm(@PathVariable Long id, Model model) {
        TourDto tour = tourService.getTourById(id);
        model.addAttribute("pageTitle", "Редактировать тур | Native Travel");
        model.addAttribute("activeAdminTab", "tours");
        model.addAttribute("hotels", adminService.getAllHotels());
        model.addAttribute("tour", tour);
        return "pages/admin/tour-form";
    }

    @GetMapping("/hotels")
    public String hotels(Model model) {
        model.addAttribute("pageTitle", "Управление отелями | Native Travel");
        model.addAttribute("activeAdminTab", "hotels");
        model.addAttribute("hotels", adminService.getAllHotels());
        return "pages/admin/hotels";
    }

    @GetMapping("/users")
    public String users(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("pageTitle", "Пользователи | Native Travel");
        model.addAttribute("activeAdminTab", "users");
        model.addAttribute("usersPage", adminService.getAllUsers(pageable));
        return "pages/admin/users";
    }

    @GetMapping("/reviews")
    public String reviews(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("pageTitle", "Модерация отзывов | Native Travel");
        model.addAttribute("activeAdminTab", "reviews");
        model.addAttribute("reviewsPage", reviewService.getAllReviews(pageable));
        return "pages/admin/reviews";
    }

    @GetMapping("/promocodes")
    public String promoCodes(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("pageTitle", "Промокоды | Native Travel");
        model.addAttribute("activeAdminTab", "promocodes");
        model.addAttribute("promoCodesPage", adminService.getAllPromoCodes(pageable));
        return "pages/admin/promocodes";
    }

    @GetMapping("/bookings")
    public String bookings(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("pageTitle", "Все бронирования | Native Travel");
        model.addAttribute("activeAdminTab", "bookings");
        model.addAttribute("bookingsPage", adminService.getAllBookings(pageable));
        return "pages/admin/bookings";
    }
}
