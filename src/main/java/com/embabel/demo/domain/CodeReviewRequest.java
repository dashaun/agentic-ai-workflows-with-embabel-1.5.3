package com.embabel.demo.domain;

public record CodeReviewRequest(
        String sourceCode,
        String language,
        String context
) {
    public static CodeReviewRequest of(String sourceCode) {
        return new CodeReviewRequest(sourceCode, "Java", "Production codebase - analyze for bugs, style, and improvements");
    }
}
