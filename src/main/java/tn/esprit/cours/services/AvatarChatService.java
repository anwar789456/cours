package tn.esprit.cours.services;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.entity.CourseProgress;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.entity.dto.AvatarChatRequest;
import tn.esprit.cours.entity.dto.AvatarChatResponse;
import tn.esprit.cours.entity.dto.AvatarChatResponse.Suggestion;
import tn.esprit.cours.repository.CoursRepository;
import tn.esprit.cours.repository.CourseProgressRepository;
import tn.esprit.cours.repository.QuizRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvatarChatService {

    private final CoursRepository coursRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final QuizRepository quizRepository;

    private final String ollamaUrl = "https://minolingo.online/ollama/v1/completions";
    private final RestTemplate restTemplate;

    public AvatarChatService(CoursRepository coursRepository,
                             CourseProgressRepository courseProgressRepository,
                             QuizRepository quizRepository) {
        this.coursRepository = coursRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.quizRepository = quizRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(50000);
        this.restTemplate = new RestTemplate(factory);
    }

    public AvatarChatResponse chat(AvatarChatRequest request) {
        String prompt = buildPrompt(request);
        String aiReply = callOllama(prompt);
        List<Suggestion> suggestions = extractSuggestions(aiReply, request.getCurrentPage());
        return new AvatarChatResponse(aiReply, suggestions);
    }

    private String buildPrompt(AvatarChatRequest request) {
        StringBuilder sb = new StringBuilder();

        // System instruction
        sb.append("You are \"Lingo\", a friendly, fun AI English tutor for kids on the MiniLingo platform. ");
        sb.append("You help children aged 6-14 learn English. ");
        sb.append("Keep your answers SHORT (2-3 sentences max), encouraging, and easy to understand. ");
        sb.append("Use simple words. Be enthusiastic! ");
        sb.append("If a kid asks about a course or quiz, recommend one from the list below by name. ");
        sb.append("If they ask an English grammar or vocabulary question, explain it simply with an example. ");
        sb.append("Never use markdown formatting. Reply in plain text only.\n\n");

        // Available courses
        List<Cours> courses = coursRepository.findAll();
        List<Cours> activeCourses = courses.stream()
                .filter(c -> !c.isArchived())
                .collect(Collectors.toList());

        if (!activeCourses.isEmpty()) {
            sb.append("AVAILABLE COURSES:\n");
            for (Cours c : activeCourses.stream().limit(15).collect(Collectors.toList())) {
                sb.append("- \"").append(c.getTitle()).append("\"");
                if (c.getDescription() != null && !c.getDescription().isBlank()) {
                    String desc = c.getDescription();
                    if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
                    sb.append(" (").append(desc).append(")");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        // Student progress
        if (request.getUserId() != null && request.getUserId() > 0) {
            List<CourseProgress> progresses = courseProgressRepository.findByUserId(request.getUserId());
            if (!progresses.isEmpty()) {
                sb.append("STUDENT'S PROGRESS:\n");
                for (CourseProgress p : progresses) {
                    String courseTitle = activeCourses.stream()
                            .filter(c -> c.getId().equals(p.getCoursId()))
                            .map(Cours::getTitle)
                            .findFirst()
                            .orElse("Course #" + p.getCoursId());
                    sb.append("- ").append(courseTitle).append(": ")
                            .append(String.format("%.0f", p.getProgress())).append("% complete\n");
                }
                sb.append("\n");
            }
        }

        // Available quizzes
        List<Quiz> quizzes = quizRepository.findAll();
        List<Quiz> openQuizzes = quizzes.stream()
                .filter(q -> !q.isArchived())
                .limit(10)
                .collect(Collectors.toList());

        if (!openQuizzes.isEmpty()) {
            sb.append("AVAILABLE QUIZZES:\n");
            for (Quiz q : openQuizzes) {
                sb.append("- \"").append(q.getTitle()).append("\" (Level: ")
                        .append(q.getLevel()).append(", XP: ").append(q.getXpReward()).append(")\n");
            }
            sb.append("\n");
        }

        // Current page context
        if (request.getCurrentPage() != null && !request.getCurrentPage().isBlank()) {
            sb.append("The student is currently on the page: ").append(request.getCurrentPage()).append("\n\n");
        }

        // Student message
        sb.append("Student says: ").append(request.getMessage()).append("\n");
        sb.append("Lingo's reply:");

        return sb.toString();
    }

    private String callOllama(String prompt) {
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
                    restTemplate.exchange(ollamaUrl, HttpMethod.POST, request, Map.class);

            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("choices")) {
                Object choicesObj = response.get("choices");
                if (choicesObj instanceof Iterable<?> choices) {
                    for (Object choice : choices) {
                        if (choice instanceof Map<?, ?> choiceMap && choiceMap.containsKey("text")) {
                            String raw = choiceMap.get("text").toString().trim();
                            // Clean up: take only first paragraph to keep it short
                            int doubleNewline = raw.indexOf("\n\n");
                            if (doubleNewline > 0 && doubleNewline < raw.length() - 2) {
                                raw = raw.substring(0, doubleNewline).trim();
                            }
                            return raw;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Oops! I'm having a little trouble right now. Try asking me again in a moment!";
        }

        return "Hmm, I didn't quite get that. Could you try asking me in a different way?";
    }

    private List<Suggestion> extractSuggestions(String reply, String currentPage) {
        List<Suggestion> suggestions = new ArrayList<>();
        String lower = reply.toLowerCase();

        // Smart suggestions based on what Lingo is talking about
        if (lower.contains("course") || lower.contains("lesson") || lower.contains("learn")) {
            if (currentPage == null || !currentPage.contains("/courses")) {
                suggestions.add(new Suggestion("Browse Courses", "/courses"));
            }
        }
        if (lower.contains("quiz") || lower.contains("test") || lower.contains("practice")) {
            if (currentPage == null || !currentPage.contains("/quiz")) {
                suggestions.add(new Suggestion("Take a Quiz", "/quiz"));
            }
        }
        if (lower.contains("friend") || lower.contains("together") || lower.contains("classmate")) {
            if (currentPage == null || !currentPage.contains("/friends")) {
                suggestions.add(new Suggestion("Find Friends", "/friends"));
            }
        }
        if (lower.contains("session") || lower.contains("tutor") || lower.contains("teacher")) {
            if (currentPage == null || !currentPage.contains("/sessions")) {
                suggestions.add(new Suggestion("Book a Session", "/sessions"));
            }
        }
        if (lower.contains("forum") || lower.contains("discuss") || lower.contains("ask")) {
            if (currentPage == null || !currentPage.contains("/forums")) {
                suggestions.add(new Suggestion("Visit Forums", "/forums"));
            }
        }

        // Limit to max 3 suggestions
        if (suggestions.size() > 3) {
            suggestions = suggestions.subList(0, 3);
        }

        return suggestions;
    }
}
