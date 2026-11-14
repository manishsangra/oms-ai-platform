package com.example.springaiassistant.dto;

import java.util.Map;

public record DocumentSearchResult(
        String id,
        String content,
        Map<String, Object> metadata
) {
}