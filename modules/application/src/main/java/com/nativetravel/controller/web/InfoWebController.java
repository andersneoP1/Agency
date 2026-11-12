package com.nativetravel.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Веб-контроллер информационных страниц: О нас (/about) и Часто задаваемые вопросы (/faq).
 */
@Controller
@RequiredArgsConstructor
public class InfoWebController {

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "О нас | Native Travel");
        model.addAttribute("activePage", "about");
        return "pages/info/about";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("pageTitle", "FAQ — Часто задаваемые вопросы | Native Travel");
        model.addAttribute("activePage", "faq");
        return "pages/info/faq";
    }
}

