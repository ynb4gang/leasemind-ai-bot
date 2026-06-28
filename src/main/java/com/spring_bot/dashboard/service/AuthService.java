package com.spring_bot.dashboard.service;

import com.spring_bot.dashboard.dto.AuthView;
import com.spring_bot.dashboard.dto.CreateUserRequest;
import com.spring_bot.dashboard.dto.UserView;
import com.spring_bot.dashboard.model.AppRole;
import com.spring_bot.dashboard.model.AppUser;
import com.spring_bot.dashboard.repo.AuthRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    public static final String SESSION_USER_KEY = "dashboardUser";

    private final AuthRepository authRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public AuthView login(String username, String password, HttpSession session) {
        AppUser user = authRepository.findByUsername(username)
                .filter(AppUser::enabled)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.passwordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        session.setAttribute(SESSION_USER_KEY, user);
        return toAuthView(user);
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public AppUser requireUser(HttpSession session) {
        Object user = session.getAttribute(SESSION_USER_KEY);
        if (user instanceof AppUser appUser) {
            return appUser;
        }
        throw new UnauthorizedException("Unauthorized");
    }

    public void requireAnyRole(AppUser user, AppRole... roles) {
        for (AppRole role : roles) {
            if (user.hasRole(role)) {
                return;
            }
        }
        throw new ForbiddenException("Forbidden");
    }

    public List<UserView> allUsers() {
        return authRepository.findAllUsers().stream().map(this::toUserView).toList();
    }

    public UserView createUser(CreateUserRequest request) {
        Set<AppRole> roles = request.roles() == null || request.roles().isEmpty()
                ? Set.of(AppRole.VIEWER)
                : request.roles().stream().filter(Objects::nonNull).map(String::trim).map(String::toUpperCase).map(AppRole::valueOf).collect(Collectors.toSet());
        long id = authRepository.createUser(request.username(), passwordEncoder.encode(request.password()), request.displayName(), roles);
        AppUser user = authRepository.findAllUsers().stream().filter(u -> u.id().equals(id)).findFirst().orElseThrow();
        return toUserView(user);
    }

    public AuthView toAuthView(AppUser user) {
        return new AuthView(user.username(), user.displayName(), user.roles().stream().map(Enum::name).collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
    }

    private UserView toUserView(AppUser user) {
        return new UserView(user.id(), user.username(), user.displayName(), user.enabled(), user.roles().stream().map(Enum::name).collect(Collectors.toCollection(java.util.LinkedHashSet::new)));
    }
}
