package com.demo.co2watcher.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sensor")
public class Sensor {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(
            mappedBy = "sensor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Measurement> measurements = new ArrayList<>();

    @OneToMany(
            mappedBy = "sensor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Alert> alerts = new ArrayList<>();

    public void addMeasurement(Measurement measurement) {
        if (this.measurements == null) {
            this.measurements = new ArrayList<>();
        }
        measurement.setSensor(this);
        this.measurements.add(measurement);
    }

    public void addAlert(Alert alert) {
        if (this.alerts == null) {
            this.alerts = new ArrayList<>();
        }
        alert.setSensor(this);
        this.alerts.add(alert);
    }
}
