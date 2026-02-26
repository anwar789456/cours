package tn.esprit.cours.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.2";

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateDescription(String title) {
        // Read token from environment variable
        String token = System.getenv("HF_TOKEN");
        if (token == null || token.isBlank()) {
            return "AI token not set in environment variables.";
        }

        String prompt = "Write a professional and engaging 150-word course description for an online children english learning platform.\nCourse Title: " + title;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of("inputs", prompt);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // HuggingFace API call
            List<Map<String, String>> response = restTemplate.postForObject(apiUrl, request, List.class);

            if (response != null && !response.isEmpty() && response.get(0).containsKey("generated_text")) {
                String raw = response.get(0).get("generated_text");
                // remove prompt prefix if present
                return raw.startsWith(prompt) ? raw.substring(prompt.length()).trim() : raw.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "AI could not generate description.";
    }
}