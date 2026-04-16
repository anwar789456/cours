package tn.esprit.cours.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    // ── Cross-service data fetchers ──────────────────────────────────────────

    private List<Map<String, Object>> fetchEvents() {
        try {
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    "http://localhost:8082/api/events/get-all-events",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> fetchSubscriptionPlans() {
        try {
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    "http://localhost:8085/api/abonnements/get-all-abonnements",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> fetchUserCurrentSubscription(Long userId) {
        if (userId == null || userId <= 0) return Collections.emptyMap();
        try {
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    "http://localhost:8085/api/abonnements/user/" + userId + "/current-subscription",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> fetchForumTopics() {
        try {
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    "http://localhost:8083/api/forums/get-trending-topics",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Map<String, Object> fetchUserProfile(Long userId) {
        if (userId == null || userId <= 0) return Collections.emptyMap();
        try {
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    "http://localhost:8092/api/users/get-user-by-id/" + userId,
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            return res.getBody() != null ? res.getBody() : Collections.emptyMap();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> fetchDonations() {
        try {
            ResponseEntity<List<Map<String, Object>>> res = restTemplate.exchange(
                    "http://localhost:8084/api/donations/get-all-donations",
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<>() {});
            List<Map<String, Object>> all = res.getBody() != null ? res.getBody() : Collections.emptyList();
            return all.stream().limit(8).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
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

        sb.append("You are Mino, the friendly AI assistant for MinoLingo — an education platform for children to learn English. ");
        sb.append("You help with EVERYTHING on the website: courses, quizzes, events, donations, forums, subscriptions, AND account management.\n");
        sb.append("RULES:\n");
        sb.append("- Reply in 1-3 short sentences only. Be cheerful and simple.\n");
        sb.append("- When recommending a course, ALWAYS write 'Course #N'. When recommending a quiz, ALWAYS write 'Quiz #N'.\n");
        sb.append("- When recommending an event, ALWAYS write 'Event #N'. When suggesting the forum, write 'Forum'.\n");
        sb.append("- When suggesting donations, write 'Donation'. When suggesting subscriptions, write 'Subscription'.\n");
        sb.append("- You can answer questions about donating (what can be donated, how to improve donations, etc.).\n");
        sb.append("- You can recommend subscription plans and remind users to subscribe if they haven't.\n");
        sb.append("- You can suggest forum topics and encourage users to participate in discussions.\n");
        sb.append("- You can recommend upcoming events and encourage users to register.\n");
        sb.append("- You can help with account tasks: creating an account, logging in, resetting password, updating profile.\n");
        sb.append("- For account-related questions, mention the relevant page: 'Register', 'Login', 'Profile', 'Forgot Password'.\n\n");

        sb.append("ACCOUNT & NAVIGATION HELP:\n");
        sb.append("- To create an account: go to the Register page. User needs a name, email, and password. A verification code is sent by email.\n");
        sb.append("- First-time setup: after registering, the user should set up their username, password, and optionally a parental email for safety.\n");
        sb.append("- To log in: go to the Login page. Enter email and password, then solve the image captcha.\n");
        sb.append("- Login options: regular password login, Face Recognition login, or Google Sign-In.\n");
        sb.append("- Forgot password: go to the Forgot Password page, enter email, and a reset link will be sent.\n");
        sb.append("- To update profile: go to the Profile page. User can change name, email, avatar, and password.\n");
        sb.append("- Face setup: go to Face Setup to register face for biometric login (optional).\n");
        sb.append("- Google setup: link Google account for quick sign-in.\n\n");

        // ── Courses ──
        List<Cours> courses = coursRepository.findAll();
        List<Cours> activeCourses = courses.stream()
                .filter(c -> !c.isArchived()).collect(Collectors.toList());

        if (!activeCourses.isEmpty()) {
            sb.append("AVAILABLE COURSES:\n");
            activeCourses.stream().limit(12).forEach(c -> {
                sb.append("- Course #").append(c.getId())
                        .append(": \"").append(c.getTitle()).append("\"");
                if (c.getDescription() != null && !c.getDescription().isBlank()) {
                    String d = c.getDescription();
                    sb.append(" (").append(d.length() > 70 ? d.substring(0, 70) + "..." : d).append(")");
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        // ── Student progress ──
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

        // ── Quizzes ──
        List<Quiz> openQuizzes = quizRepository.findAll().stream()
                .filter(q -> !q.isArchived()).limit(8).collect(Collectors.toList());
        if (!openQuizzes.isEmpty()) {
            sb.append("AVAILABLE QUIZZES:\n");
            openQuizzes.forEach(q -> sb.append("- Quiz #").append(q.getId())
                    .append(": \"").append(q.getTitle())
                    .append("\" (").append(q.getLevel()).append(")\n"));
            sb.append("\n");
        }

        // ── Events (cross-service) ──
        List<Map<String, Object>> events = fetchEvents();
        List<Map<String, Object>> upcomingEvents = events.stream()
                .filter(e -> "UPCOMING".equals(e.get("status")) || "ONGOING".equals(e.get("status")))
                .limit(8).collect(Collectors.toList());
        if (!upcomingEvents.isEmpty()) {
            sb.append("UPCOMING EVENTS:\n");
            upcomingEvents.forEach(e -> {
                sb.append("- Event #").append(e.get("id"))
                        .append(": \"").append(e.getOrDefault("title", "")).append("\"");
                if (e.get("startDate") != null) sb.append(" on ").append(e.get("startDate"));
                if (e.get("location") != null) sb.append(" at ").append(e.get("location"));
                if (e.get("targetLevel") != null) sb.append(" (").append(e.get("targetLevel")).append(")");
                sb.append("\n");
            });
            sb.append("\n");
        }

        // ── Subscription Plans (cross-service) ──
        List<Map<String, Object>> plans = fetchSubscriptionPlans();
        if (!plans.isEmpty()) {
            sb.append("SUBSCRIPTION PLANS:\n");
            plans.forEach(p -> {
                sb.append("- ").append(p.getOrDefault("name", p.getOrDefault("planType", "")))
                        .append(": ").append(p.getOrDefault("price", "")).append(" EUR/")
                        .append(p.getOrDefault("durationDays", "")).append(" days");
                if (p.get("description") != null) {
                    String d = p.get("description").toString();
                    sb.append(" — ").append(d.length() > 60 ? d.substring(0, 60) + "..." : d);
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        // ── User's subscription status ──
        if (request.getUserId() != null && request.getUserId() > 0) {
            Map<String, Object> currentSub = fetchUserCurrentSubscription(request.getUserId());
            if (currentSub.isEmpty()) {
                sb.append("NOTE: This student has NO active subscription. Encourage them to subscribe for full access!\n\n");
            } else {
                sb.append("STUDENT SUBSCRIPTION:\n");
                sb.append("- Plan: ").append(currentSub.getOrDefault("planType", ""))
                        .append(", Status: ").append(currentSub.getOrDefault("status", ""))
                        .append(", Expires: ").append(currentSub.getOrDefault("endDate", "unknown"))
                        .append("\n\n");
            }
        }

        // ── Forum Topics (cross-service) ──
        List<Map<String, Object>> topics = fetchForumTopics();
        if (!topics.isEmpty()) {
            sb.append("TRENDING FORUM TOPICS:\n");
            topics.stream().limit(6).forEach(t -> {
                sb.append("- \"").append(t.getOrDefault("title", "")).append("\"");
                if (t.get("category") != null) sb.append(" [").append(t.get("category")).append("]");
                sb.append("\n");
            });
            sb.append("\n");
        }

        // ── Donation Info ──
        sb.append("DONATION INFO:\n");
        sb.append("- Users can donate items (clothes/VETEMENT, toys/games/JEU) to help other children.\n");
        sb.append("- Donors earn MerciPoints as rewards for their generosity.\n");
        sb.append("- Tips for good donations: items should be clean, in good condition, and clearly described.\n");
        sb.append("- Categories: VETEMENT (clothing), JEU (toys/games).\n");
        sb.append("- Conditions: NEW, GOOD, FAIR.\n\n");

        // ── User Profile (cross-service) ──
        if (request.getUserId() != null && request.getUserId() > 0) {
            Map<String, Object> profile = fetchUserProfile(request.getUserId());
            if (!profile.isEmpty()) {
                sb.append("CURRENT USER PROFILE:\n");
                sb.append("- Name: ").append(profile.getOrDefault("name", "unknown")).append("\n");
                sb.append("- Email: ").append(profile.getOrDefault("email", "unknown")).append("\n");
                sb.append("- Role: ").append(profile.getOrDefault("role", "ETUDIANT")).append("\n");
                if (profile.get("needsSetup") != null && Boolean.TRUE.equals(profile.get("needsSetup"))) {
                    sb.append("- NOTE: This user still needs to complete their first-time setup! Guide them to set a username, password, and parental email.\n");
                }
                sb.append("\n");
            }
        } else {
            sb.append("NOTE: The user is NOT logged in. If they ask account questions, guide them to Register or Login.\n\n");
        }

        if (request.getCurrentPage() != null && !request.getCurrentPage().isBlank()) {
            sb.append("Student is on page: ").append(request.getCurrentPage()).append("\n\n");
        }

        sb.append("Student says: ").append(request.getMessage()).append("\nMino's reply:");
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
        Set<String> seen = new HashSet<>();

        if (reply == null) reply = "";
        String lower = reply.toLowerCase();

        List<Quiz> activeQuizzes = quizRepository.findAll().stream()
                .filter(q -> !q.isArchived() && q.getTitle() != null)
                .collect(Collectors.toList());
        List<Cours> activeCourses = coursRepository.findAll().stream()
                .filter(c -> !c.isArchived() && c.getTitle() != null)
                .collect(Collectors.toList());

        // 1. Extract explicit quiz IDs — e.g. "Quiz #5"
        java.util.regex.Matcher quizMatcher = java.util.regex.Pattern
                .compile("\\bquiz\\s*#?(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(reply);
        while (quizMatcher.find() && list.size() < 4) {
            String id = quizMatcher.group(1);
            Quiz q = activeQuizzes.stream()
                    .filter(x -> String.valueOf(x.getId()).equals(id))
                    .findFirst().orElse(null);
            if (q == null) continue;
            String route = "/quiz/" + id + "/play";
            if (seen.add(route)) list.add(new Suggestion("▶ " + q.getTitle(), route));
        }

        // 2. Extract explicit course IDs — e.g. "Course #3"
        java.util.regex.Matcher courseMatcher = java.util.regex.Pattern
                .compile("\\bcourse[s]?\\s*#?(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(reply);
        while (courseMatcher.find() && list.size() < 4) {
            String id = courseMatcher.group(1);
            Cours c = activeCourses.stream()
                    .filter(x -> String.valueOf(x.getId()).equals(id))
                    .findFirst().orElse(null);
            if (c == null) continue;
            String route = "/courses?open=" + id;
            if (seen.add(route)) list.add(new Suggestion("📖 " + c.getTitle(), route));
        }

        // 3. Extract explicit event IDs — e.g. "Event #7"
        java.util.regex.Matcher eventMatcher = java.util.regex.Pattern
                .compile("\\bevent\\s*#?(\\d+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(reply);
        while (eventMatcher.find() && list.size() < 4) {
            String id = eventMatcher.group(1);
            String route = "/events?open=" + id;
            if (seen.add(route)) list.add(new Suggestion("📅 Event #" + id, route));
        }

        // 4. Title-match fallback
        if (list.size() < 4) {
            for (Quiz q : activeQuizzes) {
                if (list.size() >= 4) break;
                if (lower.contains(q.getTitle().toLowerCase())) {
                    String route = "/quiz/" + q.getId() + "/play";
                    if (seen.add(route)) list.add(new Suggestion("▶ " + q.getTitle(), route));
                }
            }
            for (Cours c : activeCourses) {
                if (list.size() >= 4) break;
                if (lower.contains(c.getTitle().toLowerCase())) {
                    String route = "/courses?open=" + c.getId();
                    if (seen.add(route)) list.add(new Suggestion("📖 " + c.getTitle(), route));
                }
            }
        }

        // 5. Topic-based fallback for all sections
        if (list.size() < 4) {
            boolean mentionsQuiz = lower.contains("quiz") || lower.contains("test") || lower.contains("practice");
            boolean mentionsCourse = lower.contains("course") || lower.contains("lesson") || lower.contains("learn");
            boolean mentionsEvent = lower.contains("event") || lower.contains("workshop") || lower.contains("meetup");
            boolean mentionsForum = lower.contains("forum") || lower.contains("discuss") || lower.contains("topic") || lower.contains("post");
            boolean mentionsDonation = lower.contains("donat") || lower.contains("give") || lower.contains("merci");
            boolean mentionsSubscription = lower.contains("subscri") || lower.contains("plan") || lower.contains("premium") || lower.contains("abonnement");

            if (mentionsQuiz && !activeQuizzes.isEmpty() && list.size() < 4) {
                Quiz q = activeQuizzes.get(0);
                String route = "/quiz/" + q.getId() + "/play";
                if (seen.add(route)) list.add(new Suggestion("▶ " + q.getTitle(), route));
            }
            if (mentionsCourse && !activeCourses.isEmpty() && list.size() < 4) {
                Cours c = activeCourses.get(0);
                String route = "/courses?open=" + c.getId();
                if (seen.add(route)) list.add(new Suggestion("📖 " + c.getTitle(), route));
            }
            if (mentionsEvent && list.size() < 4 && seen.add("/events")) {
                list.add(new Suggestion("📅 Browse Events", "/events"));
            }
            if (mentionsForum && list.size() < 4 && seen.add("/forums")) {
                list.add(new Suggestion("💬 Visit Forums", "/forums"));
            }
            if (mentionsDonation && list.size() < 4 && seen.add("/donations")) {
                list.add(new Suggestion("🎁 Make a Donation", "/donations"));
            }
            if (mentionsSubscription && list.size() < 4 && seen.add("/subscriptions")) {
                list.add(new Suggestion("⭐ View Plans", "/subscriptions"));
            }

            // Account-related suggestions
            boolean mentionsRegister = lower.contains("register") || lower.contains("sign up") || lower.contains("create account") || lower.contains("new account");
            boolean mentionsLogin = lower.contains("log in") || lower.contains("login") || lower.contains("sign in");
            boolean mentionsPassword = lower.contains("password") || lower.contains("forgot") || lower.contains("reset");
            boolean mentionsProfile = lower.contains("profile") || lower.contains("avatar") || lower.contains("account") || lower.contains("settings");
            boolean mentionsFace = lower.contains("face") || lower.contains("biometric") || lower.contains("face recognition");
            boolean mentionsSetup = lower.contains("setup") || lower.contains("first time") || lower.contains("username") || lower.contains("parental");

            if (mentionsRegister && list.size() < 4 && seen.add("/register")) {
                list.add(new Suggestion("📝 Create Account", "/register"));
            }
            if (mentionsLogin && list.size() < 4 && seen.add("/login")) {
                list.add(new Suggestion("🔑 Go to Login", "/login"));
            }
            if (mentionsPassword && list.size() < 4 && seen.add("/forgot-password")) {
                list.add(new Suggestion("🔒 Reset Password", "/forgot-password"));
            }
            if (mentionsProfile && list.size() < 4 && seen.add("/profile")) {
                list.add(new Suggestion("👤 My Profile", "/profile"));
            }
            if (mentionsFace && list.size() < 4 && seen.add("/face-setup")) {
                list.add(new Suggestion("📷 Face Setup", "/face-setup"));
            }
            if (mentionsSetup && list.size() < 4 && seen.add("/google-setup")) {
                list.add(new Suggestion("⚙ Complete Setup", "/google-setup"));
            }
        }

        return list.size() > 4 ? list.subList(0, 4) : list;
    }
}
