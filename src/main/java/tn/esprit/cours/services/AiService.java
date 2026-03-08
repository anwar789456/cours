package tn.esprit.cours.services;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://minolingo.online/ollama/v1/completions";

    public AiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10s connect
        factory.setReadTimeout(50000);     // 50s read (under nginx 60s limit)
        this.restTemplate = new RestTemplate(factory);
    }

    public String generateDescription(String title) {

        if (title == null || title.isBlank()) {
            return "Please provide a valid course title.";
        }

        // Build JSON body for Ollama
        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",


                "prompt", """
                Write a detailed, engaging, SEO-friendly 50-word course description 
                for an online children's English learning platform.
                IMPORTANT:
                - Output ONLY the description text.
                - Do NOT add introductions.
                - Do NOT say "Here is a description" or anything alike.
                - Do NOT add explanations.
                - Do NOT use quotes.
                - Do NOT add extra commentary.

                Course Title: """ + title,

                "max_tokens", 150
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

    public String generateQuizDescription(String title, String level) {
        if (title == null || title.isBlank()) {
            return "Please provide a valid quiz title.";
        }

        String levelInfo = (level != null && !level.isBlank()) ? "\nDifficulty Level: " + level : "";

        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",
                "prompt", """
                Write a short, engaging 20-word quiz description for a children's English learning platform.
                IMPORTANT:
                - Output ONLY the description text.
                - Do NOT add introductions or explanations.
                - Do NOT say "Here is a description" or anything alike.
                - Do NOT use quotes.
                - Make it fun and encouraging for young learners.

                Quiz Title: """ + title + levelInfo,
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
        return "AI could not generate quiz description.";
    }

    public String generateSingleQuizQuestion(String title, String level, int questionNumber) {
        if (title == null || title.isBlank()) {
            return "{}";
        }

        String levelInfo = (level != null && !level.isBlank()) ? level : "BEGINNER";

        String prompt = "Generate 1 multiple-choice question (#" + questionNumber + ") for a children's English quiz titled \"" + title + "\" (" + levelInfo + " level).\n"
                + "Output ONLY a single JSON object with these fields:\n"
                + "\"question\": text, \"options\": [4 strings], \"correctAnswer\": one of the options, \"explanation\": 1 sentence.\n"
                + "Output MUST start with { and end with }. No extra text.";

        Map<String, Object> body = Map.of(
                "model", "llama3.2:1b",
                "prompt", prompt,
                "max_tokens", 300
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
                            String raw = choiceMap.get("text").toString().trim();
                            // Extract JSON object from response
                            int startIdx = raw.indexOf('{');
                            int endIdx = raw.lastIndexOf('}');
                            if (startIdx >= 0 && endIdx > startIdx) {
                                return raw.substring(startIdx, endIdx + 1);
                            }
                            return raw;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
        return "{}";
    }
}