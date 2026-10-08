package com.embabel.demo.domain;

import java.util.List;
import java.util.Objects;

public record CodeAnalysis(
        String summary,
        List<String> issues,
        List<String> suggestions,
        int qualityScore
) {
    public CodeAnalysis {
        issues = Objects.requireNonNullElse(issues, List.of());
        suggestions = Objects.requireNonNullElse(suggestions, List.of());
    }

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
