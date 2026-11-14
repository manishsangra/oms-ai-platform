package com.example.langchain4j.config;

import com.example.langchain4j.service.AiAssistant;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiServiceConfig {

    @Bean
    public AiAssistant aiAssistant(ChatModel chatModel) {

        return AiServices.builder(AiAssistant.class)
                .chatModel(chatModel)
                .build();
    }
}