package com.nativetravel.service;

import com.nativetravel.dto.ProfileUpdateRequest;
import com.nativetravel.dto.UserDto;
import com.nativetravel.entity.Tour;
import com.nativetravel.entity.User;
import com.nativetravel.entity.ViewedTour;
import com.nativetravel.mapper.TourMapper;
import com.nativetravel.mapper.UserMapper;
import com.nativetravel.repository.UserRepository;
import com.nativetravel.repository.ViewedTourRepository;
import com.nativetravel.service.impl.ProfileServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ProfileService")
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViewedTourRepository viewedTourRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TourMapper tourMapper;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    @DisplayName("Получение профиля пользователя")
    void getProfile_returnsUserDto() {
        User user = User.builder().id(1L).email("alex@example.com").firstName("Алексей").lastName("Смирнов").build();
        UserDto dto = new UserDto(1L, "alex@example.com", "Алексей", "Смирнов", "+79991112233", null, "REF-123", Set.of("ROLE_USER"), true, Instant.now());

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = profileService.getProfile("alex@example.com");

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("alex@example.com");
    }

    @Test
    @DisplayName("Обновление данных профиля")
    void updateProfile_updatesFields() {
        User user = User.builder().id(1L).email("alex@example.com").firstName("Алексей").lastName("Смирнов").build();
        ProfileUpdateRequest req = new ProfileUpdateRequest("Алексей", "Иванов", "+79993332211");
        UserDto dto = new UserDto(1L, "alex@example.com", "Алексей", "Иванов", "+79993332211", null, "REF-123", Set.of("ROLE_USER"), true, Instant.now());

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = profileService.updateProfile(req, "alex@example.com");

        assertThat(result.lastName()).isEqualTo("Иванов");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Получение недавно просмотренных туров")
    void getViewedTours_returnsList() {
        User user = User.builder().id(1L).email("alex@example.com").build();
        Tour tour = Tour.builder().id(5L).title("Тур в Египет").build();
        ViewedTour vt = ViewedTour.builder().id(1L).user(user).tour(tour).viewedAt(Instant.now()).build();

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(viewedTourRepository.findByUserOrderByViewedAtDesc(user, PageRequest.of(0, 6)))
                .thenReturn(new PageImpl<>(List.of(vt)));

        profileService.getViewedTours("alex@example.com");

        verify(viewedTourRepository).findByUserOrderByViewedAtDesc(user, PageRequest.of(0, 6));
    }
}
