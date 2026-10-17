<!-- .slide: data-background-color="#6db33f" -->

# Spring Beans as Actions

The tools you already run become the worker's actions.

Notes:
- Learning one: turn the Spring Beans you already have into AI actions.
- Best part: you already have the tools. You've been writing them for years.

---

## Turn Your Spring Beans into Agent Actions

Beans, constructor injection, repositories, and records: the building blocks you already know.

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

Today this is a plain `@Command`. Wrap it in an `@Action` and the planner can use it.

Notes:
- Coding domain first. This is plain Spring.
- Read a file. Call the model with ChatClient. Look at .entity(CodeAnalysis.class): the answer comes back as a record, not a String.
- Then save it to Redis with a normal Spring Data repository.
- Today it's a @Command. Wrap it in an @Action and the planner can use it as a tool.
- (CodingCommands.analyzeCode)

---

## Strong Typing All the Way Down

```java
// Your domain models
public record CodeAnalysis(String summary, List<String> issues,
        List<String> suggestions, int qualityScore) {}

public record RunbookAssessment(String diagnosis, List<String> evidence,
        String recommendedAction, boolean requiresApproval) {}

public enum IncidentType { OUT_OF_MEMORY, HIGH_LATENCY }
```

Model output, runbook policy, and live observations all land in the same domain types the rest of the application uses.

Notes:
- Records in, records out.
- CodeAnalysis for the coding worker. RunbookAssessment for the SRE worker. An enum for the incident type.
- The model fills your types. Not a string you parse with regex at 2 a.m.
- Question three, answered: your domain models sit at the heart of the reasoning.

---

## One Annotation Away

```java
@Component
@Agent(
    name = "sre-incident-worker",
    description = "Produce a bounded, approval-required "
        + "response to a production incident",
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

Embabel adds each `@Action` to the planner. Spring still owns the bean.

Notes:
- Here's the whole trick. Two annotations.
- @Agent on the class: this is a worker, with a name and a job description.
- @Action on the method: this is something the worker can do. Its parameters are what it needs. Its return type is what it produces.
- FIRE_ONCE on the agent: no retry storms.
- At startup Embabel finds the bean and adds every action to the planner. Spring still owns it: injection, tests, config. Nothing new to learn.

---

<!-- .slide: data-beat="2" data-takeaway="Beans as actions: the coding worker picks one safe fix and waits for approval." -->
## Live: The Coding Worker

```text
stored-analyses
safe-quick-win
```

An `@Agent` with one `@Action`: it reads Redis and chooses one bounded remediation plan.

Notes:
- Enough code. Here's a worker built exactly like that, live. The coding assistant.
- Redis holds code reviews from earlier: three services, each with issues and a quality score.
- stored-analyses lists them. safe-quick-win picks one safe fix: the review with the fewest issues, then the highest score.
- With the seed data that's the checkout controller. One fix. Read-only. Waiting for approval.
- That's Spring Data and one @Action. Next, the SRE worker: same runner, more actions.
