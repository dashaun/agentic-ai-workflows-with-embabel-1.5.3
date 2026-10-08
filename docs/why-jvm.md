## Why the JVM Fits Production AI

Everything you just saw was Spring Beans, records, tests, and a budget. Fast prototypes matter;
production workers also need strong types, mature operations, and direct access to the
systems we already run.

Notes:
- Closing argument: everything in the demo was plain Spring and Java
- Ask how many people already run Spring in production

---

## Advantage 1: Strong Typing

```java
public record CodeAnalysis(
    String summary,
    List<String> issues,
    List<String> suggestions,
    int qualityScore
) {}
```

The domain model carries meaning through every action in the workflow.

Notes:
- Java naming conventions
- Have been a blessing
- In the age of AI

---

## Advantage 2: Production Ready

The JVM has **30 years** of enterprise battle-testing:

- **Observability**: Structured logging, tracing, metrics built in
- **Reliability**: Process isolation, crash recovery, memory management
- **Security**: Bytecode verification, sandboxing, proven patterns
- **Performance**: JIT compilation optimized for real workloads

Notes:
- The JVM is already running your enterprise workloads
- Bring AI to the platform, security, and observability teams already operate

---

## Advantage 3: Existing Investments

Your Spring Boot applications, your testing frameworks, your build tools, your deployment infrastructure: **they're all JVM**.

Embabel **plugs directly into** your existing IDE and toolchain.

```java
// Your existing Spring code
@Repository
public interface CodeAnalysisRepository extends ListCrudRepository<StoredCodeAnalysis, String> {
}

// Integrated into your agent
public CodingCommands(ChatClient chatClient, CodeAnalysisRepository codeAnalysisRepository) {
    this.chatClient = chatClient;
    this.codeAnalysisRepository = codeAnalysisRepository;
}
```

One language. One toolchain. **Java.**

---

## Advantage 4: Constraints That Matter

Production assistants need explicit, enforceable safety bounds. This action produces a
recommendation while write access and retries remain disabled:

```java
@Action(
        description = "Recommend one low-risk fix from the stored code analyses",
        readOnly = true,
        canRerun = false,
        actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
@AchievesGoal(description = "Produce a single approval-required, low-risk remediation plan")
public ConstrainedRemediationPlan recommendQuickWin(StoredCodeAnalyses stored) {
    // Select one issue; return a plan for human approval.
}
```

You saw this constraint hold in the `safe-quick-win` demo.
