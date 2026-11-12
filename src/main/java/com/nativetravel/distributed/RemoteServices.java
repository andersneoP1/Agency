package com.nativetravel.distributed;

import com.nativetravel.dto.*;
import com.nativetravel.entity.*;
import com.nativetravel.mapper.BookingMapper;
import com.nativetravel.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import static com.nativetravel.distributed.InternalRequests.*;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "app.distributed", havingValue = "true")
public class RemoteServices {
    @Bean
    @ConditionalOnExpression("'${app.service.kind:}' == 'identity' or '${app.service.kind:}' == 'payments'")
    EmailService remoteEmail(CloudTransport.Calls calls) {
        return new EmailService() {
            public void sendPasswordResetCode(String email, String code) {
                calls.post("communications", "email/reset", new Email(email, code, null, null, null, null), Void.class);
            }
            public void sendBookingConfirmation(String email, String number, byte[] qr, byte[] pdf) {
                calls.post("communications", "email/booking", new Email(email, null, number, qr, pdf, null), Void.class);
            }
            public void sendHotOffersNewsletter(String email, String content) {
                calls.post("communications", "email/newsletter", new Email(email, null, null, null, null, content), Void.class);
            }
        };
    }

    @Bean
    @ConditionalOnExpression("'${app.service.kind:}' == 'booking' or '${app.service.kind:}' == 'payments'")
    PromoCodeService remotePromo(CloudTransport.Calls calls) {
        return new PromoCodeService() {
            public PromoCodeApplyResponse validateAndCalculate(PromoCodeValidateRequest request) {
                return calls.post("marketing", "promos/validate", request, PromoCodeApplyResponse.class);
            }
            public PromoCode getValidPromoCode(String code, BigDecimal amount) {
                return calls.post("marketing", "promos/lookup", new Promo(code, amount), PromoSnapshot.class).toEntity();
            }
            public void incrementUses(PromoCode code) {
                calls.post("marketing", "promos/use", new Id(code.getId()), Void.class);
            }
        };
    }

    @Bean
    @ConditionalOnExpression("'${app.service.kind:}' == 'identity' or '${app.service.kind:}' == 'payments'")
    ReferralService remoteReferral(CloudTransport.Calls calls) {
        return new ReferralService() {
            public void registerReferral(String code, User user) {
                calls.post("marketing", "referrals/register", new InternalRequests.Referral(user.getId(), code), Void.class);
            }
            public void checkAndRewardReferrer(User user) {
                calls.post("marketing", "referrals/reward", new Id(user.getId()), Void.class);
            }
            public ReferralDto getReferralInfo(String email) {
                return calls.post("marketing", "referrals/info", new EmailLookup(email), ReferralDto.class);
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "app.service.kind", havingValue = "payments")
    QrCodeService remoteQr(CloudTransport.Calls calls) {
        return new QrCodeService() {
            public String generateQrCodeBase64(String content, int width, int height) {
                return calls.post("communications", "qr/base64", new Qr(content, width, height), String.class);
            }
            public byte[] generateQrCodePng(String content, int width, int height) {
                return calls.post("communications", "qr/png", new Qr(content, width, height), byte[].class);
            }
        };
    }

    @Bean
    @ConditionalOnExpression("'${app.service.kind:}' == 'booking' or '${app.service.kind:}' == 'payments'")
    PdfVoucherService remoteVoucher(CloudTransport.Calls calls, BookingMapper mapper) {
        return booking -> calls.post("communications", "documents/voucher",
                new Voucher(mapper.toDto(booking), booking.getUser().getFirstName(),
                        booking.getUser().getLastName(), booking.getUser().getEmail()), byte[].class);
    }

    @Bean
    @ConditionalOnProperty(name = "app.service.kind", havingValue = "booking")
    PaymentService remotePayment(CloudTransport.Calls calls) {
        return (request, email) -> calls.post("payments", "payments/process", new Payment(request, email), PaymentResponse.class);
    }
}
