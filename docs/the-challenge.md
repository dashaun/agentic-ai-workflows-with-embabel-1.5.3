## Digital Workers: Two Domains, Common Problems

### Domain 1: Coding Assistants
1. **Infinite loops**: Agent keeps refactoring the same method over and over
2. **Tool spam**: Calls linters 20 times on the same file
3. **Lost context**: Code quality standards ignored

### Domain 2: SRE Workflows
1. **Infinite loops**: Agent keeps restarting the same pod
2. **Tool spam**: Queries metrics 100 times for the same issue
3. **Lost context**: Operational procedures and runbooks ignored

---

## Imperative Hell

```java
if (response.contains("refactor")) {
    if (response.contains("method")) { refactorMethod(); }
    else if (response.contains("class")) { refactorClass(); }
} else if (response.contains("restart")) {
    if (response.contains("pod")) { restartPod(); }
    else if (response.contains("service")) { restartService(); }
}
// ...50 more nested blocks
```

Unmaintainable. The model lacks your standards, constraints, and current system state.

---

## What We Need

| Capability | Benefit | Example |
|-----------|---------|---------|
| **Structured goal planning** | Worker knows what it's trying to accomplish | Goal: an approval-ready incident report |
| **Strong typing** | Domain models guide reasoning | `CodeAnalysis`, `ServiceObservation` |
| **Bounded execution** | Worker knows when to stop | Budget of 8 actions |
| **Tool as first-class** | Reuse existing tools seamlessly | Spring Beans → Agent Actions |
| **Observability** | Full visibility into decisions | Trace every OODA loop step |
