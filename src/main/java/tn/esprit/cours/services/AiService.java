package tn.esprit.cours.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    // Use your domain URL here (reverse-proxied to Ollama)
    private final String apiUrl = "https://minolingo.online/ollama/v1/completions";

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateDescription(String title) {
        if (title == null || title.isBlank()) {
            return "Please provide a valid course title.";
        }

        String prompt = "Write a short course description for " + title;

        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",
                "prompt", prompt,
                "max_tokens", 200
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    return choices.get(0).get("text").toString().trim();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "AI generation failed: " + e.getMessage();
        }

        return "AI could not generate description.";
    }
}