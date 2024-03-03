package com.demo.co2watcher.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.OffsetDateTime;


public record MeasurementDto(@PositiveOrZero int co2, @NotNull OffsetDateTime time) {
}
