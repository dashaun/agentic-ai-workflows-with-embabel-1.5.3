package com.embabel.demo.sre;

import com.embabel.demo.domain.IncidentRequest;
import com.embabel.demo.domain.IncidentType;
import com.embabel.demo.domain.RunbookAssessment;
import com.embabel.demo.domain.ServiceObservation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SreRunbookTest {

    private final SreRunbook runbook = new SreRunbook();

    @Test
    void appliesTheOutOfMemoryRunbookAndRequiresApproval() {
        IncidentRequest request = IncidentRequest.outOfMemory();
        ServiceObservation observation = new ServiceObservation(
                IncidentType.OUT_OF_MEMORY,
                List.of(),
                List.of("ollama"));

        RunbookAssessment assessment = runbook.assess(request, observation);

        assertThat(assessment.diagnosis()).contains("OutOfMemory");
        assertThat(assessment.evidence()).anyMatch(value -> value.contains("ollama"));
        assertThat(assessment.recommendedAction()).contains("heap dump", "canary");
        assertThat(assessment.requiresApproval()).isTrue();
    }
}
