package com.embabel.demo.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("code-analysis-v2")
public record StoredCodeAnalysis(
        @Id String sourceId,
        CodeAnalysis analysis
) {
}
