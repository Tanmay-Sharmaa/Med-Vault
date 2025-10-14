package com.medvault.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class AIService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    private static final String MODEL_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";

    /**
     * Summarize a medical record into simple language for patients
     */
    public String summarizeText(String text) {
        try {
            if (text == null || text.isBlank()) {
                return "⚠️ The uploaded file seems empty or unreadable for summarization.";
            }

            // ✅ Prepare API request
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // ✅ Cleaned-up instruction (not echoed in output)
            Map<String, Object> payload = Collections.singletonMap("inputs",
                    "Provide a clear and patient-friendly summary of the following medical report:\n\n" + text);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // ✅ Send request to Hugging Face
            ResponseEntity<String> response = restTemplate.postForEntity(MODEL_URL, entity, String.class);

            // ✅ Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            // ⚠️ Handle model loading/unavailability
            if (root.has("error")) {
                return "⚠️ The AI model is still loading or temporarily unavailable. Please try again in a few seconds.";
            }

            // ✅ Extract summary text if available
            if (root.isArray() && root.size() > 0 && root.get(0).has("summary_text")) {
                String summary = root.get(0).get("summary_text").asText();

                // 🧹 Remove unwanted instruction echoes if present
                summary = summary.replaceAll("(?i)summarize this medical record.*", "").trim();
                summary = summary.replaceAll("(?i)provide a clear and patient-friendly summary.*", "").trim();

                return summary;
            }

            return "⚠️ No summary could be generated. Try again later.";

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Unable to summarize this record at the moment. Please try again later.\n(" + e.getMessage() + ")";
        }
    }
}
