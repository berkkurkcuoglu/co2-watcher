package com.demo.co2watcher.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class MeasurementsRepositoryTest {

    @Autowired
    private MeasurementsRepository measurementsRepository;

    @Autowired
    private SensorsRepository sensorsRepository;

    @Test
    void getSensorMetricsStartingFrom_should_calculate_average_and_max_co2_accurately_for_given_time_range() {
        UUID sensorId = UUID.randomUUID();
        Sensor sensor = sensorsRepository.save(Sensor.builder().id(sensorId).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(100).time(OffsetDateTime.now().minusDays(31)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(200).time(OffsetDateTime.now().minusDays(8)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(300).time(OffsetDateTime.now().minusDays(7)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(400).time(OffsetDateTime.now().minusDays(6)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(500).time(OffsetDateTime.now().minusDays(5)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(600).time(OffsetDateTime.now().minusDays(4)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(700).time(OffsetDateTime.now().minusDays(3)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(800).time(OffsetDateTime.now().minusDays(2)).build());
        measurementsRepository.save(Measurement.builder().sensor(sensor).co2(900).time(OffsetDateTime.now().minusDays(1)).build());

        var result = measurementsRepository.getSensorMetricsStartingFrom(sensorId, OffsetDateTime.now().minusDays(30));
        assertEquals(900, result.maxLast30Days());
        assertEquals((200 + 300 + 400 + 500 + 600 + 700 + 800 + 900) / 8.0, result.avgLast30Days());
    }
}