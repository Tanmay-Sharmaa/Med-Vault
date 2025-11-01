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

    //  These two are hosted & free to use
    private static final String PRIMARY_MODEL_URL = "https://api-inference.huggingface.co/models/philschmid/bart-large-cnn-samsum";
    //private static final String PRIMARY_MODEL_URL = "https://api-inference.huggingface.co/models/google/flan-t5-large";
    private static final String FALLBACK_MODEL_URL = "https://api-inference.huggingface.co/models/sshleifer/distilbart-cnn-12-6";



    /**
     *  Summarize a medical record into simple and patient-friendly language
     */
    public String summarizeText(String text) {
        try {
            if (text == null || text.isBlank()) {
                return "⚠️ The uploaded file seems empty or unreadable for summarization.";
            }

            //  Step 1: Build RestTemplate and headers
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

             // Step 2: Use a guiding instruction (prompt) to improve summary quality
            String prompt = """
                    You are a helpful medical analysis assistant.
                    Read the following lab report text carefully and:
                    1. Write a short and clear summary of the patient’s test results.
                    2. For each major parameter (like Hemoglobin, WBC, Glucose, Cholesterol, etc.), mention if it is Normal, High, or Low compared to common healthy ranges.
                    3. At the end, provide a one-line overall interpretation or advice for the patient in simple English.
                    Do NOT repeat these instructions or say "You are a medical assistant AI".
                    
                    Report text:
                    """ + text;



            Map<String, Object> payload = Collections.singletonMap("inputs", prompt);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            //  Step 3: Try medical model first
            String summary = callModel(restTemplate, entity, PRIMARY_MODEL_URL);
            if (summary == null || summary.isBlank()) {
                // Fallback to general summarizer
                summary = callModel(restTemplate, entity, FALLBACK_MODEL_URL);
            }

            //  Step 4: Post-processing — clean up weird outputs
            if (summary == null || summary.isBlank()) {
                return "⚠️ No meaningful summary could be generated. Please upload a clearer medical file.";
            }

            // Remove echoed instructions (prompt text)
            summary = summary
                    .replaceAll("(?i)you are a medical assistant.*", "")
                    .replaceAll("(?i)read the following.*", "")
                    .replaceAll("(?i)report text:.*", "")
                    .trim();
            // Add a patient-friendly note at the end
            if (!summary.toLowerCase().contains("overall") && !summary.toLowerCase().contains("suggest")) {
                summary += "\n\n🩺 Overall: Please discuss these results with your doctor for personalized interpretation.";
            }
            return summary.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Unable to summarize this record at the moment. Please try again later.\n(" + e.getMessage() + ")";
        }
    }

    /**
     *  Helper method — calls Hugging Face model and extracts summary
     */
    private String callModel(RestTemplate restTemplate, HttpEntity<Map<String, Object>> entity, String modelUrl) {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(modelUrl, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            if (root.has("error")) {
                System.out.println("⚠️ Model at " + modelUrl + " is unavailable or loading...");
                return null;
            }

            if (root.isArray() && root.size() > 0 && root.get(0).has("summary_text")) {
                return root.get(0).get("summary_text").asText();
            }
        } catch (Exception ex) {
            System.out.println("❌ Error calling model: " + modelUrl + " — " + ex.getMessage());
        }
        return null;
    }
}
