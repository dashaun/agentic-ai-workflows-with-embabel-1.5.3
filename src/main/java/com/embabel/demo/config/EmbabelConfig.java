package com.embabel.demo.config;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.shell.jline.PromptProvider;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

@Configuration
public class EmbabelConfig {

    private static final Logger log = LoggerFactory.getLogger(EmbabelConfig.class);

    @Bean
    @ConditionalOnMissingBean
    public RestClient.Builder restClientBuilder(
            @Value("${embabel.ollama.request-timeout:300s}") Duration requestTimeout) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(requestTimeout);
        return RestClient.builder().requestFactory(requestFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public PromptProvider promptProvider(
            @Value("${spring.shell.prompt.text:embabel>}") String promptText,
            @Value("${spring.shell.prompt.color:GREEN}") String promptColor) {
        return () -> new AttributedString(promptText, promptStyle(promptColor));
    }

    private AttributedStyle promptStyle(String color) {
        return switch (color.trim().toUpperCase()) {
            case "BLACK" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.BLACK);
            case "RED" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.RED);
            case "GREEN" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
            case "YELLOW" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW);
            case "BLUE" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE);
            case "MAGENTA" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA);
            case "CYAN" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN);
            case "WHITE" -> AttributedStyle.DEFAULT.foreground(AttributedStyle.WHITE);
            default -> {
                log.warn("Unknown spring.shell.prompt.color '{}'; using GREEN", color);
                yield AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN);
            }
        };
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        try {
            ChatClient chatClient = event.getApplicationContext().getBean(ChatClient.class);
            if (chatClient != null) {
                log.info("✅ ChatClient bean initialized: {}", chatClient.getClass().getSimpleName());
            } else {
                log.warn("⚠️  ChatClient bean is null");
            }
        } catch (NoSuchBeanDefinitionException e) {
            log.warn("⚠️  No ChatClient bean - verify:");
            log.warn("   1. ollama serve is running at http://localhost:11434");
            log.warn("   2. Model is downloaded: ollama pull qwen2.5-coder:1.5b");
            log.warn("   3. Spring AI auto-configuration is enabled");
        } catch (Exception e) {
            log.error("Error accessing ChatClient: {}", e.getMessage());
        }
    }
}
