
package com.example.springaiassistant.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Service
public class DocumentIngestionService {

    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingestDocument(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please upload a valid document."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new IllegalArgumentException(
                    "Document filename is missing."
            );
        }

        String lowerCaseFilename =
                filename.toLowerCase(Locale.ROOT);

        if (!(lowerCaseFilename.endsWith(".pdf")
                || lowerCaseFilename.endsWith(".docx")
                || lowerCaseFilename.endsWith(".txt"))) {

            throw new IllegalArgumentException(
                    "Only PDF, DOCX and TXT files are supported."
            );
        }

        // Step 1: Read the uploaded document
        ByteArrayResource resource =
                new ByteArrayResource(file.getBytes()) {

                    @Override
                    public String getFilename() {
                        return filename;
                    }
                };

        TikaDocumentReader reader =
                new TikaDocumentReader(resource);

        List<Document> documents = reader.read();

        // Step 2: Split the document into chunks
        TokenTextSplitter splitter =
                TokenTextSplitter.builder()
                        .withChunkSize(800)
                        .withMinChunkSizeChars(350)
                        .withMinChunkLengthToEmbed(5)
                        .withMaxNumChunks(10000)
                        .withKeepSeparator(true)
                        .build();

        List<Document> chunks =
                splitter.apply(documents);

        // Step 3: Store chunks and generate embeddings
        vectorStore.add(chunks);

        System.out.println("Document: " + filename);
        System.out.println("Total chunks: " + chunks.size());

        return chunks.size();
    }
}
