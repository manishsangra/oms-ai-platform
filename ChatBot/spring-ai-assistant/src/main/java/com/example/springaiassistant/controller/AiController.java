
package com.example.springaiassistant.controller;

import com.example.springaiassistant.dto.ChatRequest;
import com.example.springaiassistant.dto.ChatResponse;
import com.example.springaiassistant.dto.WeatherSummary;
import com.example.springaiassistant.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String conversationId = request.conversationId();

        if (conversationId == null || conversationId.isBlank()) {
            conversationId = UUID.randomUUID().toString();
        }

        String response = aiService.askAi(
                conversationId,
                request.message()
        );

        return new ChatResponse(conversationId, response);
    }

    @GetMapping("/structured-weather")
    public WeatherSummary structuredWeather(
            @RequestParam String city,
            @RequestParam String conversationId) {

        return aiService.getStructuredWeatherSummary(
                conversationId,
                city
        );
    }
}
