package com.demo.co2watcher.service;

import com.demo.co2watcher.controller.dto.MeasurementDto;
import com.demo.co2watcher.controller.dto.MetricsDto;
import com.demo.co2watcher.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorsServiceTest {

    @Mock
    private SensorsRepository sensorsRepository;
    @Mock
    private MeasurementsRepository measurementsRepository;
    @InjectMocks
    private SensorsService sensorsService;

    @Test
    void saveMeasurement_should_set_sensor_status_to_warn_when_co2_is_higher_than_2000() {
        var mockSensorId = UUID.randomUUID();
        var mockMeasurement = new MeasurementDto(2000, OffsetDateTime.now());

        when(measurementsRepository.save(any())).thenReturn(Measurement.builder().co2(2000).build());

        sensorsService.saveMeasurement(mockSensorId, mockMeasurement);

        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorsRepository).save(captor.capture());
        var result = captor.getValue();

        assertEquals(mockSensorId, result.getId());
        assertEquals(1, result.getMeasurements().size());
        assertEquals(Status.WARN, result.getStatus());
    }

    @Test
    void saveMeasurement_should_set_sensor_status_to_alert_and_store_an_alert_when_co2_is_higher_than_2000_for_3_consecutive_times() {
        var oldMeasurements = new ArrayList<Measurement>();
        oldMeasurements.add(Measurement.builder().co2(2000).time(OffsetDateTime.now()).build());
        oldMeasurements.add(Measurement.builder().co2(2000).time(OffsetDateTime.now()).build());
        var mockSensor = Sensor.builder()
                .measurements(oldMeasurements)
                .build();
        var lastMeasurementTime = OffsetDateTime.now();

        when(sensorsRepository.findById(any())).thenReturn(Optional.of(mockSensor));
        when(measurementsRepository.save(any())).thenReturn(Measurement.builder().co2(2000).time(lastMeasurementTime).build());

        sensorsService.saveMeasurement(UUID.randomUUID(), new MeasurementDto(2000, lastMeasurementTime));

        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorsRepository).save(captor.capture());
        var result = captor.getValue();

        assertEquals(3, result.getMeasurements().size());
        assertEquals(Status.ALERT, result.getStatus());
        assertEquals(1, result.getAlerts().size());
        assertEquals(lastMeasurementTime, result.getAlerts().get(0).getStartTime());
    }

    @Test
    void saveMeasurement_should_set_sensor_status_back_to_ok_when_co2_is_lower_than_2000_for_3_consecutive_times() {
        var oldMeasurements = new ArrayList<Measurement>();
        oldMeasurements.add(Measurement.builder().co2(2000).time(OffsetDateTime.now().minusDays(1)).build());
        oldMeasurements.add(Measurement.builder().co2(2000).time(OffsetDateTime.now().minusDays(1)).build());
        oldMeasurements.add(Measurement.builder().co2(2000).time(OffsetDateTime.now().minusDays(1)).build());
        oldMeasurements.add(Measurement.builder().co2(100).time(OffsetDateTime.now()).build());
        oldMeasurements.add(Measurement.builder().co2(100).time(OffsetDateTime.now()).build());
        var existingAlerts = new ArrayList<Alert>();
        existingAlerts.add(Alert.builder().startTime(OffsetDateTime.now()).build());
        var mockSensor = Sensor.builder()
                .status(Status.ALERT)
                .measurements(oldMeasurements)
                .alerts(existingAlerts)
                .build();
        var lastMeasurementTime = OffsetDateTime.now();

        when(sensorsRepository.findById(any())).thenReturn(Optional.of(mockSensor));
        when(measurementsRepository.save(any())).thenReturn(Measurement.builder().co2(100).time(lastMeasurementTime).build());

        sensorsService.saveMeasurement(UUID.randomUUID(), new MeasurementDto(100, lastMeasurementTime));

        ArgumentCaptor<Sensor> captor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorsRepository).save(captor.capture());
        var result = captor.getValue();

        assertEquals(6, result.getMeasurements().size());
        assertEquals(Status.OK, result.getStatus());
        assertEquals(1, result.getAlerts().size());
        assertEquals(lastMeasurementTime, result.getAlerts().get(0).getEndTime());
    }

    @Test
    void getStatus_should_throw_exception_when_sensor_is_not_found() {
        when(sensorsRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> sensorsService.getStatus(any()));
    }


    @Test
    void getStatus_should_return_sensor_status_when_it_is_found() {
        var mockSensor = Sensor.builder().status(Status.OK).build();
        when(sensorsRepository.findById(any())).thenReturn(Optional.of(mockSensor));

        assertEquals(mockSensor.getStatus(), sensorsService.getStatus(any()));
    }

    @Test
    void getAlerts_should_throw_exception_when_sensor_is_not_found() {
        when(sensorsRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> sensorsService.getAlerts(any()));
    }

    @Test
    void getAlerts_should_return_all_alerts_when_entries_are_found() {
        var mockSensor = Sensor.builder().id(UUID.randomUUID()).status(Status.ALERT).build();
        var mockAlert1 = Alert.builder().sensor(mockSensor).startTime(OffsetDateTime.now()).build();
        var mockAlert2 = Alert.builder().sensor(mockSensor).startTime(OffsetDateTime.now()).build();
        mockSensor.setAlerts(List.of(mockAlert1, mockAlert2));
        when(sensorsRepository.findById(any())).thenReturn(Optional.of(mockSensor));

        var result = sensorsService.getAlerts(any());

        assertEquals(2, result.size());
        assertThat(result).containsExactlyInAnyOrder(mockAlert1, mockAlert2);
    }

    @Test
    void getMetrics_should_return_the_repository_response() {
        var mockMetrics = MetricsDto.builder().maxLast30Days(2000L).avgLast30Days(1500.0).build();
        when(measurementsRepository.getSensorMetricsInLast30Days(any())).thenReturn(mockMetrics);

        assertEquals(mockMetrics, sensorsService.getMetrics(any()));
    }
}