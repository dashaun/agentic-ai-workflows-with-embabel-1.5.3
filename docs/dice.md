## DICE: Domain-Integrated Context Engineering

Operational context grounds the model's reasoning.

```text
IncidentRequest          What happened?
       +
ServiceObservation       What is true right now?
       +
RunbookAssessment        What does our team permit?
       ↓
Embabel Ai               Reason inside those boundaries
       ↓
IncidentResponseReport   Return a typed recommendation
```

DICE keeps domain knowledge in Java, where it is testable, reviewable, and versioned.

Notes:
- Define DICE using the wording from the abstract.
- Context comes from typed application state, live observations, and team policy.

---

## The Runbook Is Executable Domain Knowledge

```java
return switch (request.incidentType()) {
    case OUT_OF_MEMORY -> new RunbookAssessment(
        "Heap pressure is consistent with an OutOfMemory failure.",
        evidence,
        "Capture a heap dump, roll back the latest risky change, " +
            "then validate with a canary.",
        true);

    case HIGH_LATENCY -> new RunbookAssessment(
        "Database timeouts and cache misses are consistent " +
            "with dependency saturation.",
        evidence,
        "Check database and cache health, then apply a bounded " +
            "rollback or scale-out with approval.",
        true);
};
```

The LLM can explain and prioritize. Java keeps the approval requirement fixed.

---

## Live State Becomes Model Context

```java
return context.ai().withDefaultLlm()
    .creating(IncidentResponseReport.class)
    .fromPrompt("""
    You are an SRE following a production runbook.

    Incident: %s
    Metrics: %s
    Logs: %s
    Unavailable services: %s
    Runbook diagnosis: %s
    Approved strategy: %s

    Return a concise incident response with analysis,
    diagnosis, and recommendation.
    Keep every recommendation inside the approved strategy.
    """.formatted(
        request.incidentType().displayName(),
        request.metrics(), request.logs(),
        observation.unavailableServices(),
        assessment.diagnosis(), assessment.recommendedAction()))
    .withRunbookDefaults(assessment);
```

This is **beyond RAG**: context includes live tools, typed state, and executable policy.

---

## Domain Rules Survive Model Failure

```java
catch (RuntimeException ex) {
    return new IncidentResponseReport(
        "The LLM was unavailable; " +
            "the deterministic runbook result was retained.",
        assessment.diagnosis(),
        assessment.recommendedAction());
}
```

The worker still produces a safe, typed result when the model is slow or unavailable.

**Policy remains available through every model outcome.**

Notes:
- This fallback is in SreIncidentWorker.analyzeIncident.
- It is especially relevant for the CPU-based local Ollama demo.
- withRunbookDefaults also fills any field a small model leaves empty.
