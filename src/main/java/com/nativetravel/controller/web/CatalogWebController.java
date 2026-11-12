package com.nativetravel.controller.web;

import com.nativetravel.dto.FlightFilterRequest;
import com.nativetravel.dto.TourDto;
import com.nativetravel.dto.TourFilterRequest;
import com.nativetravel.service.ComparisonService;
import com.nativetravel.service.CurrencyService;
import com.nativetravel.service.FavoriteService;
import com.nativetravel.service.FlightService;
import com.nativetravel.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Веб-контроллер для страниц каталога: туры, авиабилеты, карточка тура, сравнение и избранное.
 */
@Controller
@RequiredArgsConstructor
public class CatalogWebController {

    private final TourService tourService;
    private final FlightService flightService;
    private final ComparisonService comparisonService;
    private final FavoriteService favoriteService;
    private final CurrencyService currencyService;

    @GetMapping("/tours")
    public String tours(
            @ModelAttribute("filter") TourFilterRequest filter,
            Model model,
            Principal principal
    ) {
        Locale locale = LocaleContextHolder.getLocale();
        TourFilterRequest searchFilter = filter;
        if (filter != null) {
            Integer effectiveStars = filter.stars() != null ? filter.stars() : filter.minStars();
            BigDecimal minRubles = filter.minPrice() != null ? currencyService.toBaseRubles(filter.minPrice(), locale) : null;
            BigDecimal maxRubles = filter.maxPrice() != null ? currencyService.toBaseRubles(filter.maxPrice(), locale) : null;
            searchFilter = new TourFilterRequest(
                    filter.country(),
                    filter.city(),
                    minRubles,
                    maxRubles,
                    filter.startDate(),
                    filter.endDate(),
                    filter.minStars(),
                    effectiveStars,
                    filter.hotOfferOnly(),
                    filter.sortBy(),
                    filter.sortDirection(),
                    filter.page(),
                    filter.size()
            );
        }
        Page<TourDto> toursPage = tourService.getTours(searchFilter);
        model.addAttribute("pageTitle", "Каталог туров | Native Travel");
        model.addAttribute("activePage", "tours");
        model.addAttribute("toursPage", toursPage);
        model.addAttribute("countries", tourService.getAvailableCountries());
        model.addAttribute("filter", filter);

        if (principal != null) {
            List<Long> favoriteTourIds = favoriteService.getUserFavorites(principal.getName(), Pageable.unpaged())
                    .getContent().stream().map(TourDto::id).toList();
            model.addAttribute("favoriteTourIds", favoriteTourIds);
        } else {
            model.addAttribute("favoriteTourIds", Collections.emptyList());
        }

        return "pages/catalog/tours";
    }

    @GetMapping("/tours/{id}")
    public String tourDetails(
            @PathVariable Long id,
            Model model,
            Principal principal
    ) {
        String email = principal != null ? principal.getName() : null;
        tourService.recordTourView(id, email);
        TourDto tour = tourService.getTourById(id);

        boolean isFav = false;
        if (email != null) {
            isFav = favoriteService.isFavorite(id, email);
        }

        model.addAttribute("pageTitle", tour.title() + " | Native Travel");
        model.addAttribute("activePage", "tours");
        model.addAttribute("tour", tour);
        model.addAttribute("isFavorite", isFav);
        return "pages/catalog/tour-details";
    }

    @GetMapping("/flights")
    public String flights(
            @ModelAttribute("filter") FlightFilterRequest filter,
            Model model
    ) {
        model.addAttribute("pageTitle", "Авиабилеты | Native Travel");
        model.addAttribute("activePage", "flights");
        model.addAttribute("flightsPage", flightService.getFlights(filter));
        model.addAttribute("departureCities", flightService.getDepartureCities());
        model.addAttribute("arrivalCities", flightService.getArrivalCities());
        model.addAttribute("filter", filter);
        return "pages/catalog/flights";
    }

    @GetMapping("/compare")
    public String compare(
            @RequestParam(required = false) List<Long> ids,
            Model model
    ) {
        model.addAttribute("pageTitle", "Сравнение туров | Native Travel");
        model.addAttribute("activePage", "compare");
        List<TourDto> comparedTours = (ids != null && !ids.isEmpty())
                ? comparisonService.compareTours(ids)
                : Collections.emptyList();
        model.addAttribute("tours", comparedTours);
        model.addAttribute("selectedIds", ids != null ? ids : Collections.emptyList());
        return "pages/catalog/compare";
    }

    @GetMapping("/favorites")
    public String favorites(
            @PageableDefault(size = 9) Pageable pageable,
            Model model,
            Principal principal
    ) {
        model.addAttribute("pageTitle", "Избранные туры | Native Travel");
        model.addAttribute("activePage", "favorites");

        if (principal != null) {
            model.addAttribute("favoritesPage", favoriteService.getUserFavorites(principal.getName(), pageable));
        } else {
            model.addAttribute("favoritesPage", Page.empty(pageable));
        }
        return "pages/catalog/favorites";
    }
}

