package com.nativetravel.service;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Сервис мультивалютности с автоматической конвертацией и форматированием
 * цен в зависимости от текущей локали пользователя:
 * - Русский (ru): Российский рубль (RUB, ₽) — базовая валюта (курс 1.0)
 * - English (en): Доллар США (USD, $) — курс 1 USD = 90 RUB
 * - Қазақша (kk): Казахстанский тенге (KZT, ₸) — курс 1 RUB = 5 KZT
 * - Беларуская (be): Белорусский рубль (BYN, Br) — курс 1 BYN = 30 RUB
 */
@Service("currencyService")
public class CurrencyService {

    public record CurrencyInfo(
            String code,
            String symbol,
            String flag,
            double rate,
            boolean prefix
    ) {}

    public CurrencyInfo getCurrency(Locale locale) {
        if (locale == null) {
            locale = LocaleContextHolder.getLocale();
        }
        String lang = locale != null ? locale.getLanguage().toLowerCase(Locale.ROOT) : "ru";
        return switch (lang) {
            case "en" -> new CurrencyInfo("USD", "$", "🇬🇧", 1.0 / 90.0, true);
            case "kk" -> new CurrencyInfo("KZT", "₸", "🇰🇿", 5.0, false);
            case "be" -> new CurrencyInfo("BYN", "Br", "🇧🇾", 1.0 / 30.0, false);
            default   -> new CurrencyInfo("RUB", "₽", "🇷🇺", 1.0, false);
        };
    }

    public CurrencyInfo getCurrentCurrency() {
        return getCurrency(LocaleContextHolder.getLocale());
    }

    public String format(Number amountInRubles) {
        return format(amountInRubles, LocaleContextHolder.getLocale());
    }

    public String format(Number amountInRubles, Locale locale) {
        if (amountInRubles == null) {
            return "0";
        }
        CurrencyInfo info = getCurrency(locale);
        long converted = Math.round(amountInRubles.doubleValue() * info.rate());

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ROOT);
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        String formatted = df.format(converted);

        if (info.prefix()) {
            return info.symbol() + formatted;
        } else {
            return formatted + " " + info.symbol();
        }
    }

    public long convert(Number amountInRubles, Locale locale) {
        if (amountInRubles == null) {
            return 0;
        }
        CurrencyInfo info = getCurrency(locale);
        return Math.round(amountInRubles.doubleValue() * info.rate());
    }

    public BigDecimal toBaseRubles(BigDecimal amountInLocalCurrency, Locale locale) {
        if (amountInLocalCurrency == null) {
            return null;
        }
        CurrencyInfo info = getCurrency(locale);
        if (info.rate() == 1.0) {
            return amountInLocalCurrency;
        }
        double inRubles = amountInLocalCurrency.doubleValue() / info.rate();
        return BigDecimal.valueOf(Math.round(inRubles));
    }
}

