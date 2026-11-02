package com.nativetravel.service.impl;

import com.nativetravel.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса отправки электронной почты с защитой от сбоев доставки.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * Отправка 6-значного кода сброса пароля.
     *
     * @param toEmail адрес получателя
     * @param code 6-значный код
     */
    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        log.info("Sending password reset code to {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Native Travel - Код восстановления пароля");
            message.setText("Здравствуйте!\n\n"
                    + "Ваш одноразовый код для сброса пароля: " + code + "\n\n"
                    + "Код действителен в течение 15 минут. Если вы не запрашивали смену пароля, проигнорируйте это письмо.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Отправка подтверждения бронирования с билетом и ваучером.
     *
     * @param toEmail адрес электронной почты получателя
     * @param bookingNumber номер бронирования
     * @param qrBytes байты изображения QR-кода
     * @param pdfBytes байты PDF-ваучера
     */
    @Override
    public void sendBookingConfirmation(String toEmail, String bookingNumber, byte[] qrBytes, byte[] pdfBytes) {
        log.info("Sending booking confirmation for #{} to {}", bookingNumber, toEmail);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Native Travel - Ваше бронирование #" + bookingNumber + " оплачено!");
            helper.setText("Здравствуйте!\n\n"
                    + "Ваше бронирование #" + bookingNumber + " успешно подтверждено и оплачено.\n"
                    + "Ваучер и QR-код для посадки прикреплены к этому письму.\n\n"
                    + "Приятного путешествия вместе с Native Travel!");

            if (qrBytes != null && qrBytes.length > 0) {
                helper.addAttachment("qr-code-" + bookingNumber + ".png", new ByteArrayResource(qrBytes));
            }
            if (pdfBytes != null && pdfBytes.length > 0) {
                helper.addAttachment("voucher-" + bookingNumber + ".pdf", new ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Отправляет рассылку горящих предложений.
     *
     * @param toEmail адрес подписчика
     * @param content текст рассылки
     */
    @Override
    public void sendHotOffersNewsletter(String toEmail, String content) {
        log.info("Sending hot offers newsletter to {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("🔥 Горящие туры недели от Native Travel!");
            message.setText("Здравствуйте!\n\n"
                    + "Специально для вас мы собрали подборку самых выгодных горящих предложений:\n\n"
                    + content + "\n\n"
                    + "Забронируйте отдых мечты прямо сейчас на сайте Native Travel!");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send hot offers newsletter to {}: {}", toEmail, e.getMessage());
        }
    }
}
