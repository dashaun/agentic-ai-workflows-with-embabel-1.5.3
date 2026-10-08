package com.embabel.demo.domain;

import java.util.List;

public record PlanExecution<T>(
        String agent,
        String goalType,
        String status,
        int actionBudget,
        List<String> actions,
        T result) {

    public PlanExecution {
        actions = List.copyOf(actions);
    }

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
