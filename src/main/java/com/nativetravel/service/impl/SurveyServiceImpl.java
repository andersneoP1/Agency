package com.nativetravel.service.impl;

import com.nativetravel.dto.SurveyRequest;
import com.nativetravel.dto.SurveyResponse;
import com.nativetravel.dto.TourDto;
import com.nativetravel.entity.Survey;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.exception.ResourceNotFoundException;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.repository.SurveyRepository;
import com.nativetravel.repository.TourRepository;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.service.SurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса подбора туров по результатам опроса.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyServiceImpl implements SurveyService {

    private final SurveyRepository surveyRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional
    public SurveyResponse submitSurvey(SurveyRequest request, String userEmail) {
        User user = getUser(userEmail);
        List<Tour> allTours = tourRepository.findAll();

        Tour recommended = selectBestTour(request, allTours);

        Survey survey = Survey.builder()
                .user(user)
                .preferredBudget(request.preferredBudget())
                .travelType(request.travelType())
                .preferredSeason(request.preferredSeason())
                .climatePreference(request.climatePreference())
                .recommendedTour(recommended)
                .build();

        Survey saved = surveyRepository.save(survey);
        log.info("Saved survey #{} for user {}, recommended tour: {}", 
                saved.getId(), userEmail, recommended != null ? recommended.getTitle() : "none");

        TourDto tourDto = recommended != null ? tourMapper.toDto(recommended) : null;
        String reason = generateRecommendationReason(request, recommended);

        return new SurveyResponse(saved.getId(), tourDto, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public SurveyResponse getLatestRecommendation(String userEmail) {
        User user = getUser(userEmail);
        Optional<Survey> surveyOpt = surveyRepository.findLatestByUserId(user.getId());

        if (surveyOpt.isEmpty()) {
            return new SurveyResponse(null, null, "Вы еще не проходили наш опрос предпочтений");
        }

        Survey survey = surveyOpt.get();
        TourDto tourDto = survey.getRecommendedTour() != null ? tourMapper.toDto(survey.getRecommendedTour()) : null;
        String reason = "Подобран специально для вас на основе ваших ответов в опросе путешествий";

        return new SurveyResponse(survey.getId(), tourDto, reason);
    }

    private Tour selectBestTour(SurveyRequest request, List<Tour> tours) {
        if (tours.isEmpty()) return null;

        String travelType = request.travelType() != null ? request.travelType().toUpperCase() : "BEACH";
        String budget = request.preferredBudget() != null ? request.preferredBudget().toUpperCase() : "MEDIUM";

        // Filter based on preference
        return tours.stream()
                .filter(t -> matchesBudget(t.getPrice(), budget))
                .filter(t -> matchesStyle(t, travelType))
                .findFirst()
                .or(() -> tours.stream().filter(t -> matchesStyle(t, travelType)).findFirst())
                .or(() -> tours.stream().findFirst())
                .orElse(null);
    }

    private boolean matchesBudget(BigDecimal price, String budget) {
        if ("ECONOMY".equalsIgnoreCase(budget)) {
            return price.compareTo(BigDecimal.valueOf(60000)) <= 0;
        } else if ("LUXURY".equalsIgnoreCase(budget)) {
            return price.compareTo(BigDecimal.valueOf(110000)) >= 0;
        }
        return price.compareTo(BigDecimal.valueOf(40000)) >= 0 && price.compareTo(BigDecimal.valueOf(140000)) <= 0;
    }

    private boolean matchesStyle(Tour tour, String style) {
        String titleDesc = (tour.getTitle() + " " + tour.getDescription() + " " + tour.getCountry()).toLowerCase();
        if ("BEACH".equalsIgnoreCase(style)) {
            return titleDesc.contains("пляж") || titleDesc.contains("море") || titleDesc.contains("океан") || titleDesc.contains("белек") || titleDesc.contains("бали");
        } else if ("MOUNTAIN".equalsIgnoreCase(style) || "ACTIVE".equalsIgnoreCase(style)) {
            return titleDesc.contains("альп") || titleDesc.contains("гора") || titleDesc.contains("озеро") || titleDesc.contains("австрия");
        } else if ("SIGHTSEEING".equalsIgnoreCase(style)) {
            return titleDesc.contains("экскур") || titleDesc.contains("стамбул") || titleDesc.contains("каппадокия") || titleDesc.contains("дубай") || titleDesc.contains("лувр");
        }
        return true;
    }

    private String generateRecommendationReason(SurveyRequest req, Tour tour) {
        if (tour == null) return "На основе ваших ответов мы подберем индивидуальный маршрут.";
        return "На основе ваших предпочтений («" + req.travelType() + "», бюджет «" + req.preferredBudget() 
                + "») мы рекомендуем этот тур. Здесь вас ждут отличные условия, комфортное проживание и высочайший уровень сервиса!";
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь " + email + " не найден"));
    }
}

