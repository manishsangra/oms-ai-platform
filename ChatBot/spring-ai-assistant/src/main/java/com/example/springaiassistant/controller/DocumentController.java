package com.example.springaiassistant.controller;

import com.example.springaiassistant.service.DocumentIngestionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        int chunks = ingestionService.ingestDocument(file);

        return Map.of(
                "message", "Document uploaded and processed successfully",
                "filename", file.getOriginalFilename(),
                "totalChunks", chunks
        );
    }
}