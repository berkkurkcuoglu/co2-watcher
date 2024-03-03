package com.demo.co2watcher.service;

import com.demo.co2watcher.controller.dto.MeasurementDto;
import com.demo.co2watcher.controller.dto.MetricsDto;
import com.demo.co2watcher.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SensorsService {

    private static final int CO2_THRESHOLD = 2000;

    private final SensorsRepository sensorsRepository;
    private final MeasurementsRepository measurementsRepository;

    @Transactional
    public void saveMeasurement(UUID sensorId, MeasurementDto measurementDto) {
        var sensor = getSensor(sensorId);
        var measurement = createMeasurement(measurementDto);
        sensor.addMeasurement(measurement);
        if (measurementDto.co2() >= CO2_THRESHOLD) {
            if (isAlertRequired(sensor.getMeasurements())) {
                sensor.addAlert(Alert.builder().startTime(measurementDto.time()).build());
                sensor.setStatus(Status.ALERT);
            } else {
                sensor.setStatus(Status.WARN);
            }
        } else {
            if (sensor.getStatus().equals(Status.ALERT) && canAlertBeRemoved(sensor.getMeasurements())) {
                sensor.getAlerts().get(sensor.getAlerts().size() - 1).setEndTime(measurementDto.time());
                sensor.setStatus(Status.OK);
            }
        }
        sensorsRepository.save(sensor);
    }

    private Sensor getSensor(UUID sensorId) {
        return sensorsRepository
                .findById(sensorId)
                .orElse(
                        Sensor.builder()
                                .id(sensorId)
                                .status(Status.OK)
                                .build()
                );
    }

    private Measurement createMeasurement(MeasurementDto measurementDto) {
        return measurementsRepository.save(
                Measurement.builder()
                        .co2(measurementDto.co2())
                        .time(measurementDto.time())
                        .build()
        );
    }

    private boolean isAlertRequired(List<Measurement> measurements) {
        return measurements.size() >= 3 &&
                doLast3MeasurementsMeetCondition(measurements, measurement -> measurement.getCo2() >= CO2_THRESHOLD);
    }

    private boolean canAlertBeRemoved(List<Measurement> measurements) {
        return doLast3MeasurementsMeetCondition(measurements, measurement -> measurement.getCo2() < CO2_THRESHOLD);
    }

    private boolean doLast3MeasurementsMeetCondition(
            List<Measurement> measurements,
            Predicate<Measurement> condition
    ) {
        return measurements.stream()
                .sorted(Comparator.comparing(Measurement::getTime).reversed())
                .limit(3)
                .allMatch(condition);
    }

    public Status getStatus(UUID sensorId) {
        return sensorsRepository
                .findById(sensorId)
                .map(Sensor::getStatus)
                .orElseThrow();
    }

    public List<Alert> getAlerts(UUID sensorId) {
        return sensorsRepository
                .findById(sensorId)
                .map(Sensor::getAlerts)
                .orElseThrow();
    }

    public MetricsDto getMetrics(UUID sensorId) {
        return measurementsRepository.getSensorMetricsInLast30Days(sensorId);
    }
}
