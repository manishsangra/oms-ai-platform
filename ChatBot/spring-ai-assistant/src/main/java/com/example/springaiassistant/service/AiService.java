
package com.example.springaiassistant.service;

import com.example.springaiassistant.dto.WeatherSummary;
import com.example.springaiassistant.tools.CalculatorTools;

import com.example.springaiassistant.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final CalculatorTools calculatorTools;
    private final WeatherTools weatherTools;
    private final SyncMcpToolCallbackProvider mcpTools;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            CalculatorTools calculatorTools,
            WeatherTools weatherTools,
            SyncMcpToolCallbackProvider mcpTools) {

        this.calculatorTools = calculatorTools;
        this.weatherTools = weatherTools;
        this.mcpTools = mcpTools;

        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(mcpTools)
                .build();
    }

    public String askAi(String conversationId, String message) {

        return chatClient.prompt()
                .user(message)
                .tools(calculatorTools, weatherTools)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId
                ))
                .call()
                .content();
    }


    public WeatherSummary getStructuredWeatherSummary(
            String conversationId,
            String city) {

        return chatClient.prompt()
                .system("""
                You are a weather assistant.
                Provide a concise weather summary.
                Use the available weather tool to retrieve
                actual current weather information.
                Based on the weather data, provide a
                practical recommendation.
                Never invent weather measurements.
                """)
                .user("Get the current weather summary for " + city)
                .tools(weatherTools)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId
                ))
                .call()
                .entity(WeatherSummary.class);
    }

}
