package com.embabel.demo.domain;

public record IncidentWorkflowReport(
        String goal,
        IncidentType incidentType,
        ServiceObservation observation,
        RunbookAssessment runbookAssessment,
        IncidentResponseReport response,
        RunbookCheck runbookCheck,
        boolean requiresApproval) {
}
