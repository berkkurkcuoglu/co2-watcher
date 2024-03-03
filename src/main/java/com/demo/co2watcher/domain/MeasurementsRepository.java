package com.demo.co2watcher.domain;

import com.demo.co2watcher.controller.dto.MetricsDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface MeasurementsRepository extends CrudRepository<Measurement, UUID> {

    @Query(value = """
            select new com.demo.co2watcher.controller.dto.MetricsDto(
               max(m.co2),
               cast(avg(m.co2) as double)
            )
            from Measurement m
            where m.sensor.id = :sensorId
            and  m.time >= :from
            """)
    MetricsDto getSensorMetricsStartingFrom(UUID sensorId, OffsetDateTime from);

    default MetricsDto getSensorMetricsInLast30Days(UUID sensorId) {
        return getSensorMetricsStartingFrom(sensorId, OffsetDateTime.now().minusDays(30));
    }
}
