package com.embabel.demo.sre;

import com.embabel.demo.domain.DockerComposeStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class DockerComposeService {

    private static final int COMMAND_TIMEOUT_SECONDS = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<DockerComposeStatus> statuses() {
        try {
            List<DockerComposeStatus> existing = parseStatuses(
                    run("ps", "--all", "--format", "json"));
            List<String> configured = run("config", "--services").lines()
                    .filter(service -> !service.isBlank())
                    .toList();
            return includeUncreatedServices(existing, configured);
        } catch (IOException e) {
            throw new IllegalStateException("Docker is not available: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Docker Compose status check was interrupted", e);
        }
    }

    private String run(String... arguments) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>(List.of("docker", "compose"));
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        if (!process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("Docker Compose command timed out");
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (process.exitValue() != 0) {
            throw new IllegalStateException("Docker Compose command failed: " + output);
        }
        return output;
    }

    private List<DockerComposeStatus> parseStatuses(String output) throws IOException {
        if (output.isBlank()) {
            return List.of();
        }

        JsonNode json = objectMapper.readTree(output);
        List<DockerComposeStatus> statuses = new ArrayList<>();
        if (json.isArray()) {
            json.forEach(node -> statuses.add(toStatus(node)));
        } else {
            statuses.add(toStatus(json));
        }
        return statuses;
    }

    private List<DockerComposeStatus> includeUncreatedServices(
            List<DockerComposeStatus> statuses,
            List<String> configuredServices) {
        Map<String, DockerComposeStatus> byService = new LinkedHashMap<>();
        configuredServices.forEach(service -> byService.put(service,
                new DockerComposeStatus(service, "", "", service, "", "not created", "")));
        statuses.forEach(status -> byService.put(status.service(), status));
        return new ArrayList<>(byService.values());
    }

    private DockerComposeStatus toStatus(JsonNode node) {
        return new DockerComposeStatus(
                node.path("Name").asText(),
                node.path("Image").asText(),
                node.path("Command").asText(),
                node.path("Service").asText(),
                firstText(node, "CreatedAt", "Created"),
                firstText(node, "Status", "State"),
                ports(node));
    }

    private String firstText(JsonNode node, String first, String second) {
        String value = node.path(first).asText();
        return value.isBlank() ? node.path(second).asText() : value;
    }

    private String ports(JsonNode node) {
        JsonNode ports = node.path("Ports");
        if (!ports.isMissingNode()) {
            return ports.isTextual() ? ports.asText() : ports.toString();
        }
        JsonNode publishers = node.path("Publishers");
        return publishers.isMissingNode() ? "" : publishers.toString();
    }
}
