package tn.esprit.cours.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://minolingo.online/ollama/v1/completions";

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateDescription(String title) {

        if (title == null || title.isBlank()) {
            return "Please provide a valid course title.";
        }

        // Build JSON body for Ollama
        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",


                "prompt", """
                Write a detailed, engaging, SEO-friendly 80-word course description 
                for an online children's English learning platform.
                IMPORTANT:
                - Output ONLY the description text.
                - Do NOT add introductions.
                - Do NOT say "Here is a description" or anything alike.
                - Do NOT add explanations.
                - Do NOT use quotes.
                - Do NOT add extra commentary.

                Course Title: """ + title,

                "max_tokens", 120
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> responseEntity =
                    restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("choices")) {
                Object choicesObj = response.get("choices");
                if (choicesObj instanceof Iterable<?> choices) {
                    for (Object choice : choices) {
                        if (choice instanceof Map<?, ?> choiceMap && choiceMap.containsKey("text")) {
                            return choiceMap.get("text").toString().trim();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "AI generation failed: " + e.getMessage();
        }

        return "AI could not generate description.";
    }
}