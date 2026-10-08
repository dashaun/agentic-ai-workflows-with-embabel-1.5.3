package com.embabel.demo.sre;

import com.embabel.demo.domain.IncidentResponseReport;
import com.embabel.demo.domain.RunbookAssessment;
import com.embabel.demo.domain.RunbookCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.typesafe.JsonContent;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.SystemOneResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Asks Jev, TypeSafe's System One decision model, whether the LLM stayed inside the runbook.
 *
 * <p>Jev is optional: without {@code spring.ai.typesafe.api-key} there is no client and the
 * check is skipped. When a configured judge fails, the check fails closed and escalates.
 */
@Component
public class RunbookChecker {

    static final String QUESTION = "within_runbook";

    private static final Logger log = LoggerFactory.getLogger(RunbookChecker.class);

    private static final Noul WITHIN_RUNBOOK = Noul.builder()
            .instructions("Does the recommendation stay inside the approved strategy?")
            .whenTrue("Every step in the recommendation is part of the approved strategy")
            .whenFalse("The recommendation adds steps the approved strategy does not allow")
            .build();

    private final ObjectProvider<TypeSafeClient> typeSafe;
    private final double threshold;

    public RunbookChecker(
            ObjectProvider<TypeSafeClient> typeSafe,
            @Value("${demo.runbook-check.threshold:0.7}") double threshold) {
        this.typeSafe = typeSafe;
        this.threshold = threshold;
    }

    public RunbookCheck check(RunbookAssessment assessment, IncidentResponseReport response) {
        TypeSafeClient client = typeSafe.getIfAvailable();
        if (client == null) {
            return new RunbookCheck("not configured: set TYPESAFE_API_KEY to enable Jev", null, false);
        }
        try {
            SystemOneResponse answer = client.systemOne(SystemOneRequest.builder()
                    .state(JsonContent.object(
                            "approved_strategy", assessment.recommendedAction(),
                            "llm_recommendation", response.recommendation()))
                    .question(QUESTION, WITHIN_RUNBOOK)
                    .build());
            double withinRunbook = answer.noulValue(QUESTION);
            return new RunbookCheck(answer.model(), withinRunbook, withinRunbook < threshold);
        } catch (RuntimeException e) {
            log.warn("Jev runbook check failed; escalating: {}", e.getMessage());
            return new RunbookCheck("unavailable: " + e.getMessage(), null, true);
        }
    }
}
