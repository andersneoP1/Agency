package com.nativetravel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nativetravel.dto.HotelCreateRequest;
import com.nativetravel.dto.PromoCodeCreateRequest;
import com.nativetravel.dto.TourCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@org.springframework.transaction.annotation.Transactional
@DisplayName("Интеграционные тесты панели администратора (Phase 5)")
class AdminControllerIT {

    @org.junit.jupiter.api.BeforeAll
    static void synchronizeH2IdentityCounters(@Autowired org.springframework.jdbc.core.JdbcTemplate jdbc) {
        // PostgreSQL seed migrations use setval; H2's compatibility function does
        // not advance IDENTITY columns after inserting explicit seed identifiers.
        for (String table : java.util.List.of("hotels", "tours", "promo_codes")) {
            Long next = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM " + table, Long.class);
            jdbc.execute("ALTER TABLE " + table + " ALTER COLUMN id RESTART WITH " + next);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin@nativetravel.local", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/admin/dashboard доступен для администратора")
    void getDashboard_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers", notNullValue()))
                .andExpect(jsonPath("$.totalRevenue", notNullValue()))
                .andExpect(jsonPath("$.monthlyRevenue", notNullValue()));
    }

    @Test
    @WithMockUser(username = "ivan@nativetravel.local", roles = {"USER"})
    @DisplayName("GET /api/v1/admin/dashboard запрещен для обычного пользователя (403)")
    void getDashboard_user_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@nativetravel.local", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/hotels создает новый отель")
    void createHotel_admin_returns201() throws Exception {
        HotelCreateRequest request = new HotelCreateRequest(
                "Rixos Premium Belek Test",
                "Турция",
                "Белек",
                "Ileribasi Mevkii, Belek",
                5,
                "Ультра все включено на берегу моря",
                "Пляж, Спа, Бассейн",
                "https://images.unsplash.com/photo-1566073771259-6a8506099945"
        );

        mockMvc.perform(post("/api/v1/admin/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Rixos Premium Belek Test"));
    }

    @Test
    @WithMockUser(username = "admin@nativetravel.local", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/tours создает новый тур")
    void createTour_admin_returns201() throws Exception {
        TourCreateRequest request = new TourCreateRequest(
                "Новый эксклюзивный тур в Дубай",
                "Премиальный отдых в сердце ОАЭ с посещением Бурдж-Халифа",
                "ОАЭ",
                "Дубай",
                2L,
                BigDecimal.valueOf(149000),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(17),
                7,
                "All Inclusive",
                "https://images.unsplash.com/photo-1512453979798-5ea266f8880c",
                true,
                20
        );

        mockMvc.perform(post("/api/v1/admin/tours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title").value("Новый эксклюзивный тур в Дубай"));
    }

    @Test
    @WithMockUser(username = "admin@nativetravel.local", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/promocodes создает новый промокод")
    void createPromoCode_admin_returns201() throws Exception {
        PromoCodeCreateRequest request = new PromoCodeCreateRequest(
                "ADMINTEST50",
                15,
                BigDecimal.valueOf(30000),
                100,
                60
        );

        mockMvc.perform(post("/api/v1/admin/promocodes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ADMINTEST50"));
    }

    @Test
    @WithMockUser(username = "admin@nativetravel.local", roles = {"ADMIN"})
    @DisplayName("Веб-страницы панели управления открываются для администратора")
    void adminWebPages_admin_returns200() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/admin/tours"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/admin/hotels"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/admin/bookings"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
