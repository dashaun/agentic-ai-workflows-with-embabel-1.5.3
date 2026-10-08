package com.embabel.demo.sre;

import com.embabel.demo.domain.IncidentResponseReport;
import com.embabel.demo.domain.RunbookAssessment;
import com.embabel.demo.domain.RunbookCheck;
import org.junit.jupiter.api.Test;
import org.springaicommunity.typesafe.TypeSafeClient;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.SystemOneResponse;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RunbookCheckerTest {

    private final RunbookAssessment assessment = new RunbookAssessment(
            "Heap pressure is consistent with an OutOfMemory failure.",
            List.of(),
            "Capture a heap dump, roll back the latest risky change, then validate with a canary.",
            true);

    private final IncidentResponseReport response = new IncidentResponseReport(
            "Heap usage is at 95%.",
            "The service is out of heap space.",
            "Restart every pod in production and double the heap on all nodes.");

    @Test
    void skipsTheCheckWhenJevIsNotConfigured() {
        RunbookCheck check = checkerWith(null).check(assessment, response);

        assertThat(check.withinRunbook()).isNull();
        assertThat(check.escalate()).isFalse();
        assertThat(check.judge()).startsWith("not configured");
    }

    @Test
    void escalatesWhenJevSaysTheRecommendationLeftTheRunbook() {
        RunbookCheck check = checkerWith(jevAnswering(0.03)).check(assessment, response);

        assertThat(check.judge()).isEqualTo("jev-latest");
        assertThat(check.withinRunbook()).isEqualTo(0.03);
        assertThat(check.escalate()).isTrue();
    }

    @Test
    void passesWhenJevSaysTheRecommendationStayedInside() {
        RunbookCheck check = checkerWith(jevAnswering(0.99)).check(assessment, response);

        assertThat(check.escalate()).isFalse();
    }

    @Test
    void failsClosedWhenJevIsUnavailable() {
        TypeSafeClient client = mock(TypeSafeClient.class);
        when(client.systemOne(any(SystemOneRequest.class))).thenThrow(new IllegalStateException("timeout"));

        RunbookCheck check = checkerWith(client).check(assessment, response);

        assertThat(check.withinRunbook()).isNull();
        assertThat(check.escalate()).isTrue();
    }

    private static TypeSafeClient jevAnswering(double withinRunbook) {
        SystemOneResponse answer = mock(SystemOneResponse.class);
        when(answer.model()).thenReturn("jev-latest");
        when(answer.noulValue(RunbookChecker.QUESTION)).thenReturn(withinRunbook);
        TypeSafeClient client = mock(TypeSafeClient.class);
        when(client.systemOne(any(SystemOneRequest.class))).thenReturn(answer);
        return client;
    }

    @SuppressWarnings("unchecked")
    private static RunbookChecker checkerWith(TypeSafeClient client) {
        ObjectProvider<TypeSafeClient> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(client);
        return new RunbookChecker(provider, 0.7);
    }
}
