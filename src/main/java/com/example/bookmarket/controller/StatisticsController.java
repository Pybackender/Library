package com.example.bookmarket.controller;

import com.example.bookmarket.dto.StatisticsDto;
import com.example.bookmarket.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/statistics")
@Tag(name = "وضعیت جامع سایت", description = "")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
    @Operation(summary = "پیدا کردن وضعیت ")
    @GetMapping
    public ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto stats = statisticsService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}