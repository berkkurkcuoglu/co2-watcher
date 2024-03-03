package com.demo.co2watcher.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "alert")
public class Alert {

    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;

    private OffsetDateTime startTime;

    private OffsetDateTime endTime;

}
