package com.embabel.demo.domain;

import java.util.List;
import java.util.Objects;

public record OpenIssuesSummary(
        List<String> issueTypes,
        String quickWin,
        String conciseSummary
) {
    public OpenIssuesSummary {
        issueTypes = Objects.requireNonNullElse(issueTypes, List.of());
        quickWin = Objects.requireNonNullElse(quickWin, "No quick win identified.");
        conciseSummary = Objects.requireNonNullElse(conciseSummary, "No open issues found.");
    }

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
