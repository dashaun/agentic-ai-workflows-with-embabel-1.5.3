package com.embabel.demo.domain;

/** Structured outcome of an SRE incident-response action. */
public record IncidentResponseReport(
        String analysis,
        String diagnosis,
        String recommendation) {

    /** Small models sometimes skip a field; the deterministic runbook fills the gap. */
    public IncidentResponseReport withRunbookDefaults(RunbookAssessment assessment) {
        return new IncidentResponseReport(
                isBlank(analysis) ? "The model returned no analysis." : analysis,
                isBlank(diagnosis) ? assessment.diagnosis() : diagnosis,
                isBlank(recommendation) ? assessment.recommendedAction() : recommendation);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
