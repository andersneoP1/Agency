package com.nativetravel.service.impl;

import com.nativetravel.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса отправки email через Spring JavaMailSender.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    /**
     * Отправляет одноразовый 6-значный код для сброса пароля.
     *
     * @param toEmail адрес электронной почты получателя
     * @param code 6-значный код подтверждения
     */
    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        log.info("Sending password reset code to email: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Native Travel - Восстановление пароля");
            message.setText("Здравствуйте!\n\n"
                    + "Ваш код для восстановления пароля: " + code + "\n\n"
                    + "Код действителен в течение 15 минут. Если вы не запрашивали смену пароля, проигнорируйте это письмо.\n\n"
                    + "С уважением, команда Native Travel");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Отправляет подтверждение бронирования с билетом и ваучером.
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
        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
