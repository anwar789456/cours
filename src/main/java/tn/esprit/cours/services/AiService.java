package tn.esprit.cours.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://router.huggingface.co/models/facebook/opt-1.3b";

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateDescription(String title) {
        String token = System.getenv("HF_TOKEN");
        if (token == null || token.isBlank()) {
            return "AI token not set in environment variables.";
        }

        if (title == null || title.isBlank()) {
            return "Please provide a valid course title.";
        }

        String prompt = """
                You are a professional educational content writer.
                Write a detailed, engaging, and SEO-friendly 150-word course description
                for an online children's English learning platform.

                Course Title: %s

                Course Description:
                """.formatted(title);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of(
                "inputs", prompt,
                "parameters", Map.of(
                        "max_new_tokens", 200,
                        "temperature", 0.7
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List> responseEntity =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, request, List.class);

            List<Map<String, Object>> response = responseEntity.getBody();

            if (response != null && !response.isEmpty()) {
                Map<String, Object> first = response.get(0);

                // Check for 'generated_text'
                if (first.containsKey("generated_text")) {
                    return first.get("generated_text").toString().trim();
                }

                // If model returns an error
                if (first.containsKey("error")) {
                    return "AI generation failed: " + first.get("error");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "AI generation failed: " + e.getMessage();
        }

        return "AI could not generate description.";
    }
}