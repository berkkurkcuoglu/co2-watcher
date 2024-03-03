package com.demo.co2watcher.controller.dto;

import lombok.Builder;

@Builder
public record MetricsDto(Long maxLast30Days, Double avgLast30Days) {
}
