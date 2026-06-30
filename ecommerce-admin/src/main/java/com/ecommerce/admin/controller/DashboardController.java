package com.ecommerce.admin.controller;

import com.ecommerce.admin.service.DashboardService;
import com.ecommerce.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}