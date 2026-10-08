package com.embabel.demo.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Shared shell rendering for structured command results. */
public final class PrettyJson {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private PrettyJson() {
    }

    public static String format(Object value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to render JSON output", e);
        }
    }

    public static String formatIfJson(String value) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(OBJECT_MAPPER.readTree(value));
        } catch (JsonProcessingException e) {
            return value;
        }
    }
}
