package com.embabel.demo.domain;

import java.util.List;

/** Every stored analysis, as one typed input for the remediation agent's blackboard. */
public record StoredCodeAnalyses(List<StoredCodeAnalysis> analyses) {

    public StoredCodeAnalyses {
        analyses = List.copyOf(analyses);
    }
}
