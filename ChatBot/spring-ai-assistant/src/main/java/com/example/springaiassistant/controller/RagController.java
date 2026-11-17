/*

package com.example.springaiassistant.controller;

import com.example.springaiassistant.dto.DocumentQuestionRequest;
import com.example.springaiassistant.service.RagAnswerService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class RagController {

    private final RagAnswerService ragAnswerService;

    public RagController(RagAnswerService ragAnswerService) {
        this.ragAnswerService = ragAnswerService;
    }

    @PostMapping("/ask")
    public Map<String, String> askQuestion(
            @RequestBody DocumentQuestionRequest request) {

        String answer =
                ragAnswerService.answerQuestion(request.question());

        return Map.of("answer", answer);
    }
}
*/
