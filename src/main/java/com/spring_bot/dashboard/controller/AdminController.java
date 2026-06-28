package com.spring_bot.dashboard.controller;

import com.spring_bot.dashboard.dto.CreateUserRequest;
import com.spring_bot.dashboard.dto.UserView;
import com.spring_bot.dashboard.model.AppRole;
import com.spring_bot.dashboard.model.AppUser;
import com.spring_bot.dashboard.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard/api/admin")
public class AdminController {
    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/users")
    public List<UserView> users(HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN);
        return authService.allUsers();
    }

    @PostMapping("/users")
    public UserView create(@RequestBody CreateUserRequest request, HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN);
        return authService.createUser(request);
    }

    @GetMapping("/roles")
    public Map<String, Object> roles(HttpSession session) {
        AppUser user = authService.requireUser(session);
        authService.requireAnyRole(user, AppRole.ADMIN);
        return Map.of("roles", AppRole.values());
    }
}
