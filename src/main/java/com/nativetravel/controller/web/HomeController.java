package com.nativetravel.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Веб-контроллер для отображения главной страницы и общих экранов.
 */
@Tag(name = "Web - Главная страница", description = "Рендеринг главной страницы и статических экранов")
@Controller
public class HomeController {

    /**
     * Отображает главную страницу Native Travel.
     *
     * @param model модель представления
     * @return путь к шаблону главной страницы
     */
    @Operation(summary = "Главная страница", description = "Возвращает главную страницу со списком горящих предложений и поиском")
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("activePage", "home");
        model.addAttribute("pageTitle", "Главная");
        return "pages/index";
    }
}
