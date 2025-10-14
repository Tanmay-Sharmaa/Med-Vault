package com.medvault.controller;

import com.medvault.model.MedicalRecord;
import com.medvault.repository.MedicalRecordRepository;
import com.medvault.service.AIService;
import com.medvault.service.RecordStorageService;
import com.medvault.service.TikaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;
    private final MedicalRecordRepository recordRepo;
    private final TikaService tikaService;
    private final RecordStorageService storageService;

    @GetMapping("/summarize/{recordId}")
    public ResponseEntity<String> summarizeRecord(@PathVariable Long recordId) {
        return recordRepo.findById(recordId)
                .map(record -> {
                    try {
                        // ✅ Step 1: Decrypt encrypted .bin into a temporary readable file
                        File decryptedFile = storageService.decryptToTempFile(record);

                        // ✅ Step 2: Extract text using Tika
                        String extractedText = tikaService.extractText(decryptedFile.toPath());

                        // ✅ Step 3: Summarize the extracted text
                        String summary = aiService.summarizeText(extractedText);

                        // ✅ Step 4: Clean up temporary file
                        decryptedFile.delete();

                        return ResponseEntity.ok(summary);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.internalServerError()
                                .body("❌ Error reading or summarizing the file: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.badRequest().body("❌ Record not found."));
    }
}
