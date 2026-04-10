package tn.esprit.cours.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import tn.esprit.cours.entity.Cours;
import tn.esprit.cours.entity.CourseProgress;
import tn.esprit.cours.entity.Quiz;
import tn.esprit.cours.entity.dto.AvatarChatRequest;
import tn.esprit.cours.entity.dto.AvatarChatResponse;
import tn.esprit.cours.entity.dto.AvatarChatResponse.Suggestion;
import tn.esprit.cours.repository.CoursRepository;
import tn.esprit.cours.repository.CourseProgressRepository;
import tn.esprit.cours.repository.QuizRepository;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvatarChatService {

    private final CoursRepository coursRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final QuizRepository quizRepository;

    private final String ollamaUrl = "https://minolingo.online/ollama/v1/completions";

 
    @Value("${avatar.ai.model:qwen2.5:3b}")
    private String model;

    public String getModel() { return model; }

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public AvatarChatService(CoursRepository coursRepository,
                             CourseProgressRepository courseProgressRepository,
                             QuizRepository quizRepository) {
        this.coursRepository = coursRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.quizRepository = quizRepository;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(55000);
        this.restTemplate = new RestTemplate(factory);

        this.webClient = WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    // ── Non-streaming (used by floating widget) ──────────────────────────────
    public AvatarChatResponse chat(AvatarChatRequest request) {
        String prompt = buildPrompt(request);
        String aiReply = callOllama(prompt);
        List<Suggestion> suggestions = extractSuggestions(aiReply, request.getCurrentPage());
        return new AvatarChatResponse(aiReply, suggestions);
    }

    // ── Streaming (used by dedicated tutor page) ─────────────────────────────
    public Flux<String> streamChat(AvatarChatRequest request) {
        String prompt = buildPrompt(request);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("max_tokens", 120);
        body.put("stream", true);

        return webClient.post()
                .uri(ollamaUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(DataBuffer.class)
                .concatMap(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    String chunk = new String(bytes, StandardCharsets.UTF_8);

                    // Each chunk may contain multiple newline-delimited JSON lines
                    List<String> tokens = new ArrayList<>();
                    for (String line : chunk.split("\n")) {
                        line = line.trim();
                        if (line.isBlank() || "[DONE]".equals(line)) continue;
                        // Strip SSE "data: " prefix if present
                        if (line.startsWith("data: ")) line = line.substring(6).trim();
                        if ("[DONE]".equals(line) || line.isBlank()) continue;
                        String token = extractStreamToken(line);
                        if (token != null && !token.isEmpty()) tokens.add(token);
                    }
                    return Flux.fromIterable(tokens);
                })
                .onErrorReturn("[ERROR]");
    }

    // ── Extract one text token from a streaming JSON line ────────────────────
    private String extractStreamToken(String line) {
        if (line == null || line.isBlank()) return null;
        // Skip lines indicating end/error
        if (line.equals("[DONE]") || line.contains("\"finish_reason\":\"stop\"")
                || line.contains("\"finish_reason\": \"stop\"")
                || line.contains("\"finish_reason\":\"length\"")) {
            return null;
        }
        // Find "text":"<value>" — handle both compact and spaced JSON formats
        int idx = line.indexOf("\"text\":\"");
        int start;
        if (idx >= 0) {
            start = idx + 8;
        } else {
            idx = line.indexOf("\"text\": \"");
            if (idx < 0) return null;
            start = idx + 9;
        }
        if (start >= line.length()) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = start; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\\' && i + 1 < line.length()) {
                char next = line.charAt(++i);
                switch (next) {
                    case '"'  -> sb.append('"');
                    case 'n'  -> sb.append('\n');
                    case 't'  -> sb.append('\t');
                    case 'r'  -> {} // skip CR
                    case '\\' -> sb.append('\\');
                    default   -> { sb.append('\\'); sb.append(next); }
                }
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // ── Shared prompt builder ─────────────────────────────────────────────────
    public String buildPrompt(AvatarChatRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a fun English tutor for kids. ");
        sb.append("RULES: Reply in 1-3 short sentences only. ");
        sb.append("Be cheerful and simple. If asked about a course or quiz, recommend one from the list below.\n\n");

        List<Cours> courses = coursRepository.findAll();
        List<Cours> activeCourses = courses.stream()
                .filter(c -> !c.isArchived()).collect(Collectors.toList());

        if (!activeCourses.isEmpty()) {
            sb.append("AVAILABLE COURSES:\n");
            activeCourses.stream().limit(12).forEach(c -> {
                sb.append("- \"").append(c.getTitle()).append("\"");
                if (c.getDescription() != null && !c.getDescription().isBlank()) {
                    String d = c.getDescription();
                    sb.append(" (").append(d.length() > 70 ? d.substring(0, 70) + "..." : d).append(")");
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        if (request.getUserId() != null && request.getUserId() > 0) {
            List<CourseProgress> progresses = courseProgressRepository.findByUserId(request.getUserId());
            if (!progresses.isEmpty()) {
                sb.append("STUDENT PROGRESS:\n");
                progresses.forEach(p -> {
                    String title = activeCourses.stream()
                            .filter(c -> c.getId().equals(p.getCoursId()))
                            .map(Cours::getTitle).findFirst()
                            .orElse("Course #" + p.getCoursId());
                    sb.append("- ").append(title).append(": ")
                            .append(String.format("%.0f", p.getProgress())).append("%\n");
                });
                sb.append("\n");
            }
        }

        List<Quiz> openQuizzes = quizRepository.findAll().stream()
                .filter(q -> !q.isArchived()).limit(8).collect(Collectors.toList());
        if (!openQuizzes.isEmpty()) {
            sb.append("AVAILABLE QUIZZES:\n");
            openQuizzes.forEach(q -> sb.append("- \"").append(q.getTitle())
                    .append("\" (").append(q.getLevel()).append(")\n"));
            sb.append("\n");
        }

        if (request.getCurrentPage() != null && !request.getCurrentPage().isBlank()) {
            sb.append("Student is on page: ").append(request.getCurrentPage()).append("\n\n");
        }

        sb.append("Student says: ").append(request.getMessage()).append("\nLingo's reply:");
        return sb.toString();
    }

    // ── Non-streaming Ollama call (for floating widget) ───────────────────────
    private String callOllama(String prompt) {
        Map<String, Object> body = Map.of("model", model, "prompt", prompt, "max_tokens", 300);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            ResponseEntity<Map> res = restTemplate.exchange(
                    ollamaUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            Map<String, Object> r = res.getBody();
            if (r != null && r.containsKey("choices")) {
                for (Object ch : (Iterable<?>) r.get("choices")) {
                    if (ch instanceof Map<?, ?> m && m.containsKey("text")) {
                        String raw = m.get("text").toString().trim();
                        int nl = raw.indexOf("\n\n");
                        return nl > 0 ? raw.substring(0, nl).trim() : raw;
                    }
                }
            }
        } catch (Exception e) {
            return "Oops! I'm having a little trouble right now. Try asking again!";
        }
        return "Hmm, could you try asking me in a different way?";
    }

    // ── Suggestion extraction ─────────────────────────────────────────────────
    List<Suggestion> extractSuggestions(String reply, String currentPage) {
        List<Suggestion> list = new ArrayList<>();
        String l = reply.toLowerCase();
        String p = currentPage == null ? "" : currentPage;
        if ((l.contains("course") || l.contains("lesson") || l.contains("learn")) && !p.contains("/courses"))
            list.add(new Suggestion("Browse Courses", "/courses"));
        if ((l.contains("quiz") || l.contains("test") || l.contains("practice")) && !p.contains("/quiz"))
            list.add(new Suggestion("Take a Quiz", "/quiz"));
        if ((l.contains("session") || l.contains("tutor") || l.contains("teacher")) && !p.contains("/sessions"))
            list.add(new Suggestion("Book a Session", "/sessions"));
        if ((l.contains("forum") || l.contains("discuss")) && !p.contains("/forums"))
            list.add(new Suggestion("Visit Forums", "/forums"));
        return list.size() > 3 ? list.subList(0, 3) : list;
    }
}
