package com.demo.co2watcher.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SensorsRepository extends CrudRepository<Sensor, UUID> {


}
