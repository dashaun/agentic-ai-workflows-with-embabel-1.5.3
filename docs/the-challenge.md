<!-- .slide: data-background-color="#6db33f" -->

# The Challenge

Two domains, the same three failures.

Notes:
- Let's make the problem concrete.
- Two domains tonight: coding assistants and SRE.
- I picked two on purpose. If the pattern only works in one, it's a trick, not a pattern.

---

## Digital Workers: Two Domains, Common Problems

### Domain 1: Coding Assistants
1. **Infinite loops**: Agent keeps refactoring the same method over and over
2. **Tool spam**: Calls linters 20 times on the same file
3. **Lost context**: Code quality standards ignored

### Domain 2: SRE Workflows
1. **Infinite loops**: Agent keeps restarting the same pod
2. **Tool spam**: Queries metrics 100 times for the same issue
3. **Lost context**: Operational procedures and runbooks ignored

Notes:
- Coding assistant. Infinite loop: it refactors the same method again and again.
- Tool spam: it runs the linter twenty times on the same file.
- Lost context: it ignores your team's standards.
- SRE worker. Same three failures: restarts the same pod, queries metrics a hundred times, ignores the runbook.
- Different domains. Identical failure modes. That's the tell: the problem is the workflow, not the model.

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

Notes:
- Be honest. We've all written this.
- Look at the first line: we're routing on response.contains("refactor"). String matching on model output.
- Every new case is another branch. Every model upgrade can break it.
- And the model still knows nothing about your standards or what's actually running.
- Nobody wants to own this code. So what replaces it?

---

## What We Need

| Capability | Benefit | Example |
|-----------|---------|---------|
| **Structured goal planning** | Worker knows what it's trying to accomplish | Goal: an approval-ready incident report |
| **Strong typing** | Domain models guide reasoning | `CodeAnalysis`, `ServiceObservation` |
| **Bounded execution** | Worker knows when to stop | Budget of 8 actions |
| **Tool as first-class** | Reuse existing tools seamlessly | Spring Beans → Agent Actions |
| **Observability** | Full visibility into decisions | Trace every OODA loop step |

Notes:
- Here's the wish list. Read it top to bottom.
- Goals instead of steps. Types instead of strings. A budget so it stops. Your beans as the tools. A trace for every decision.
- Every row shows up live tonight, and each one answers one of our three questions.
