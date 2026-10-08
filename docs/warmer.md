## The Problem We All Know

Everyone's seen this:

```java
@Component
public class EmbabelCommands {
    private final ChatClient chatClient;

    public EmbabelCommands(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @Command(description = "Tell a dad joke about a topic")
    public String dadJoke(
            @Option(longName = "about", description = "Topic for the joke", defaultValue = "dogs") String topic) {
        return "😄 " + chatClient.prompt()
                .user("Tell me a funny dad joke about " + topic +
                        ". Keep it short and cheesy.")
                .call()
                .content();
    }
}
```

✨ It works. Beautiful. Demo-worthy. **Spring AI 2.0. Spring Boot 4.1. Spring Shell 4.0.**

**Production adds the workflow.**

Notes:
- How many of you have done this example?
- With Spring AI?
- With something else in Java?
---

## The Production Reality

Agentic AI introduces **digital workers** that take actions:

- 🔄 **Agent loops**: How do you stop a worker from looping infinitely?
- 🧩 **Tool coordination**: Connect several tools through typed actions
- 🏛️ **Domain models**: Where do your Java domain models and constraints fit in?
- 📊 **Observability**: How do you explain why the worker took that action?

> The LLM call is one step inside the workflow.
> Embabel brings **discipline to agentic AI**.

Notes:
- Chatbots, RAG, MCP, now agents
- We need controlled loops
- Smart tool use
- Easy integration with existing domains
- Observability
