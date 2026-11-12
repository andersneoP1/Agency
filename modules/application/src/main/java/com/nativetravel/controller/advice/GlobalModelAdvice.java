package com.nativetravel.controller.advice;

import com.nativetravel.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Глобальный советник контроллеров для предоставления данных локализации
 * и мультивалютности во все представления Thymeleaf.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final CurrencyService currencyService;

    @ModelAttribute("currentCurrency")
    public CurrencyService.CurrencyInfo currentCurrency() {
        return currencyService.getCurrency(LocaleContextHolder.getLocale());
    }
}

