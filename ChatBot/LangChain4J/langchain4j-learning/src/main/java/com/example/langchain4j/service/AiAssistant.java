package com.example.langchain4j.service;

import dev.langchain4j.service.SystemMessage;

public interface AiAssistant {

    @SystemMessage("""
        You are an experienced Java Technical Architect.
        Explain concepts clearly with practical Java examples.
        Keep answers suitable for an experienced Java developer.
        """)
    String chat(String message);
}