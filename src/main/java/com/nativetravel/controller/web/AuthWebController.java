package com.nativetravel.controller.web;

import com.nativetravel.util.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Веб-контроллер для отображения HTML-экранов авторизации, регистрации и сброса пароля.
 */
@Tag(name = "Web - Экраны аутентификации", description = "Рендеринг страниц входа, регистрации и восстановления доступа")
@Controller
public class AuthWebController {

    @Operation(summary = "Страница авторизации", description = "Отображает форму входа в систему")
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String registered,
            @RequestParam(required = false) String reset,
            Model model
    ) {
        model.addAttribute("activePage", "login");
        model.addAttribute("pageTitle", "Вход в систему");
        model.addAttribute("error", error);
        model.addAttribute("registered", registered);
        model.addAttribute("reset", reset);
        return "pages/auth/login";
    }

    @Operation(summary = "Страница регистрации", description = "Отображает форму создания новой учетной записи")
    @GetMapping("/register")
    public String registerPage(
            @RequestParam(required = false) String ref,
            Model model
    ) {
        model.addAttribute("activePage", "register");
        model.addAttribute("pageTitle", "Регистрация");
        model.addAttribute("referralCode", ref);
        return "pages/auth/register";
    }

    @Operation(summary = "Страница запроса сброса пароля", description = "Отображает форму ввода email для получения кода восстановления")
    @GetMapping("/recovery")
    public String recoveryPage(Model model) {
        model.addAttribute("activePage", "recovery");
        model.addAttribute("pageTitle", "Восстановление пароля");
        return "pages/auth/recovery";
    }

    @Operation(summary = "Страница ввода кода подтверждения сброса пароля", description = "Отображает форму ввода 6-значного кода и нового пароля")
    @GetMapping("/recovery/confirm")
    public String recoveryConfirmPage(
            @RequestParam(required = false) String email,
            Model model
    ) {
        model.addAttribute("activePage", "recovery");
        model.addAttribute("pageTitle", "Подтверждение сброса пароля");
        model.addAttribute("email", email);
        return "pages/auth/recovery-confirm";
    }

    @Operation(summary = "Выход из системы (Web)", description = "Удаляет авторизационные cookie и перенаправляет на главную")
    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        ResponseCookie delAccess = CookieUtils.deleteAccessCookie();
        ResponseCookie delRefresh = CookieUtils.deleteRefreshCookie();
        ResponseCookie delRefreshRoot = ResponseCookie.from(CookieUtils.REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, delAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, delRefresh.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, delRefreshRoot.toString());
        return "redirect:/?logout=true";
    }
}
