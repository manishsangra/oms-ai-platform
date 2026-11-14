package com.example.langchain4j.config;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Bean
    public ChatModel chatModel(
            @Value("${app.gemini.api-key}") String apiKey,
            @Value("${app.gemini.model}") String model) {

        return GoogleGenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .maxRetries(2)
                .build();
    }
}