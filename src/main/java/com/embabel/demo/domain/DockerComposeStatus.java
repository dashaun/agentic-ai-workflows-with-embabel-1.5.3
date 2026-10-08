package com.embabel.demo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One service row returned by {@code docker compose ps --format json}. */
public record DockerComposeStatus(
        @JsonProperty("NAME") String name,
        @JsonProperty("IMAGE") String image,
        @JsonProperty("COMMAND") String command,
        @JsonProperty("SERVICE") String service,
        @JsonProperty("CREATED") String created,
        @JsonProperty("STATUS") String status,
        @JsonProperty("PORTS") String ports) {

    @Override
    public String toString() {
        return PrettyJson.format(this);
    }
}
