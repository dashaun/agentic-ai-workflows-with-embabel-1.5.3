package com.embabel.demo.domain;

/**
 * System One verdict on a System Two recommendation.
 *
 * @param judge          the decision model that answered, or why none did
 * @param withinRunbook  calibrated probability that the recommendation stays inside the
 *                       approved strategy; {@code null} when no judge answered
 * @param escalate       {@code true} when a human must review before anything else happens
 */
public record RunbookCheck(String judge, Double withinRunbook, boolean escalate) {

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
