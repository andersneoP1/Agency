package com.nativetravel.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@DisplayName("Интеграционные тесты каталога (REST и Web)")
class CatalogControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/tours возвращает страницу с турами из сид-данных")
    void getTours_returnsSeedTours() throws Exception {
        mockMvc.perform(get("/api/v1/tours?size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("GET /api/v1/tours?sortBy=price_desc сортирует туры по убыванию цены")
    void getTours_sortedByPriceDesc_returnsDescOrder() throws Exception {
        mockMvc.perform(get("/api/v1/tours?sortBy=price_desc&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price").value(215000.00))
                .andExpect(jsonPath("$.content[9].price").value(38500.00));
    }

    @Test
    @DisplayName("GET /api/v1/tours?stars=3 возвращает только туры 3-звёздочных отелей")
    void getTours_filteredByStars3_returnsOnly3StarTours() throws Exception {
        mockMvc.perform(get("/api/v1/tours?stars=3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].hotel.stars").value(3))
                .andExpect(jsonPath("$.content[1].hotel.stars").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/tours?stars=4 возвращает только туры 4-звёздочных отелей")
    void getTours_filteredByStars4_returnsOnly4StarTours() throws Exception {
        mockMvc.perform(get("/api/v1/tours?stars=4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].hotel.stars").value(4))
                .andExpect(jsonPath("$.content[1].hotel.stars").value(4));
    }

    @Test
    @DisplayName("GET /api/v1/tours?sortBy=stars_desc сортирует туры по звёздности от лучших")
    void getTours_sortedByStarsDesc_returnsTopStarsFirst() throws Exception {
        mockMvc.perform(get("/api/v1/tours?sortBy=stars_desc&size=10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].hotel.stars").value(5))
                .andExpect(jsonPath("$.content[9].hotel.stars").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/tours/1 возвращает детальную информацию о туре")
    void getTourById_returnsTourDetails() throws Exception {
        mockMvc.perform(get("/api/v1/tours/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.country").value("Турция"))
                .andExpect(jsonPath("$.hotel.stars").value(5));
    }

    @Test
    @DisplayName("GET /api/v1/tours/countries возвращает список уникальных стран")
    void getCountries_returnsDistinctCountries() throws Exception {
        mockMvc.perform(get("/api/v1/tours/countries")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("GET /api/v1/flights возвращает список авиабилетов")
    void getFlights_returnsSeedFlights() throws Exception {
        mockMvc.perform(get("/api/v1/flights")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(8))));
    }

    @Test
    @DisplayName("Веб-страницы каталога /tours, /flights, /compare открываются со статусом 200")
    void getCatalogWebPages_return200() throws Exception {
        mockMvc.perform(get("/tours"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/flights"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        mockMvc.perform(get("/compare"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
    }
}
