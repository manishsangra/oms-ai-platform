
package com.example.springaiassistant.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class WeatherTools {

    private final RestClient geocodingClient;
    private final RestClient weatherClient;

    public WeatherTools() {
        this.geocodingClient = RestClient.create(
                "https://geocoding-api.open-meteo.com"
        );

        this.weatherClient = RestClient.create(
                "https://api.open-meteo.com"
        );
    }


    @Tool(description = "Get the current weather for a city. Use this tool whenever the user asks about current weather, temperature, humidity or wind conditions in a city.")
    public String getCurrentWeather(
            @ToolParam(description = "Name of the city, for example Delhi, Mumbai or London")
            String city) {

        // 1. Get geographical coordinates
        Map<?, ?> locationResponse = geocodingClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("name", city)
                        .queryParam("count", 1)
                        .queryParam("language", "en")
                        .queryParam("format", "json")
                        .build())
                .retrieve()
                .body(Map.class);

        if (locationResponse == null
                || !(locationResponse.get("results") instanceof List<?> results)
                || results.isEmpty()) {
            return "Sorry, I could not find the city: " + city;
        }

        Map<?, ?> location = (Map<?, ?>) results.get(0);

        double latitude = ((Number) location.get("latitude")).doubleValue();
        double longitude = ((Number) location.get("longitude")).doubleValue();

        String cityName = String.valueOf(location.get("name"));
        String country = String.valueOf(location.get("country"));

        // 2. Fetch current weather
        Map<?, ?> weatherResponse = weatherClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/forecast")
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("current",
                                "temperature_2m,relative_humidity_2m,"
                                        + "apparent_temperature,weather_code,"
                                        + "wind_speed_10m")
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(Map.class);

        if (weatherResponse == null
                || !(weatherResponse.get("current") instanceof Map<?, ?> current)) {
            return "Weather information is currently unavailable.";
        }

        double temperature =
                ((Number) current.get("temperature_2m")).doubleValue();

        double feelsLike =
                ((Number) current.get("apparent_temperature")).doubleValue();

        int humidity =
                ((Number) current.get("relative_humidity_2m")).intValue();

        double windSpeed =
                ((Number) current.get("wind_speed_10m")).doubleValue();

        int weatherCode =
                ((Number) current.get("weather_code")).intValue();

        String condition = getWeatherDescription(weatherCode);

        // 3. Return weather information to AI
        return String.format("""
                        Current weather for %s, %s:
                        Temperature: %.1f °C
                        Feels like: %.1f °C
                        Condition: %s
                        Humidity: %d%%
                        Wind speed: %.1f km/h
                        """,
                cityName,
                country,
                temperature,
                feelsLike,
                condition,
                humidity,
                windSpeed
        );
    }

    private String getWeatherDescription(int code) {
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 61, 63, 65 -> "Rain";
            case 71, 73, 75 -> "Snowfall";
            case 80, 81, 82 -> "Rain showers";
            case 85, 86 -> "Snow showers";
            case 95, 96, 99 -> "Thunderstorm";
            default -> "Unknown weather condition";
        };
    }
}
