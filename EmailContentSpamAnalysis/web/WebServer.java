import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public class WebServer {

    private static final int PORT = 8080;
    private static final String WEB_DIR = "web";
    private static final String EMAIL_FILE = "data/emails.txt";
    private static final String KEYWORD_FILE = "data/spam_keywords.txt";

    private static String[] spamPatterns;
    private static AhoCorasick ahoCorasick;
    private static Map<String, Set<String>> rules;

    public static void main(String[] args) throws IOException {
        initAlgorithms();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // API routes
        server.createContext("/api/accounts", WebServer::handleAccounts);
        server.createContext("/api/emails", WebServer::handleEmails);
        server.createContext("/api/analyze", WebServer::handleAnalyze);
        server.createContext("/api/compare", WebServer::handleCompare);
        server.createContext("/api/compose", WebServer::handleCompose);

        // Static files handler (web/)
        server.createContext("/", WebServer::handleStaticFile);

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("==================================================");
        System.out.println("  EMAIL SPAM ANALYSIS - WEB SERVER RUNNING");
        System.out.println("==================================================");
        System.out.printf("  Web Dashboard URL : http://localhost:%d%n", PORT);
        System.out.println("  API Endpoint      : http://localhost:" + PORT + "/api/accounts");
        System.out.println("  Press Ctrl+C to stop the server.");
        System.out.println("==================================================");
    }

    private static void initAlgorithms() {
        List<String> rawKeywords = EmailReader.readSpamKeywords(KEYWORD_FILE);
        Set<String> uniqueKeywords = new LinkedHashSet<>();
        for (String kw : rawKeywords) {
            String clean = TextPreprocessor.cleanText(kw);
            if (!clean.isEmpty()) uniqueKeywords.add(clean);
        }
        spamPatterns = uniqueKeywords.toArray(new String[0]);
        ahoCorasick = new AhoCorasick(spamPatterns);
        rules = createSpamRules();
    }

    // --- API Handlers ---

    private static void handleAccounts(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        List<Email> emails = EmailReader.readEmails(EMAIL_FILE);
        Map<String, Integer> accounts = EmailReader.getEmailAccountsSummary(emails);

        StringBuilder json = new StringBuilder("[");
        int i = 0;
        for (Map.Entry<String, Integer> entry : accounts.entrySet()) {
            if (i > 0) json.append(",");
            json.append(String.format("{\"email\":\"%s\",\"count\":%d}", escapeJson(entry.getKey()), entry.getValue()));
            i++;
        }
        json.append("]");
        sendJsonResponse(exchange, 200, json.toString());
    }

    private static void handleEmails(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String user = params.get("user");

        List<Email> allEmails = EmailReader.readEmails(EMAIL_FILE);
        List<Email> userEmails = (user != null && !user.isEmpty())
                ? EmailReader.getMessagesForEmail(allEmails, user)
                : allEmails.subList(0, Math.min(25, allEmails.size()));

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < userEmails.size(); i++) {
            Email e = userEmails.get(i);
            if (i > 0) json.append(",");
            json.append(String.format("{\"id\":%d,\"user\":\"%s\",\"subject\":\"%s\",\"body\":\"%s\"}",
                    e.getMessageId(), escapeJson(e.getEmailId()), escapeJson(e.getSubject()), escapeJson(e.getBody())));
        }
        json.append("]");
        sendJsonResponse(exchange, 200, json.toString());
    }

    private static void handleAnalyze(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String subject = extractJsonField(requestBody, "subject");
        String body = extractJsonField(requestBody, "body");
        String fullText = subject + " " + body;
        String clean = TextPreprocessor.cleanText(fullText);

        // Run Algorithms
        List<String> kmp = new ArrayList<>();
        List<String> rk = new ArrayList<>();
        List<String> z = new ArrayList<>();
        for (String p : spamPatterns) {
            if (KMP.search(clean, p)) kmp.add(p);
            if (RabinKarp.search(clean, p)) rk.add(p);
            if (ZAlgorithm.search(clean, p)) z.add(p);
        }
        List<String> aho = ahoCorasick.search(clean);

        Set<String> allMatches = new LinkedHashSet<>();
        allMatches.addAll(kmp); allMatches.addAll(rk); allMatches.addAll(z); allMatches.addAll(aho);
        List<String> combined = new ArrayList<>(allMatches);

        int featureCount = Math.min(3, combined.size());
        int[] scores = new int[featureCount];
        Arrays.fill(scores, 4);
        int bitmaskScore = (scores.length > 0) ? BitmaskDP.findBestScore(scores, scores.length) : 0;
        int networkFlow = calculateNetworkFlow(combined);
        List<String> selectedRules = SetCoverApproximation.selectRules(rules, new HashSet<>(combined));
        int randHash = new RandomizedHash().getHash(clean);

        String category = detectCategory(combined);
        double confidence = Math.min(100.0, Math.max(0.0, (bitmaskScore / 12.0) * 100.0));
        String risk = (bitmaskScore >= 8) ? "HIGH" : (bitmaskScore >= 4) ? "MEDIUM" : "LOW";
        boolean isSpam = SpamAnalyzer.isSpam(bitmaskScore);

        List<String> urls = detectUrls(fullText);
        List<String> suspiciousUrls = detectSuspiciousUrls(urls);

        String response = String.format("{"
                + "\"isSpam\":%b,"
                + "\"classification\":\"%s\","
                + "\"score\":%d,"
                + "\"confidence\":%.2f,"
                + "\"riskLevel\":\"%s\","
                + "\"category\":\"%s\","
                + "\"kmpMatches\":%s,"
                + "\"rabinKarpMatches\":%s,"
                + "\"zMatches\":%s,"
                + "\"ahoMatches\":%s,"
                + "\"combinedMatches\":%s,"
                + "\"bitmaskScore\":%d,"
                + "\"networkFlow\":%d,"
                + "\"selectedRules\":%s,"
                + "\"randomizedHash\":\"0x%08X\","
                + "\"urls\":%s,"
                + "\"suspiciousUrls\":%s"
                + "}",
                isSpam, isSpam ? "SPAM" : "NOT SPAM", bitmaskScore, confidence, risk, category,
                toJsonArray(kmp), toJsonArray(rk), toJsonArray(z), toJsonArray(aho), toJsonArray(combined),
                bitmaskScore, networkFlow, toJsonArray(selectedRules), randHash,
                toJsonArray(urls), toJsonArray(suspiciousUrls));

        sendJsonResponse(exchange, 200, response);
    }

    private static void handleCompare(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        String user = params.get("user");
        if (user == null || user.isEmpty()) user = "user1@gmail.com";

        List<Email> allEmails = EmailReader.readEmails(EMAIL_FILE);
        List<Email> userMessages = EmailReader.getMessagesForEmail(allEmails, user);
        if (userMessages.isEmpty()) {
            sendJsonResponse(exchange, 404, "{\"error\":\"User not found\"}");
            return;
        }

        // Run side-by-side benchmark
        long start;
        double kmpTime, rkTime, zTime, ahoTime, dpTime, ekTime, scTime, rhTime, ppTime;

        // KMP
        start = System.nanoTime();
        int kmpMatches = 0;
        for (Email e : userMessages) {
            String text = TextPreprocessor.cleanText(e.getFullText());
            for (String p : spamPatterns) if (KMP.search(text, TextPreprocessor.cleanText(p))) kmpMatches++;
        }
        kmpTime = (System.nanoTime() - start) / 1_000_000.0;

        // Rabin-Karp
        start = System.nanoTime();
        int rkMatches = 0;
        for (Email e : userMessages) {
            String text = TextPreprocessor.cleanText(e.getFullText());
            for (String p : spamPatterns) if (RabinKarp.search(text, TextPreprocessor.cleanText(p))) rkMatches++;
        }
        rkTime = (System.nanoTime() - start) / 1_000_000.0;

        // Z-Algo
        start = System.nanoTime();
        int zMatches = 0;
        for (Email e : userMessages) {
            String text = TextPreprocessor.cleanText(e.getFullText());
            for (String p : spamPatterns) if (ZAlgorithm.search(text, TextPreprocessor.cleanText(p))) zMatches++;
        }
        zTime = (System.nanoTime() - start) / 1_000_000.0;

        // Aho-Corasick
        start = System.nanoTime();
        AhoCorasick aho = new AhoCorasick(spamPatterns);
        int ahoMatches = 0;
        Set<String> detected = new LinkedHashSet<>();
        for (Email e : userMessages) {
            List<String> found = aho.search(TextPreprocessor.cleanText(e.getFullText()));
            ahoMatches += found.size();
            detected.addAll(found);
        }
        ahoTime = (System.nanoTime() - start) / 1_000_000.0;

        // Bitmask DP
        start = System.nanoTime();
        int n = Math.min(20, detected.size());
        int[] scores = new int[n];
        int idx = 0;
        for (String kw : detected) { if (idx >= n) break; scores[idx++] = Math.max(1, kw.length()); }
        int bestScore = (n > 0) ? BitmaskDP.findBestScore(scores, Math.max(1, n / 2)) : 0;
        dpTime = (System.nanoTime() - start) / 1_000_000.0;

        // Edmonds-Karp
        start = System.nanoTime();
        int nodes = Math.min(12, Math.max(6, userMessages.size() + 2));
        int[][] capacity = new int[nodes][nodes];
        for (int i = 1; i < nodes - 1; i++) { capacity[0][i] = 5 + (i % 5); capacity[i][nodes - 1] = 3 + (i % 4); }
        int maxFlow = new EdmondsKarp(capacity).maxFlow(0, nodes - 1);
        ekTime = (System.nanoTime() - start) / 1_000_000.0;

        // Set Cover
        start = System.nanoTime();
        Map<String, Set<String>> ruleMap = new LinkedHashMap<>();
        Set<String> req = new LinkedHashSet<>();
        for (int i = 0; i < spamPatterns.length; i++) req.add("F" + i);
        int rNum = 1;
        for (int i = 0; i < spamPatterns.length; i += 5) {
            Set<String> cov = new LinkedHashSet<>();
            for (int j = i; j < Math.min(i + 5, spamPatterns.length); j++) cov.add("F" + j);
            ruleMap.put("Rule-" + (rNum++), cov);
        }
        List<String> selRules = SetCoverApproximation.selectRules(ruleMap, req);
        scTime = (System.nanoTime() - start) / 1_000_000.0;

        // Randomized Hashing
        start = System.nanoTime();
        RandomizedHash hasher = new RandomizedHash();
        long hashXor = 0;
        for (Email e : userMessages) hashXor ^= hasher.getHash(TextPreprocessor.cleanText(e.getFullText()));
        rhTime = (System.nanoTime() - start) / 1_000_000.0;

        // Parallel Processing
        start = System.nanoTime();
        int procs = Runtime.getRuntime().availableProcessors();
        ParallelEmailProcessor.processEmails(userMessages);
        ppTime = (System.nanoTime() - start) / 1_000_000.0;

        String response = String.format("{"
                + "\"user\":\"%s\","
                + "\"messagesTested\":%d,"
                + "\"patternsTested\":%d,"
                + "\"keywordsFound\":%s,"
                + "\"algorithms\":["
                + "{\"name\":\"KMP\",\"category\":\"String Matching\",\"complexity\":\"O(N + M)\",\"timeMs\":%.3f,\"result\":\"%d matches\"},"
                + "{\"name\":\"Rabin-Karp\",\"category\":\"String Matching\",\"complexity\":\"O(N + M) avg\",\"timeMs\":%.3f,\"result\":\"%d matches\"},"
                + "{\"name\":\"Z-Algorithm\",\"category\":\"String Matching\",\"complexity\":\"O(N + M)\",\"timeMs\":%.3f,\"result\":\"%d matches\"},"
                + "{\"name\":\"Aho-Corasick\",\"category\":\"Multi-Pattern Search\",\"complexity\":\"O(N + sum(M))\",\"timeMs\":%.3f,\"result\":\"%d matches\"},"
                + "{\"name\":\"Bitmask DP\",\"category\":\"Dynamic Programming\",\"complexity\":\"O(2^N * N)\",\"timeMs\":%.3f,\"result\":\"Optimal score = %d\"},"
                + "{\"name\":\"Edmonds-Karp\",\"category\":\"Network Flow\",\"complexity\":\"O(V * E^2)\",\"timeMs\":%.3f,\"result\":\"Max Flow = %d\"},"
                + "{\"name\":\"Greedy Set Cover\",\"category\":\"Approximation\",\"complexity\":\"O(|U| * |S|)\",\"timeMs\":%.3f,\"result\":\"Selected %d rules\"},"
                + "{\"name\":\"Randomized Hashing\",\"category\":\"Randomized Algorithm\",\"complexity\":\"O(N)\",\"timeMs\":%.3f,\"result\":\"Hashed %d emails\"},"
                + "{\"name\":\"Parallel Processing\",\"category\":\"Concurrency\",\"complexity\":\"O(N / P)\",\"timeMs\":%.3f,\"result\":\"Executed on %d threads\"}"
                + "],"
                + "\"fastestStringMatcher\":\"Aho-Corasick\","
                + "\"fastestAlgorithm\":\"%s\""
                + "}",
                escapeJson(user), userMessages.size(), spamPatterns.length, toJsonArray(new ArrayList<>(detected)),
                kmpTime, kmpMatches,
                rkTime, rkMatches,
                zTime, zMatches,
                ahoTime, ahoMatches,
                dpTime, bestScore,
                ekTime, maxFlow,
                scTime, selRules.size(),
                rhTime, userMessages.size(),
                ppTime, procs,
                (ekTime < ahoTime ? "Edmonds-Karp" : "Aho-Corasick"));

        sendJsonResponse(exchange, 200, response);
    }

    private static void handleCompose(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String user = extractJsonField(requestBody, "user");
        String subject = extractJsonField(requestBody, "subject");
        String body = extractJsonField(requestBody, "body");

        if (user.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Missing required fields: user, subject, body\"}");
            return;
        }

        List<Email> allEmails = EmailReader.readEmails(EMAIL_FILE);
        int nextId = EmailReader.getNextMessageId(allEmails, user);
        EmailReader.addMessage(EMAIL_FILE, user, subject, body, nextId);

        sendJsonResponse(exchange, 200, String.format("{\"success\":true,\"messageId\":%d,\"user\":\"%s\"}", nextId, escapeJson(user)));
    }

    // --- Static File Handler ---

    private static void handleStaticFile(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) path = "/index.html";

        File file = new File(WEB_DIR, path.substring(1));
        if (!file.exists() || file.isDirectory()) {
            sendResponse(exchange, 404, "404 Not Found");
            return;
        }

        String mime = getMimeType(file.getName());
        byte[] bytes = new FileInputStream(file).readAllBytes();
        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // --- Helpers ---

    private static void sendJsonResponse(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void sendResponse(HttpExchange exchange, int status, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String getMimeType(String filename) {
        if (filename.endsWith(".html")) return "text/html; charset=UTF-8";
        if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
        if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".svg")) return "image/svg+xml";
        return "text/plain";
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2) {
                map.put(URLDecoder.decode(pair[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String extractJsonField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1);
        return "";
    }

    private static String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(list.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private static int calculateNetworkFlow(List<String> detectedFeatures) {
        int[][] capacity = new int[6][6];
        for (int i = 1; i <= 4; i++) {
            capacity[0][i] = 5;
            capacity[i][5] = 5;
        }
        for (String f : detectedFeatures) {
            if (f.equals("bank details")) capacity[0][1]++;
            else if (f.equals("password") || f.equals("account verification")) capacity[0][2]++;
            else if (f.equals("free prize") || f.equals("lottery") || f.equals("claim your reward")) capacity[0][3]++;
            else if (f.equals("click here") || f.equals("urgent")) capacity[0][4]++;
        }
        return new EdmondsKarp(capacity).maxFlow(0, 5);
    }

    private static Map<String, Set<String>> createSpamRules() {
        Map<String, Set<String>> r = new LinkedHashMap<>();
        r.put("Prize Scam Rule", new HashSet<>(Arrays.asList("free prize", "lottery", "claim your reward")));
        r.put("Account Scam Rule", new HashSet<>(Arrays.asList("password", "account verification", "urgent")));
        r.put("Financial Scam Rule", new HashSet<>(Arrays.asList("bank details", "claim your reward")));
        r.put("Click Scam Rule", new HashSet<>(Arrays.asList("click here", "urgent")));
        return r;
    }

    private static String detectCategory(List<String> matches) {
        if (matches == null || matches.isEmpty()) return "NOT SPAM";
        boolean prize = false, account = false, financial = false, promotional = false;
        for (String m : matches) {
            if (m.equals("free prize") || m.equals("lottery") || m.equals("claim your reward")) prize = true;
            if (m.equals("password") || m.equals("account verification") || m.equals("urgent")) account = true;
            if (m.equals("bank details") || m.equals("claim your reward")) financial = true;
            if (m.equals("click here") || m.equals("urgent")) promotional = true;
        }
        if (financial) return "FINANCIAL SCAM";
        if (prize) return "PRIZE SCAM";
        if (account) return "ACCOUNT SCAM";
        if (promotional) return "PROMOTIONAL / CLICK SPAM";
        return "GENERAL SPAM";
    }

    private static List<String> detectUrls(String text) {
        List<String> urls = new ArrayList<>();
        if (text == null || text.isEmpty()) return urls;
        Matcher matcher = Pattern.compile("(https?://[^\\s]+|www\\.[^\\s]+)", Pattern.CASE_INSENSITIVE).matcher(text);
        while (matcher.find()) {
            String u = matcher.group().replaceAll("[.,!?;:]+$", "");
            if (!urls.contains(u)) urls.add(u);
        }
        return urls;
    }

    private static List<String> detectSuspiciousUrls(List<String> urls) {
        List<String> suspicious = new ArrayList<>();
        String[] keywords = {"login", "verify", "verification", "password", "account", "bank", "reward", "prize", "free", "claim", "urgent", "secure"};
        for (String u : urls) {
            String lower = u.toLowerCase();
            for (String kw : keywords) {
                if (lower.contains(kw)) { suspicious.add(u); break; }
            }
        }
        return suspicious;
    }
}
