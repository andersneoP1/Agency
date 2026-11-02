package com.nativetravel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NativeTravelApplicationTests {

    @Test
    @DisplayName("Проверка инициализации основного класса приложения")
    void mainClassLoads() {
        NativeTravelApplication app = new NativeTravelApplication();
        assertNotNull(app);
    }
}
