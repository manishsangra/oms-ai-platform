package com.example.langchain4j.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final AiAssistant aiAssistant;

    public ChatService(AiAssistant aiAssistant) {
        this.aiAssistant = aiAssistant;
    }

    public String ask(String message) {
        return aiAssistant.chat(message);
    }
}