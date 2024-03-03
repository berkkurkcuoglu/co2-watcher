# About

co2-watcher Application is designed to collect CO<sub>2</sub> Sensor Measurements,
and provide status/metrics information about sensors upon request.

# Author

Berk Kurkcuoglu

# Stack

* Java 17
* Maven 3.2.0
* h2-database

# Building/Running Instructions

* The app is dockerized and can be started by running the following commands:
* ```mvn clean package```
* ```docker build -t co2-watcher . ```
* ```docker run -p 8080:8080 co2-watcher```
* The server will be running at ```localhost:8080```

# API

## Save sensor measurements

POST /api/v1/sensors/{uuid}/measurements

```
{
  "co2" : 2000,
  "time" : "2019-02-01T18:55:47+00:00"
}
```

## Get Sensor status

GET /api/v1/sensors/{uuid}

```
{
  "status" : "OK" // Possible status OK,WARN,ALERT
}
```

## Get Sensor metrics

GET /api/v1/sensors/{uuid}/metrics

```
{
  "maxLast30Days" : 1200,
  "avgLast30Days" : 900
}
```

## List alerts

GET /api/v1/sensors/{uuid}/alerts

```
[
  {
    "startTime" : "2019-02-02T18:55:47+00:00",
    "endTime" : "2019-02-02T20:00:47+00:00"
  }
]
```