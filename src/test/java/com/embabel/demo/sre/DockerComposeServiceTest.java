package com.embabel.demo.sre;

import com.embabel.demo.domain.DockerComposeStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DockerComposeServiceTest {

    private final DockerComposeService service = new DockerComposeService();

    @Test
    void parsesOneJsonObjectPerLine() throws IOException {
        List<DockerComposeStatus> statuses = service.parseStatuses("""
                {"Name":"workshop-ollama","Service":"ollama","State":"running"}
                {"Name":"workshop-redis","Service":"redis","State":"running"}
                """);

        assertThat(statuses).extracting(DockerComposeStatus::service).containsExactly("ollama", "redis");
    }

    @Test
    void parsesAJsonArray() throws IOException {
        List<DockerComposeStatus> statuses = service.parseStatuses("""
                [{"Name":"workshop-ollama","Service":"ollama","State":"running"},
                 {"Name":"workshop-redis","Service":"redis","State":"running"}]
                """);

        assertThat(statuses).extracting(DockerComposeStatus::service).containsExactly("ollama", "redis");
    }
}
