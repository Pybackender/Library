package com.example.bookmarket.controller;

import com.example.bookmarket.dto.StatisticsDto;
import com.example.bookmarket.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto stats = statisticsService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}