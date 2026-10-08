## Digital Workers: SRE Domain

The demo worker has one goal: **produce a safe incident response for human approval**.

```text
IncidentRequest
      ↓
OBSERVE  DockerComposeService → ServiceObservation
      ↓
ORIENT   SreRunbook → RunbookAssessment
      ↓
DECIDE   Embabel Ai → IncidentResponseReport
      ↓
ACT      prepareReport → IncidentWorkflowReport
```

Each arrow is a typed Java action. Input and output types give the planner everything it
needs to build the path.

Notes:
- This is the transition from prompt/response to a digital worker.
- The goal ends with a response prepared for human approval.
- Every proposed remediation requires human approval.

---

## Observe and Orient Are Spring Actions

```java
@Action(description = "Observe every Docker Compose service", readOnly = true)
public ServiceObservation observeServices(IncidentRequest request) {
    List<DockerComposeStatus> services = dockerComposeService.statuses();
    List<String> unavailable = services.stream()
        .filter(status -> !isRunning(status.status()))
        .map(DockerComposeStatus::service)
        .toList();
    return new ServiceObservation(
        request.incidentType(), services, unavailable);
}

@Action(description = "Apply the production runbook", readOnly = true)
public RunbookAssessment applyRunbook(
        IncidentRequest request, ServiceObservation observation) {
    return runbook.assess(request, observation);
}
```

The output type of one action satisfies the input type of the next.

Notes:
- These are real methods on SreIncidentWorker.
- The live observation comes from this repository's compose.yml.

---

## The LLM Returns a Domain Object

After the prompt is grounded with the request, observation, and runbook, the action asks
Embabel for a typed object:

```java
public IncidentResponseReport analyzeIncident(
        IncidentRequest request, ServiceObservation observation,
        RunbookAssessment assessment, OperationContext context) {
    return context.ai().withDefaultLlm()
        .creating(IncidentResponseReport.class)
        .fromPrompt(prompt);
}
```

```java
public record IncidentResponseReport(
    String analysis,
    String diagnosis,
    String recommendation
) {}
```

Java owns the schema.

Notes:
- The complete, literal prompt appears in the DICE section.
- The implementation also has a deterministic runbook fallback if the model call fails.

---

## A Goal Is an Explicit Completion Condition

```java
@Action(
    description = "Create the approval-required incident report",
    readOnly = true,
    actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
@AchievesGoal(description =
    "A bounded incident response is ready for human approval")
public IncidentWorkflowReport prepareReport(
        IncidentRequest request,
        ServiceObservation observation,
        RunbookAssessment assessment,
        IncidentResponseReport response) {
    return new IncidentWorkflowReport(
        "Produce a safe incident response",
        request.incidentType(), observation,
        assessment, response, assessment.requiresApproval());
}
```

Producing this goal type completes the worker's plan.

---

## The Worker Observes the Real Compose Project

```java
@Command(description = "Show every Docker Compose service, including stopped and uncreated services")
public String dockerComposeStatus() {
    return PrettyJson.format(dockerComposeService.statuses());
}
```

`DockerComposeService.statuses()` combines the results of
`docker compose ps --all --format json` and `docker compose config --services`.

`docker-compose-status` reports all three declared services:

| SERVICE | PURPOSE |
|---|---|
| `ollama` | Local CPU model |
| `redis` | Persisted code analysis |
| `grafana-lgtm` | Logs, traces, and metrics |

Notes:
- Run docker-compose-status here if time permits.
- Stopped containers and services not yet created are still represented.
