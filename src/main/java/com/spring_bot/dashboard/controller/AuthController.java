package com.spring_bot.dashboard.controller;

import com.spring_bot.dashboard.dto.AuthView;
import com.spring_bot.dashboard.dto.LoginRequest;
import com.spring_bot.dashboard.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthView login(@RequestBody LoginRequest request, HttpSession session) {
        return authService.login(request.username(), request.password(), session);
    }

    @PostMapping("/logout")
    public void logout(HttpSession session) {
        authService.logout(session);
    }

    @GetMapping("/me")
    public AuthView me(HttpSession session) {
        return authService.toAuthView(authService.requireUser(session));
    }
}
