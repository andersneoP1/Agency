package com.nativetravel.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование CurrencyService")
class CurrencyServiceTest {

    private final CurrencyService currencyService = new CurrencyService();

    @Test
    @DisplayName("Русский язык (ru) использует рубли (₽)")
    void ru_usesRubles() {
        CurrencyService.CurrencyInfo info = currencyService.getCurrency(Locale.of("ru"));
        assertThat(info.code()).isEqualTo("RUB");
        assertThat(info.symbol()).isEqualTo("₽");
        assertThat(info.rate()).isEqualTo(1.0);

        String formatted = currencyService.format(89900, Locale.of("ru"));
        assertThat(formatted).isEqualTo("89 900 ₽");
    }

    @Test
    @DisplayName("Английский язык (en) использует доллары ($)")
    void en_usesDollars() {
        CurrencyService.CurrencyInfo info = currencyService.getCurrency(Locale.of("en"));
        assertThat(info.code()).isEqualTo("USD");
        assertThat(info.symbol()).isEqualTo("$");
        assertThat(info.prefix()).isTrue();

        // 90 000 RUB / 90 = 1 000 USD
        String formatted = currencyService.format(90000, Locale.of("en"));
        assertThat(formatted).isEqualTo("$1 000");

        BigDecimal inRubles = currencyService.toBaseRubles(BigDecimal.valueOf(1000), Locale.of("en"));
        assertThat(inRubles).isEqualByComparingTo("90000");
    }

    @Test
    @DisplayName("Казахский язык (kk) использует тенге (₸)")
    void kk_usesTenge() {
        CurrencyService.CurrencyInfo info = currencyService.getCurrency(Locale.of("kk"));
        assertThat(info.code()).isEqualTo("KZT");
        assertThat(info.symbol()).isEqualTo("₸");

        // 10 000 RUB * 5 = 50 000 KZT
        String formatted = currencyService.format(10000, Locale.of("kk"));
        assertThat(formatted).isEqualTo("50 000 ₸");

        BigDecimal inRubles = currencyService.toBaseRubles(BigDecimal.valueOf(50000), Locale.of("kk"));
        assertThat(inRubles).isEqualByComparingTo("10000");
    }

    @Test
    @DisplayName("Белорусский язык (be) использует белорусские рубли (Br)")
    void be_usesBelarusianRubles() {
        CurrencyService.CurrencyInfo info = currencyService.getCurrency(Locale.of("be"));
        assertThat(info.code()).isEqualTo("BYN");
        assertThat(info.symbol()).isEqualTo("Br");

        // 30 000 RUB / 30 = 1 000 Br
        String formatted = currencyService.format(30000, Locale.of("be"));
        assertThat(formatted).isEqualTo("1 000 Br");

        BigDecimal inRubles = currencyService.toBaseRubles(BigDecimal.valueOf(1000), Locale.of("be"));
        assertThat(inRubles).isEqualByComparingTo("30000");
    }
}

