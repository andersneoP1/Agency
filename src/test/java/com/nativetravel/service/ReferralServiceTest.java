package com.nativetravel.service;

import com.nativetravel.dto.ReferralDto;
import com.nativetravel.entity.PromoCode;
import com.nativetravel.entity.Referral;
import com.nativetravel.entity.User;
import com.nativetravel.repository.PromoCodeRepository;
import com.nativetravel.repository.ReferralRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.impl.ReferralServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ReferralService")
class ReferralServiceTest {

    @Mock
    private ReferralRepository referralRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private ReferralServiceImpl referralService;

    private User referrer;
    private User referred;
    private Referral referral;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(referralService, "baseUrl", "http://localhost:8080");

        referrer = User.builder().id(1L).email("referrer@example.com").referralCode("NT-REF123").firstName("Анна").lastName("Смирнова").build();
        referred = User.builder().id(2L).email("referred@example.com").referralCode("NT-NEW456").firstName("Борис").lastName("Иванов").build();
        referral = Referral.builder().id(10L).referrer(referrer).referred(referred).rewardGranted(false).build();
    }

    @Test
    @DisplayName("Регистрация реферала связывает двух пользователей")
    void registerReferral_validCode_createsReferral() {
        when(userRepository.findByReferralCode("NT-REF123")).thenReturn(Optional.of(referrer));
        when(referralRepository.findByReferredId(2L)).thenReturn(Optional.empty());

        referralService.registerReferral("NT-REF123", referred);

        verify(referralRepository).save(any(Referral.class));
    }

    @Test
    @DisplayName("Начисление награды рефереру создает промокод и ставит rewardGranted=true")
    void checkAndRewardReferrer_firstPurchase_createsPromoCode() {
        when(referralRepository.findByReferredId(2L)).thenReturn(Optional.of(referral));

        referralService.checkAndRewardReferrer(referred);

        assertThat(referral.getRewardGranted()).isTrue();
        verify(promoCodeRepository).save(any(PromoCode.class));
        verify(referralRepository).save(referral);
    }

    @Test
    @DisplayName("Получение реферальной информации возвращает ссылку и список друзей")
    void getReferralInfo_returnsDto() {
        referral.setRewardGranted(true);
        when(userRepository.findByEmail("referrer@example.com")).thenReturn(Optional.of(referrer));
        when(referralRepository.findByReferrerIdWithReferred(1L)).thenReturn(List.of(referral));

        ReferralDto info = referralService.getReferralInfo("referrer@example.com");

        assertThat(info).isNotNull();
        assertThat(info.referralCode()).isEqualTo("NT-REF123");
        assertThat(info.referralLink()).isEqualTo("http://localhost:8080/register?ref=NT-REF123");
        assertThat(info.totalInvited()).isEqualTo(1);
        assertThat(info.earnedRewardsCount()).isEqualTo(1);
        assertThat(info.invitedFriends()).containsExactly("Борис Иванов");
    }
}
