package com.nativetravel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.dto.SubscriptionRequest;
import com.nativetravel.dto.SupportMessageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("Интеграционные тесты контента, чата поддержки и рассылок (Phase 6)")
class InfoAndCommunicationsIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /about открывает страницу 'О нас' с кодом 200")
    void getAboutPage_returns200() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Native Travel")));
    }

    @Test
    @DisplayName("GET /faq открывает страницу часто задаваемых вопросов с кодом 200")
    void getFaqPage_returns200() throws Exception {
        mockMvc.perform(get("/faq"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("FAQ")));
    }

    @Test
    @DisplayName("POST /api/v1/support/messages обрабатывает вопрос гостя и возвращает ответ бота")
    void postSupportMessage_returnsBotResponse() throws Exception {
        SupportMessageRequest request = new SupportMessageRequest("session-test-123", "Как оплатить выбранный тур?");

        mockMvc.perform(post("/api/v1/support/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senderType").value("BOT"))
                .andExpect(jsonPath("$.message", containsString("оплат")));
    }

    @Test
    @DisplayName("GET /api/v1/support/messages возвращает историю сообщений сессии")
    void getSupportMessagesHistory_returnsList() throws Exception {
        mockMvc.perform(get("/api/v1/support/messages?sessionId=session-test-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/subscriptions/hot-offers оформляет подписку на email (201)")
    void subscribeHotOffers_returns201() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest("subscriber.unique@example.com", null);

        mockMvc.perform(post("/api/v1/subscriptions/hot-offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("subscriber.unique@example.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(username = "admin@nativetravel.local", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/broadcast-hot-offers запускает рассылку администратором")
    void broadcastHotOffers_admin_returns200() throws Exception {
        mockMvc.perform(post("/api/v1/admin/broadcast-hot-offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Рассылка успешно отправлена")));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("POST /api/v1/admin/broadcast-hot-offers запрещен для обычного пользователя (403)")
    void broadcastHotOffers_user_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/broadcast-hot-offers"))
                .andExpect(status().isForbidden());
    }
}
