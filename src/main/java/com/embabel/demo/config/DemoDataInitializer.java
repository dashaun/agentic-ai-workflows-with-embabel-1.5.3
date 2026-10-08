package com.embabel.demo.config;

import java.util.List;

import jakarta.annotation.PostConstruct;

import com.embabel.demo.domain.CodeAnalysis;
import com.embabel.demo.domain.CodeAnalysisRepository;
import com.embabel.demo.domain.StoredCodeAnalysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final CodeAnalysisRepository repository;

    public DemoDataInitializer(CodeAnalysisRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void seedCodeAnalyses() {
        try {
            repository.save(new StoredCodeAnalysis("sample:payment-service", new CodeAnalysis(
                    "Payment service review: validation and observability need attention.",
                    List.of("Missing validation before calling the payment gateway", "No timeout metric for gateway calls"),
                    List.of("Validate requests at the API boundary", "Publish a payment gateway latency metric"),
                    72)));
            repository.save(new StoredCodeAnalysis("sample:checkout-controller", new CodeAnalysis(
                    "Checkout controller is concise but has incomplete error handling.",
                    List.of("Gateway exceptions are returned as generic 500 errors"),
                    List.of("Map gateway failures to an explicit API error", "Add an integration test for declined cards"),
                    81)));
            repository.save(new StoredCodeAnalysis("sample:inventory-client", new CodeAnalysis(
                    "Inventory client is production-ready with a small retry opportunity.",
                    List.of(),
                    List.of("Add bounded retries for transient inventory failures"),
                    91)));
            log.info("Seeded sample code analyses in Redis");
        } catch (RuntimeException e) {
            log.warn("Redis is unavailable; sample analyses were not seeded: {}", e.getMessage());
        }
    }
}
