package com.embabel.demo.sre;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.ActionRetryPolicy;
import com.embabel.demo.domain.DockerComposeStatus;
import com.embabel.demo.domain.IncidentRequest;
import com.embabel.demo.domain.IncidentResponseReport;
import com.embabel.demo.domain.IncidentWorkflowReport;
import com.embabel.demo.domain.RunbookAssessment;
import com.embabel.demo.domain.RunbookCheck;
import com.embabel.demo.domain.ServiceObservation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@Agent(
        name = SreIncidentWorker.NAME,
        description = "Produce a bounded, approval-required response to a production incident",
        actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
public class SreIncidentWorker {

    public static final String NAME = "sre-incident-worker";

    private final DockerComposeService dockerComposeService;
    private final SreRunbook runbook;
    private final RunbookChecker runbookChecker;

    public SreIncidentWorker(
            DockerComposeService dockerComposeService,
            SreRunbook runbook,
            RunbookChecker runbookChecker) {
        this.dockerComposeService = dockerComposeService;
        this.runbook = runbook;
        this.runbookChecker = runbookChecker;
    }

    @Action(description = "Observe every Docker Compose service", readOnly = true)
    public ServiceObservation observeServices(IncidentRequest request) {
        List<DockerComposeStatus> services = dockerComposeService.statuses();
        List<String> unavailable = services.stream()
                .filter(status -> !isRunning(status.status()))
                .map(DockerComposeStatus::service)
                .toList();
        return new ServiceObservation(request.incidentType(), services, unavailable);
    }

    @Action(description = "Apply the production runbook to the observation", readOnly = true)
    public RunbookAssessment applyRunbook(
            IncidentRequest request,
            ServiceObservation observation) {
        return runbook.assess(request, observation);
    }

    @Action(description = "Use the LLM to produce a typed incident response", readOnly = true)
    public IncidentResponseReport analyzeIncident(
            IncidentRequest request,
            ServiceObservation observation,
            RunbookAssessment assessment,
            OperationContext context) {
        try {
            IncidentResponseReport report = context.ai().withDefaultLlm()
                    .creating(IncidentResponseReport.class)
                    .withExample("A response that stays inside the approved strategy", new IncidentResponseReport(
                            "p99 latency is 5000ms and the logs show database timeouts.",
                            "The database is saturated.",
                            "Check database health, then scale out with approval."))
                    .fromPrompt("""
                    You are an SRE following a production runbook.

                    Incident: %s
                    Metrics: %s
                    Logs: %s
                    Unavailable services: %s
                    Runbook diagnosis: %s
                    Approved strategy: %s

                    Return a concise incident response with analysis, diagnosis, and recommendation.
                    Keep every recommendation inside the approved strategy.
                    """.formatted(
                    request.incidentType().displayName(),
                    request.metrics(),
                    request.logs(),
                    observation.unavailableServices(),
                    assessment.diagnosis(),
                    assessment.recommendedAction()));
            return report.withRunbookDefaults(assessment);
        } catch (RuntimeException ex) {
            return new IncidentResponseReport(
                    "The LLM was unavailable; the deterministic runbook result was retained.",
                    assessment.diagnosis(),
                    assessment.recommendedAction());
        }
    }

    @Action(description = "System One check: did the LLM stay inside the runbook?", readOnly = true)
    public RunbookCheck checkRecommendation(
            RunbookAssessment assessment,
            IncidentResponseReport response) {
        return runbookChecker.check(assessment, response);
    }

    @Action(
            description = "Create the approval-required incident report",
            readOnly = true,
            actionRetryPolicy = ActionRetryPolicy.FIRE_ONCE)
    @AchievesGoal(description = "A bounded incident response is ready for human approval")
    public IncidentWorkflowReport prepareReport(
            IncidentRequest request,
            ServiceObservation observation,
            RunbookAssessment assessment,
            IncidentResponseReport response,
            RunbookCheck check) {
        return new IncidentWorkflowReport(
                "Produce a safe incident response",
                request.incidentType(),
                observation,
                assessment,
                response,
                check,
                assessment.requiresApproval());
    }

    private boolean isRunning(String status) {
        String normalized = status.toLowerCase(Locale.ROOT);
        return normalized.contains("running") || normalized.startsWith("up");
    }
}
