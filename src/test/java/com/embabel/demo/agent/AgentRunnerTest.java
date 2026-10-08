package com.embabel.demo.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.support.AgentMetadataReader;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.demo.domain.PlanExecution;
import org.junit.jupiter.api.Test;

import static com.embabel.agent.test.integration.IntegrationTestUtils.dummyAgentPlatform;
import static org.assertj.core.api.Assertions.assertThat;

class AgentRunnerTest {

    @Test
    void embabelPlansTheActionsFromTheirTypes() {
        AgentPlatform platform = dummyAgentPlatform();
        platform.deploy(new AgentMetadataReader().createAgentMetadata(new TestAgent()));

        PlanExecution<Report> execution = new AgentRunner(platform)
                .run("test-agent", new Request(), Report.class);

        assertThat(execution.status()).isEqualTo("COMPLETED");
        assertThat(execution.actionBudget()).isEqualTo(8);
        assertThat(execution.actions()).containsExactly("observe", "orient", "report");
        assertThat(execution.result().value()).isEqualTo("observed -> oriented -> complete");
    }

    @Test
    void theBudgetStopsTheProcessBeforeTheGoal() {
        PlanExecution<Report> execution = runnerFor(new TestAgent())
                .run("test-agent", new Request(), Report.class, 2);

        assertThat(execution.status()).isEqualTo("TERMINATED");
        assertThat(execution.actions()).containsExactly("observe", "orient");
        assertThat(execution.result()).isNull();
    }

    @Test
    void aGoalWithNoPathEndsStuckInsteadOfLooping() {
        PlanExecution<Report> execution = runnerFor(new StuckAgent())
                .run("stuck-agent", new Request(), Report.class);

        assertThat(execution.status()).isEqualTo("STUCK");
        assertThat(execution.result()).isNull();
    }

    private static AgentRunner runnerFor(Object agent) {
        AgentPlatform platform = dummyAgentPlatform();
        platform.deploy(new AgentMetadataReader().createAgentMetadata(agent));
        return new AgentRunner(platform);
    }

    @Agent(name = "test-agent", description = "Observe, orient, and report")
    static class TestAgent {

        @Action
        public Observation observe(Request request) {
            return new Observation("observed");
        }

        @Action
        public Assessment orient(Request request, Observation observation) {
            return new Assessment(observation.value() + " -> oriented");
        }

        @Action
        @AchievesGoal(description = "The report is complete")
        public Report report(Request request, Observation observation, Assessment assessment) {
            return new Report(assessment.value() + " -> complete");
        }
    }

    /** The goal needs an Approval, but no action can produce one. */
    @Agent(name = "stuck-agent", description = "Report without any way to obtain approval")
    static class StuckAgent {

        @Action
        public Observation observe(Request request) {
            return new Observation("observed");
        }

        @Action
        @AchievesGoal(description = "The approved report is complete")
        public Report report(Observation observation, Approval approval) {
            return new Report("unreachable");
        }
    }

    record Approval() {
    }

    record Request() {
    }

    record Observation(String value) {
    }

    record Assessment(String value) {
    }

    record Report(String value) {
    }
}
