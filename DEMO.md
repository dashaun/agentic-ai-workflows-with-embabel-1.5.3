# Demo Script

Everything runs locally: `compose.yml` starts Ollama (with `qwen2.5-coder:1.5b` baked into the image), Redis Stack, and Grafana LGTM.

## Before You Start

- JDK 21+ (`.sdkmanrc` pins `25.0.3-librca`; run `sdk env`)
- Docker running
- Nothing else listening on port 11434. If you run Ollama natively, quit it first: the compose `ollama` service publishes that port.

```bash
./mvnw spring-boot:run
```

Spring Boot starts the compose services and waits for their health checks. Embabel then discovers the Ollama models and logs `Deployed agent sre-incident-worker`.

## Commands

| Command | What it shows |
|---|---|
| `dad-joke --about java` | Plain `ChatClient` call; warms the model |
| `docker-compose-status` | Live compose state as typed JSON |
| `oom-incident` | Embabel plans and runs the SRE agent: 4 actions, `COMPLETED` |
| `oom-incident --budget 2` | The action budget stops the agent: `TERMINATED` |
| `latency-incident` | Same agent, different runbook branch |
| `stored-analyses` | Code analyses stored in Redis |
| `safe-quick-win` | Coding agent on the same `AgentPlatform` |
| `analyze-code --file <path>` | LLM code review saved to Redis |

## Live Coding: From STUCK to a New Plan

Open `src/main/java/com/embabel/demo/sre/SreIncidentWorker.java`.

### Step 1: ask for the impossible

Create `src/main/java/com/embabel/demo/domain/CanaryPlan.java`:

```java
package com.embabel.demo.domain;

public record CanaryPlan(String change, int trafficPercent, boolean requiresApproval) {
}
```

Add a `CanaryPlan canary` parameter to `prepareReport` (and import `com.embabel.demo.domain.CanaryPlan`):

```java
public IncidentWorkflowReport prepareReport(
        IncidentRequest request,
        ServiceObservation observation,
        RunbookAssessment assessment,
        IncidentResponseReport response,
        CanaryPlan canary) {
```

Restart the shell and run `oom-incident`. Expected:

```json
"status" : "STUCK",
"actions" : [ ]
```

The planner log names the missing condition (`it:com.embabel.demo.domain.CanaryPlan`). Embabel found no path to the goal before running anything, so no LLM call was made.

### Step 2: add an action

Add to `SreIncidentWorker`:

```java
@Action(description = "Plan a canary to validate the remediation", readOnly = true)
public CanaryPlan planCanary(RunbookAssessment assessment) {
    return new CanaryPlan(assessment.recommendedAction(), 5, true);
}
```

Restart the shell and run `oom-incident`. Expected:

```json
"status" : "COMPLETED",
"actions" : [ "observeServices", "applyRunbook", "analyzeIncident", "planCanary", "prepareReport" ]
```

No routing code changed: the new type created a new path to the goal.

### Reset

Delete `CanaryPlan.java`, remove the `canary` parameter and the `planCanary` action, and run `./mvnw test`.

## Tests

`./mvnw test` covers the three agent outcomes on Embabel's real planner without Spring or a model: `COMPLETED`, `TERMINATED` by the budget, and `STUCK` with no path to the goal.
