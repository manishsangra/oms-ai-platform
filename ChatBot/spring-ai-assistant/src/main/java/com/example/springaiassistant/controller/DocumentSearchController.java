/*
package com.example.springaiassistant.controller;

import com.example.springaiassistant.dto.DocumentSearchResult;
import com.example.springaiassistant.service.DocumentSearchService;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentSearchController {

    private final DocumentSearchService searchService;

    public DocumentSearchController(DocumentSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public List<DocumentSearchResult> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {

        List<Document> documents =
                searchService.search(query, topK);

        return documents.stream()
                .map(document -> new DocumentSearchResult(
                        document.getId(),
                        document.getText(),
                        document.getMetadata()
                ))
                .toList();
    }
}*/
