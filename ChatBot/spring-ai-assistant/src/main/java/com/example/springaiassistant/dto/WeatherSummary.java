
package com.example.springaiassistant.dto;

public record WeatherSummary(
        String city,
        double temperature,
        int humidity,
        String condition,
        String recommendation
) {
}
