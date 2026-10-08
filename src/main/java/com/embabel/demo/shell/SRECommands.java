package com.embabel.demo.shell;

import com.embabel.demo.agent.AgentRunner;
import com.embabel.demo.domain.IncidentRequest;
import com.embabel.demo.domain.IncidentWorkflowReport;
import com.embabel.demo.domain.PlanExecution;
import com.embabel.demo.domain.PrettyJson;
import com.embabel.demo.sre.DockerComposeService;
import com.embabel.demo.sre.SreIncidentWorker;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class SRECommands {

    private final DockerComposeService dockerComposeService;
    private final AgentRunner agentRunner;

    public SRECommands(
            DockerComposeService dockerComposeService,
            AgentRunner agentRunner) {
        this.dockerComposeService = dockerComposeService;
        this.agentRunner = agentRunner;
    }

    @Command(description = "Resolve an OutOfMemory incident with the bounded SRE worker")
    public PlanExecution<IncidentWorkflowReport> oomIncident(
            @Option(longName = "budget", description = "Maximum actions the agent may run", defaultValue = "8")
            int budget) {
        return pursue(IncidentRequest.outOfMemory(), budget);
    }

    @Command(description = "Resolve an OutOfMemory incident whose logs contain a prompt injection")
    public PlanExecution<IncidentWorkflowReport> injectedIncident(
            @Option(longName = "budget", description = "Maximum actions the agent may run", defaultValue = "8")
            int budget) {
        return pursue(IncidentRequest.injectedOutOfMemory(), budget);
    }

    @Command(description = "Resolve a high-latency incident with the bounded SRE worker")
    public PlanExecution<IncidentWorkflowReport> latencyIncident(
            @Option(longName = "budget", description = "Maximum actions the agent may run", defaultValue = "8")
            int budget) {
        return pursue(IncidentRequest.highLatency(), budget);
    }

    @Command(description = "Show every Docker Compose service, including stopped and uncreated services")
    public String dockerComposeStatus() {
        return PrettyJson.format(dockerComposeService.statuses());
    }

    private PlanExecution<IncidentWorkflowReport> pursue(IncidentRequest request, int budget) {
        return agentRunner.run(SreIncidentWorker.NAME, request, IncidentWorkflowReport.class, budget);
    }
}
