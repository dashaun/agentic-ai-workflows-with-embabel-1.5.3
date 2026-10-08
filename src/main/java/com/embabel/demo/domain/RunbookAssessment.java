package com.embabel.demo.domain;

import java.util.List;

public record RunbookAssessment(
        String diagnosis,
        List<String> evidence,
        String recommendedAction,
        boolean requiresApproval) {

    public RunbookAssessment {
        evidence = List.copyOf(evidence);
    }
}
