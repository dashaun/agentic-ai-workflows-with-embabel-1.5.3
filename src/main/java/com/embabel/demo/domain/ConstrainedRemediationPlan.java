package com.embabel.demo.domain;

/** A read-only recommendation: this demo action never changes a source file. */
public record ConstrainedRemediationPlan(
        String sourceId,
        String issue,
        String recommendation,
        boolean requiresApproval) {

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
