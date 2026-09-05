package com.medprep.controller;

import com.medprep.dto.PerformanceResponse;
import com.medprep.dto.UserResponse;
import com.medprep.service.PerformanceService;
import com.medprep.service.UserService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PerformanceService performanceService;

    public UserController(
            UserService userService,
            PerformanceService performanceService) {

        this.userService = userService;
        this.performanceService = performanceService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(
                authentication.getName()
        );
    }

    @GetMapping("/me/performance")
    public PerformanceResponse getMyPerformance(
            Authentication authentication) {

        Long userId = userService.getCurrentUserId(
                authentication.getName()
        );

        return performanceService.getPerformance(userId);
    }
}