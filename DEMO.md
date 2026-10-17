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
| `oom-incident` | Embabel plans and runs the SRE agent: 5 actions, `COMPLETED` |
| `oom-incident --budget 2` | The action budget stops the agent: `TERMINATED` |
| `injected-incident` | Same agent with a prompt injection in the logs; the runbook and approval requirement stay in Java |
| `latency-incident` | Same agent, different runbook branch |
| `stored-analyses` | Code analyses stored in Redis |
| `safe-quick-win` | Coding agent on the same `AgentPlatform` |
| `analyze-code --file <path>` | LLM code review saved to Redis |

## In the Deck

The demo steps are spread through the talk, each right after the slide that explains it:

| STEP | SECTION | AFTER | RUNS |
|---|---|---|---|
| 1 | From Hello World to Production | The Demo We All Know | `dad-joke --about java` |
| 2 | Spring Beans as Actions | One Annotation Away | `stored-analyses`, `safe-quick-win` |
| 3 | An SRE Digital Worker | Observe and Orient Are Spring Actions | `docker-compose-status` |
| 4 | From Imperative to Goal-Oriented | Every Execution Explains Itself | `oom-incident` |
| 5 | From Imperative to Goal-Oriented | The Loop Has a Hard Stop | `oom-incident --budget 2` |
| 6 | DICE | Domain Rules Survive Model Failure | `injected-incident` |
| 7 | Why the JVM | Advantage 1: Types Are the Plan | patch 01, `javac`, `oom-incident`: `STUCK` |
| 8 | Why the JVM | Live: Ask for the Impossible | patch 02, `javac`, `oom-incident`: 6 actions |

`./run.sh` runs every step from the deck, one click per slide, including the live coding: the deck applies `demo/patches/01-require-canary-plan.patch` and `02-add-plan-canary-action.patch` and recompiles with `javac`. The steps by hand, for rehearsal or a demo without the deck:

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
        RunbookCheck check,
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
"actions" : [ "observeServices", "applyRunbook", "analyzeIncident", "checkRecommendation", "planCanary", "prepareReport" ]
```

No routing code changed: the new type created a new path to the goal.

Keep `planCanary` but drop the `canary` parameter and the run is back to five actions: the planner skips an action the goal doesn't need. That's the claim on "The Planner Chooses the Path".

### Reset

Delete `CanaryPlan.java`, remove the `canary` parameter and the `planCanary` action, and run `./mvnw test`.

## Tests

`./mvnw test` runs without a model:

- `AgentRunnerTest`: the three agent outcomes on Embabel's real planner, without Spring: `COMPLETED`, `TERMINATED` by the budget, and `STUCK` with no path to the goal
- `SreRunbookTest`: the OutOfMemory runbook and its approval requirement
- `RunbookCheckerTest`: the Jev check: not configured, escalate, pass, and fail closed when Jev is unavailable
- `DockerComposeServiceTest`: parsing `docker compose ps` JSON, both one object per line and an array
