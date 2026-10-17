<!-- .slide: data-background-color="#6db33f" -->

# From Hello World to Production

The LLM call is the easy part. The workflow is the hard part.

Notes:
- Let's start where everyone starts.
- Everyone's done hello world: prompt in, response out.
- Tonight is what comes after. The hard part isn't the LLM call. It's the workflow around it.

---

## The Demo We All Know

Everyone's seen this:

```java
@Component
public class EmbabelCommands {
    private final ChatClient chatClient;   // constructor-injected

    @Command(description = "Tell a dad joke about a topic")
    public String dadJoke(@Option(longName = "about") String topic) {
        return chatClient.prompt()
            .user("Tell me a funny dad joke about " + topic)
            .call()
            .content();
    }
}
```

It works. Beautiful. Demo-worthy. **Spring AI 2.0. Spring Boot 4.1. Spring Shell 4.0.**

**Production adds the workflow.**

Notes:
- Hands up: who's built this? Spring AI? Something else on the JVM?
- Look how little it takes: a ChatClient, a prompt, a String back.
- It works. It demos great. Your boss loves it.
- But it answers a question. It doesn't do a job.
- Production doesn't care about the demo. It cares about the workflow.

---

<!-- .slide: data-beat="1" data-takeaway="Prompt in, string out. It works. It isn't a workflow." -->

## Live: Hello World

```text
embabel> dad-joke --about java
Why did the Java developer go to the doctor?
Because he had a bug!
```

It works. Beautiful. **It answers a question. It doesn't do a job.**

Notes:
- Let's prove it. One ChatClient call, live.
- (Click to run. If the terminal isn't showing, this slide has the rehearsed answer.)
- Ha. It works. Now: what happens when we ask it to do a job?

---

## The Production Reality

Agentic AI introduces **digital workers** that take actions:

- **Agent loops**: How do you stop a worker from looping infinitely?
- **Tool coordination**: Connect several tools through typed actions
- **Domain models**: Where do your Java domain models and constraints fit in?
- **Observability**: How do you explain why the worker took that action?

> The LLM call is one step inside the workflow.
> Embabel brings **discipline to agentic AI**.

Notes:
- The moment the model takes actions instead of just talking, you inherit three questions:
- One: how do you stop it looping forever?
- Two: how do you coordinate tools without a mess of if-else?
- Three: how do you keep your Java domain models at the heart of its reasoning?
- And a fourth one your ops team asks: why did it do that?
- Embabel's answer is discipline. Hold that word. We'll come back to it.
