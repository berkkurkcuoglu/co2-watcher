package com.demo.co2watcher.controller;

import com.demo.co2watcher.controller.dto.MeasurementDto;
import com.demo.co2watcher.controller.dto.MetricsDto;
import com.demo.co2watcher.domain.Alert;
import com.demo.co2watcher.domain.Status;
import com.demo.co2watcher.service.SensorsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping("/api/v1/sensors")
@RestController
public class SensorsController {

    private final SensorsService sensorsService;

    @PostMapping("/{uuid}/measurements")
    public ResponseEntity<Void> saveMeasurement(
            @PathVariable UUID uuid,
            @Valid @RequestBody MeasurementDto measurementDto
    ) {
        sensorsService.saveMeasurement(uuid, measurementDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<Map<String, Status>> getStatus(@PathVariable UUID uuid) {
        return ResponseEntity.ok(Collections.singletonMap("status", sensorsService.getStatus(uuid)));
    }

    @GetMapping("/{uuid}/metrics")
    public ResponseEntity<MetricsDto> getMetrics(@PathVariable UUID uuid) {
        return ResponseEntity.ok(sensorsService.getMetrics(uuid));
    }

    @GetMapping("/{uuid}/alerts")
    public ResponseEntity<List<Alert>> getAlerts(@PathVariable UUID uuid) {
        return ResponseEntity.ok(sensorsService.getAlerts(uuid));
    }
}
