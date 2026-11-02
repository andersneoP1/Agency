package com.nativetravel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.dto.CreateReviewRequest;
import com.nativetravel.dto.ProfileUpdateRequest;
import com.nativetravel.dto.SurveyRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("Интеграционные тесты профиля, отзывов и опроса (Phase 4)")
class ProfileControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("GET /api/v1/profile возвращает данные текущего пользователя")
    void getProfile_returnsUser() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ivan@nativetravel.local"))
                .andExpect(jsonPath("$.referralCode", notNullValue()));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("PUT /api/v1/profile обновляет имя и телефон пользователя")
    void updateProfile_updatesData() throws Exception {
        ProfileUpdateRequest req = new ProfileUpdateRequest("Иван", "Петров-Обновленный", "+79997776655");

        mockMvc.perform(put("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Петров-Обновленный"))
                .andExpect(jsonPath("$.phone").value("+79997776655"));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("POST /api/v1/reviews добавляет отзыв к туру")
    void addReview_createsReview() throws Exception {
        CreateReviewRequest req = new CreateReviewRequest(3L, 5, "Потрясающий отдых в Хургаде!");

        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Потрясающий отдых в Хургаде!"));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("POST /api/v1/surveys сохраняет ответы и возвращает рекомендацию")
    void submitSurvey_returnsRecommendation() throws Exception {
        SurveyRequest req = new SurveyRequest("MEDIUM", "BEACH", "SUMMER", "WARM");

        mockMvc.perform(post("/api/v1/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendedTour", notNullValue()))
                .andExpect(jsonPath("$.recommendationReason", containsString("На основе ваших предпочтений")));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("Веб-страницы профиля и опроса открываются с кодом 200")
    void webPages_return200() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/survey"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}

