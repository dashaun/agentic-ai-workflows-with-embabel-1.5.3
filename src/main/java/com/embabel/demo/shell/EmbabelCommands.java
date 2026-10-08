package com.embabel.demo.shell;

import org.springframework.stereotype.Component;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.ai.chat.client.ChatClient;

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

    @Command(description = "Show Embabel framework concepts")
    public String embabel() {
        return """
                🤖 Embabel - Agentic AI on the JVM
                %s

                Core Concepts:

                @Agent - Defines an agentic component
                  Embabel discovers available agents and their capabilities

                @Action - Executable step toward a goal
                  Each action transforms input to output (type-safe)
                  Can invoke ChatClient or an existing Spring service

                @AchievesGoal - Marks action as goal-completion
                  The planner stops when goal is achieved

                GOAP - Goal-Oriented Action Planning
                  Embabel planner chains actions to achieve goals

                DICE - Domain-Integrated Context Engineering
                  Grounds model reasoning in typed requests, live observations,
                  and operational runbooks supplied by your application
                """.formatted("=".repeat(50));
    }

    @Command(description = "Show OODA loop for disciplined decision making")
    public String ooda() {
        return """
                🔄 OODA Loop - Observe, Orient, Decide, Act
                %s

                OBSERVE: Gather facts from the environment
                  • Read metrics, logs, code
                  • Current state of system/codebase

                ORIENT: Make sense of observations
                  • Apply domain knowledge
                  • Match patterns to known scenarios
                  • Consult team standards & runbooks

                DECIDE: Choose course of action
                  • Select best action for goal
                  • Apply constraints (domain rules)
                  • Plan multi-step workflows

                ACT: Execute the decision
                  • Run refactoring, deploy, etc.
                  • Monitor outcomes
                  • Return to OBSERVE (continuous loop)
                """.formatted("=".repeat(50));
    }

    @Command(description = "Show DICE framework")
    public String dice() {
        return """
                🎯 DICE - Domain-Integrated Context Engineering
                %s

                Domain context in this demo:
                  • IncidentRequest: typed incident, metrics, and logs
                  • ServiceObservation: live Docker Compose state
                  • RunbookAssessment: deterministic team policy

                The LLM reasons inside that context and returns an
                IncidentResponseReport. It does not invent the runbook.
                """.formatted("=".repeat(50));
    }

    @Command(description = "Show available commands")
    public String help() {
        return """
                📚 Embabel Shell Demo - Available Commands
                %s

                Coding Assistant (AI Code Review via Ollama):
                  analyze-code --file <path>       Analyze Java file with LLM
                  analyze-pattern --pattern <p>    Analyze: loop, null, exception
                  stored-analyses                  Show analyses stored in Redis
                  quick-win                        Show the easiest stored file to fix
                  safe-quick-win                   Recommend one constrained, approval-required fix
                  summarize-open-issues            Summarize open issues from Redis
                  list-files                       Show Java files in project

                SRE Incident Response (OODA Loop):
                  oom-incident [--budget <n>]      Run the bounded OutOfMemory worker
                  latency-incident [--budget <n>]  Run the bounded latency worker
                  docker-compose-status            Show Docker Compose service status

                Demo & Fun:
                  dad-joke --about <topic>         Tell a dad joke about a topic

                Framework Concepts:
                  embabel                          Embabel + Ollama architecture
                  ooda                             OODA loop explanation
                  dice                             DICE framework
                  help                             This message
                """.formatted("=".repeat(50));
    }
}
