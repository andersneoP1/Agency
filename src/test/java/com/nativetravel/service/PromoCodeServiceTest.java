package com.nativetravel.service;

import com.nativetravel.dto.PromoCodeApplyResponse;
import com.nativetravel.dto.PromoCodeValidateRequest;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.exception.BadRequestException;
import com.nativetravel.repository.PromoCodeRepository;
import com.nativetravel.service.impl.PromoCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование PromoCodeService")
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    private PromoCode promoCode;

    @BeforeEach
    void setUp() {
        promoCode = PromoCode.builder()
                .id(1L)
                .code("WELCOME10")
                .discountPercent(10)
                .minOrderAmount(BigDecimal.valueOf(10000))
                .maxUses(100)
                .currentUses(5)
                .validUntil(Instant.now().plus(30, ChronoUnit.DAYS))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Валидация промокода успешно рассчитывает скидку")
    void validateAndCalculate_validPromo_returnsDiscount() {
        PromoCodeValidateRequest request = new PromoCodeValidateRequest("WELCOME10", BigDecimal.valueOf(100000));
        when(promoCodeRepository.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(promoCode));

        PromoCodeApplyResponse response = promoCodeService.validateAndCalculate(request);

        assertThat(response).isNotNull();
        assertThat(response.discountPercent()).isEqualTo(10);
        assertThat(response.discountAmount()).isEqualByComparingTo(BigDecimal.valueOf(10000));
        assertThat(response.finalAmount()).isEqualByComparingTo(BigDecimal.valueOf(90000));
    }

    @Test
    @DisplayName("Применение неактивного промокода выбрасывает BadRequestException")
    void validateAndCalculate_inactive_throwsException() {
        promoCode.setActive(false);
        PromoCodeValidateRequest request = new PromoCodeValidateRequest("WELCOME10", BigDecimal.valueOf(100000));
        when(promoCodeRepository.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(promoCode));

        assertThatThrownBy(() -> promoCodeService.validateAndCalculate(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("деактивирован");
    }

    @Test
    @DisplayName("Применение промокода при недостаточной сумме заказа выбрасывает BadRequestException")
    void validateAndCalculate_belowMinAmount_throwsException() {
        PromoCodeValidateRequest request = new PromoCodeValidateRequest("WELCOME10", BigDecimal.valueOf(5000));
        when(promoCodeRepository.findByCodeIgnoreCase("WELCOME10")).thenReturn(Optional.of(promoCode));

        assertThatThrownBy(() -> promoCodeService.validateAndCalculate(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Минимальная сумма заказа");
    }

    @Test
    @DisplayName("Инкремент использования промокода увеличивает счетчик")
    void incrementUses_incrementsCurrentUses() {
        promoCodeService.incrementUses(promoCode);

        assertThat(promoCode.getCurrentUses()).isEqualTo(6);
        verify(promoCodeRepository).save(promoCode);
    }
}

