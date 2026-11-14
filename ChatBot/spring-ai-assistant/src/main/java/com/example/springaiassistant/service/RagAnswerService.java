
package com.example.springaiassistant.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagAnswerService {

    private final DocumentSearchService documentSearchService;
    private final ChatClient chatClient;

    public RagAnswerService(
            DocumentSearchService documentSearchService,
            ChatClient.Builder chatClientBuilder) {

        this.documentSearchService = documentSearchService;
        this.chatClient = chatClientBuilder.build();
    }

    public String answerQuestion(String question) {

        // 1. Retrieve relevant document chunks from PGVector
        List<Document> documents =
                documentSearchService.search(question, 5);

        if (documents.isEmpty()) {
            return "I couldn't find this information in the uploaded document.";
        }

        // 2. Combine retrieved chunks into context
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        // 3. Ask Gemini to generate a concise answer
        return chatClient.prompt()
                .system("""
                        You are a concise document-based assistant.

                        Answer the user's question using ONLY the supplied
                        document context.

                        Rules:
                        1. Answer only what the user has asked.
                        2. Do not include unrelated information.
                        3. Keep answers concise, preferably 1-3 sentences.
                        4. Do not list skills, education, projects or other
                           details unless explicitly requested.
                        5. Never invent information.
                        6. If the answer is unavailable in the context,
                           say:
                           "I couldn't find this information in the uploaded document."
                        """)
                .user(user -> user.text("""
                        Document context:
                        {context}

                        User question:
                        {question}
                        """)
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();
    }
}
