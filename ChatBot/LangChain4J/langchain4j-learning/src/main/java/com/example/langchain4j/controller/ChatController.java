package com.example.langchain4j.controller;

import com.example.langchain4j.dto.ChatRequest;
import com.example.langchain4j.dto.ChatResponse;
import com.example.langchain4j.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String answer = chatService.ask(request.message());

        return new ChatResponse(answer);
    }
}