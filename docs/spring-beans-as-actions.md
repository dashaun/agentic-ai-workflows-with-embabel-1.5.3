## Turn Your Spring Beans into Agent Actions

Your existing Spring components become the worker's **action surface**.

If you already know Spring, the building blocks are familiar: beans, constructor injection,
repositories, and records.

### Coding Domain Example:
```java
String sourceCode = Files.readString(Paths.get(filePath));
CodeReviewRequest request = CodeReviewRequest.of(sourceCode);

CodeAnalysis analysis = chatClient.prompt()
    .user(reviewPrompt(request, sourceCode))
    .call()
    .entity(CodeAnalysis.class);

return codeAnalysisRepository.save(
    new StoredCodeAnalysis(filePath, analysis)).analysis();
```

📍 View: `src/main/java/com/embabel/demo/shell/CodingCommands.java:22-74`

Your existing coding components are actions the worker can plan around.

---

## Spring Bean Actions: SRE

### SRE Domain Example:
```java
@Component
public class SRECommands {
    private final AgentRunner agentRunner;

    @Command(description =
        "Resolve an OutOfMemory incident with the bounded SRE worker")
    public PlanExecution<IncidentWorkflowReport> oomIncident() {
        return pursue(IncidentRequest.outOfMemory());
    }

    private PlanExecution<IncidentWorkflowReport> pursue(
            IncidentRequest request) {
        return agentRunner.run(
            SreIncidentWorker.NAME, request,
            IncidentWorkflowReport.class);
    }
}
```

`IncidentRequest` carries the incident type, metrics, and logs into the plan.

---

## SRE Actions in the Shell

The shell exposes the Spring Bean's operational actions:

```
oom-incident          → IncidentRequest.outOfMemory()
latency-incident      → IncidentRequest.highLatency()
docker-compose-status → pretty-printed DockerComposeStatus JSON array
```

---

## Strong Typing All the Way Down

```java
// Your domain models
public record CodeAnalysis(
    String summary,
    List<String> issues,
    List<String> suggestions,
    int qualityScore
) {}

public record IncidentResponseReport(
    String analysis,
    String diagnosis,
    String recommendation
) {}

public enum IncidentType {
    OUT_OF_MEMORY, HIGH_LATENCY
}

// The model call inside the action requests the Java type
context.ai().withDefaultLlm()
    .createObject(prompt, IncidentResponseReport.class);
```

The model output lands directly in the same domain types used by the rest of the application.

---

## One Annotation Away

```java
@Component
@Agent(
    name = "sre-incident-worker",
    description = "Produce a bounded, approval-required response to a production incident",
    actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
public class SreIncidentWorker {

    @Action(
        description = "Apply the production runbook to the observation",
        readOnly = true)
    public RunbookAssessment applyRunbook(
            IncidentRequest request,
            ServiceObservation observation) {
        return runbook.assess(request, observation);
    }
}
```

📍 **Demo Implementation:** `src/main/java/com/embabel/demo/sre/SreIncidentWorker.java`

At startup Embabel deploys the bean to the `AgentPlatform` and adds each method to the planner's available actions. Spring still owns the bean,
its dependencies, tests, and observability.
