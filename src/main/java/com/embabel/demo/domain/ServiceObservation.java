package com.embabel.demo.domain;

import java.util.List;

public record ServiceObservation(
        IncidentType incidentType,
        List<DockerComposeStatus> services,
        List<String> unavailableServices) {

    public ServiceObservation {
        services = List.copyOf(services);
        unavailableServices = List.copyOf(unavailableServices);
    }
}
