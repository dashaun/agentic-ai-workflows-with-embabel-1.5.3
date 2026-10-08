package com.embabel.demo.sre;

import com.embabel.demo.domain.IncidentRequest;
import com.embabel.demo.domain.RunbookAssessment;
import com.embabel.demo.domain.ServiceObservation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SreRunbook {

    public RunbookAssessment assess(IncidentRequest request, ServiceObservation observation) {
        List<String> evidence = new ArrayList<>();
        evidence.add(request.metrics());
        evidence.add(request.logs());
        if (!observation.unavailableServices().isEmpty()) {
            evidence.add("Unavailable services: " + String.join(", ", observation.unavailableServices()));
        }

        return switch (request.incidentType()) {
            case OUT_OF_MEMORY -> new RunbookAssessment(
                    "Heap pressure is consistent with an OutOfMemory failure.",
                    evidence,
                    "Capture a heap dump, roll back the latest risky change, then validate with a canary.",
                    true);
            case HIGH_LATENCY -> new RunbookAssessment(
                    "Database timeouts and cache misses are consistent with dependency saturation.",
                    evidence,
                    "Check database and cache health, then apply a bounded rollback or scale-out with approval.",
                    true);
        };
    }
}
