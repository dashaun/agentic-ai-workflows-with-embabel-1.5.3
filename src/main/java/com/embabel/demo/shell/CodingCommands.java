package com.embabel.demo.shell;

import org.springframework.stereotype.Component;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.ai.chat.client.ChatClient;
import com.embabel.demo.agent.AgentRunner;
import com.embabel.demo.domain.CodeAnalysis;
import com.embabel.demo.domain.CodeAnalysisRepository;
import com.embabel.demo.domain.CodeReviewRequest;
import com.embabel.demo.domain.ConstrainedRemediationPlan;
import com.embabel.demo.domain.OpenIssuesSummary;
import com.embabel.demo.domain.PlanExecution;
import com.embabel.demo.domain.PrettyJson;
import com.embabel.demo.domain.StoredCodeAnalyses;
import com.embabel.demo.domain.StoredCodeAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class CodingCommands {

    private final ChatClient chatClient;
    private final CodeAnalysisRepository codeAnalysisRepository;
    private final AgentRunner agentRunner;

    public CodingCommands(
            ChatClient chatClient,
            CodeAnalysisRepository codeAnalysisRepository,
            AgentRunner agentRunner) {
        this.chatClient = chatClient;
        this.codeAnalysisRepository = codeAnalysisRepository;
        this.agentRunner = agentRunner;
    }

    @Command(description = "Analyze Java code in a file")
    public CodeAnalysis analyzeCode(
            @Option(
                    longName = "file",
                    description = "Path to Java file to analyze",
                    defaultValue = "src/main/java/com/embabel/demo/shell/EmbabelCommands.java")
            String filePath) {
        try {
            String sourceCode = Files.readString(Paths.get(filePath));
            CodeReviewRequest request = CodeReviewRequest.of(sourceCode);

            CodeAnalysis analysis = chatClient.prompt().user(reviewPrompt(request, sourceCode))
                    .call()
                    .entity(CodeAnalysis.class);
            return codeAnalysisRepository.save(new StoredCodeAnalysis(filePath, analysis)).analysis();
        } catch (IOException e) {
            return failedAnalysis("Unable to read file: " + e.getMessage());
        }
    }

    private String reviewPrompt(CodeReviewRequest request, String sourceCode) {
        return """
                    Analyze this Java code for quality, bugs, and improvements.

                    Context: %s

                    ```java
                    %s
                    ```

                    Respond with a JSON object with these fields:
                    - summary: brief overview
                    - issues: array of issues
                    - suggestions: array of suggestions
                    - qualityScore: 0-100
                    """.formatted(request.context(), sourceCode);
    }

    @Command(description = "List Java files in current directory")
    public String listFiles() {
        try (var paths = Files.walk(Paths.get("."))) {
            String files = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(path -> "  " + path)
                    .collect(Collectors.joining("\n"));

            return """
                    📁 Java Files in Current Directory:
                    %s
                    %s
                    """.formatted("=".repeat(50), files);
        } catch (IOException e) {
            return "❌ Error listing files: " + e.getMessage();
        }
    }

    @Command(description = "Show code analyses stored in Redis")
    public String storedAnalyses() {
        List<CodeAnalysis> analyses = codeAnalysisRepository.findAll().stream()
                .map(StoredCodeAnalysis::analysis)
                .toList();
        return PrettyJson.format(analyses);
    }

    @Command(description = "Find the stored file with the quickest issue to fix")
    public String quickWin() {
        return codeAnalysisRepository.findAll().stream()
                .filter(entry -> !entry.analysis().issues().isEmpty())
                .min(Comparator
                        .comparingInt((StoredCodeAnalysis entry) -> entry.analysis().issues().size())
                        .thenComparing(entry -> entry.analysis().qualityScore(), Comparator.reverseOrder()))
                .map(StoredCodeAnalysis::sourceId)
                .orElse("No stored analyses have open issues to fix.");
    }

    @Command(description = "Recommend a safe, approval-required quick win using Embabel action constraints")
    public PlanExecution<ConstrainedRemediationPlan> safeQuickWin() {
        return agentRunner.run(
                SafeRemediationAgent.NAME,
                new StoredCodeAnalyses(codeAnalysisRepository.findAll()),
                ConstrainedRemediationPlan.class);
    }

    @Command(name = "summarize-open-issues", description = "Summarize the open issues")
    public OpenIssuesSummary summarizOpenIssues() {
        String openIssues = codeAnalysisRepository.findAll().stream()
                .flatMap(entry -> entry.analysis().issues().stream()
                        .map(issue -> "- " + entry.sourceId() + ": " + issue))
                .collect(Collectors.joining("\n"));

        if (openIssues.isBlank()) {
            return new OpenIssuesSummary(List.of(), "No quick win identified.", "No open issues are stored in Redis.");
        }

        return chatClient.prompt().user("""
                Summarize these open code issues for an engineering team.
                Return JSON with exactly these fields:
                - issueTypes: a list of recurring issue categories
                - quickWin: the single quickest issue to fix and its file
                - conciseSummary: a concise prioritized summary

                %s
                """.formatted(openIssues))
                .call()
                .entity(OpenIssuesSummary.class);
    }

    @Command(description = "Analyze a specific code pattern")
    public CodeAnalysis analyzePattern(
            @Option(longName = "pattern", description = "Pattern: loop, null, exception") String pattern) {
        String filePath = switch(pattern.toLowerCase()) {
            case "loop" -> "src/main/java/com/embabel/demo/shell/CodingCommands.java";
            case "null" -> "src/main/java/com/embabel/demo/shell/EmbabelCommands.java";
            case "exception" -> "src/main/java/com/embabel/demo/shell/SRECommands.java";
            default -> null;
        };

        if (filePath == null) {
            return failedAnalysis("Unknown pattern: " + pattern);
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return analyzeCode(filePath);
            } else {
                return failedAnalysis("File not found: " + filePath);
            }
        } catch (Exception e) {
            return failedAnalysis("Error analyzing pattern: " + e.getMessage());
        }
    }

    private CodeAnalysis failedAnalysis(String message) {
        return new CodeAnalysis(message, List.of(), List.of(), 0);
    }
}
