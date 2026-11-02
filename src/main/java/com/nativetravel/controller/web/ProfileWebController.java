package com.nativetravel.controller.web;

import com.nativetravel.dto.BookingDto;
import com.nativetravel.dto.ReferralDto;
import com.nativetravel.dto.SurveyResponse;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.UserDto;
import com.nativetravel.service.BookingService;
import com.nativetravel.service.ProfileService;
import com.nativetravel.service.ReferralService;
import com.nativetravel.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;

/**
 * Веб-контроллер личного кабинета пользователя и опросника предпочтений.
 */
@Controller
@RequiredArgsConstructor
public class ProfileWebController {

    private final ProfileService profileService;
    private final BookingService bookingService;
    private final ReferralService referralService;
    private final SurveyService surveyService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/profile";
        }

        String email = principal.getName();
        UserDto user = profileService.getProfile(email);
        Page<BookingDto> bookings = bookingService.getUserBookings(email, PageRequest.of(0, 20));
        List<TourDto> viewedTours = profileService.getViewedTours(email);
        ReferralDto referral = referralService.getReferralInfo(email);
        SurveyResponse survey = surveyService.getLatestRecommendation(email);

        model.addAttribute("pageTitle", "Личный кабинет | Native Travel");
        model.addAttribute("activePage", "profile");
        model.addAttribute("user", user);
        model.addAttribute("bookings", bookings.getContent());
        model.addAttribute("viewedTours", viewedTours);
        model.addAttribute("referral", referral);
        model.addAttribute("survey", survey);

        return "pages/user/profile";
    }

    @GetMapping("/survey")
    public String survey(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login?redirect=/survey";
        }

        model.addAttribute("pageTitle", "Подбор тура мечты | Native Travel");
        model.addAttribute("activePage", "survey");
        return "pages/user/survey";
    }
}

