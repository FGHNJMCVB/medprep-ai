package com.medprep.controller;

import com.medprep.dto.PerformanceResponse;
import com.medprep.service.PerformanceService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(
            PerformanceService performanceService) {

        this.performanceService = performanceService;
    }

    @GetMapping("/{userId}/performance")
    public PerformanceResponse getPerformance(
            @PathVariable Long userId) {

        return performanceService.getPerformance(userId);
    }
}