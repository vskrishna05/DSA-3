import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Main {

    private static final String EMAIL_FILE = "data/emails.txt";
    private static final String KEYWORD_FILE = "data/spam_keywords.txt";
    private static final String REPORT_FILE = "data/spam_analysis_report.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<String> rawKeywords = EmailReader.readSpamKeywords(KEYWORD_FILE);
        Set<String> uniqueKeywords = new LinkedHashSet<>();
        for (String kw : rawKeywords) {
            String clean = TextPreprocessor.cleanText(kw);
            if (!clean.isEmpty()) uniqueKeywords.add(clean);
        }
        String[] spamPatterns = uniqueKeywords.toArray(new String[0]);
        AhoCorasick ahoCorasick = new AhoCorasick(spamPatterns);
        Map<String, Set<String>> rules = createSpamRules();

        while (true) {
            List<Email> emails = EmailReader.readEmails(EMAIL_FILE);
            Map<String, Integer> accounts = EmailReader.getEmailAccountsSummary(emails);

            printBanner();
            System.out.println("Total Emails: " + emails.size() + " across " + accounts.size() + " accounts\n");
            System.out.print("Enter Email ID: ");

            if (!scanner.hasNextLine()) break;
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            if (input.equalsIgnoreCase("0") || input.equalsIgnoreCase("exit")) {
                System.out.println("\nExiting Email Spam Analysis System. Goodbye!");
                break;
            }

            if (input.equalsIgnoreCase("LIST") || input.equalsIgnoreCase("L")) {
                displayAccountList(accounts);
                continue;
            }

            String emailId = input;
            List<Email> userMessages = EmailReader.getMessagesForEmail(emails, emailId);
            if (userMessages.isEmpty()) {
                System.out.println("\nEmail ID '" + emailId + "' not found. Please try again.");
                continue;
            }

            handleUserMenu(scanner, emails, userMessages, emailId, spamPatterns, ahoCorasick, rules);
        }
        scanner.close();
    }

    private static void handleUserMenu(Scanner scanner, List<Email> emails, List<Email> userMessages,
                                       String emailId, String[] spamPatterns, AhoCorasick ahoCorasick,
                                       Map<String, Set<String>> rules) {
        while (true) {
            System.out.println("\n==================================================");
            System.out.printf("USER: %s (%d messages)%n", emailId, userMessages.size());
            System.out.println("==================================================");
            System.out.println("1. Compose & Analyze New Message");
            System.out.println("2. Display Existing Messages");
            System.out.println("3. Run Full Spam Analysis");
            System.out.println("4. Algorithm Comparison");
            System.out.println("0. Back to Main Menu");
            System.out.println("--------------------------------------------------");
            System.out.print("Enter your choice: ");

            if (!scanner.hasNextLine()) break;
            String choice = scanner.nextLine().trim();
            if (choice.isEmpty()) continue;

            if (choice.equals("0")) {
                break;
            } else if (choice.equals("1")) {
                userMessages = composeNewMessage(scanner, emailId);
                if (!userMessages.isEmpty()) {
                    Email latest = userMessages.get(userMessages.size() - 1);
                    System.out.println("\nRunning spam inspection on new message...");
                    analyzeEmail(latest, spamPatterns, ahoCorasick, rules);
                }
            } else if (choice.equals("2")) {
                displayMessages(userMessages);
            } else if (choice.equals("3")) {
                analyzeAllMessages(userMessages, spamPatterns, ahoCorasick, rules);
            } else if (choice.equals("4")) {
                AlgorithmComparison.compareAll(userMessages, spamPatterns);
            } else {
                System.out.println("Invalid choice. Please enter 0, 1, 2, 3, or 4.");
            }
        }
    }

    private static List<Email> composeNewMessage(Scanner scanner, String emailId) {
        System.out.println("\n--- Compose New Message ---");
        System.out.print("Enter Subject: ");
        if (!scanner.hasNextLine()) return Collections.emptyList();
        String subject = scanner.nextLine().trim();
        System.out.print("Enter Body   : ");
        if (!scanner.hasNextLine()) return Collections.emptyList();
        String body = scanner.nextLine().trim();

        List<Email> allEmails = EmailReader.readEmails(EMAIL_FILE);
        int nextId = EmailReader.getNextMessageId(allEmails, emailId);
        EmailReader.addMessage(EMAIL_FILE, emailId, subject, body, nextId);

        allEmails = EmailReader.readEmails(EMAIL_FILE);
        return EmailReader.getMessagesForEmail(allEmails, emailId);
    }

    private static void displayMessages(List<Email> messages) {
        System.out.println("\n--- Stored Messages (" + messages.size() + ") ---");
        for (Email e : messages) {
            System.out.println("Message ID: " + e.getMessageId());
            System.out.println("Subject   : " + e.getSubject());
            System.out.println("Content   : " + e.getBody());
            System.out.println("--------------------------------------------------");
        }
    }

    private static void analyzeAllMessages(List<Email> messages, String[] spamPatterns,
                                          AhoCorasick ahoCorasick, Map<String, Set<String>> rules) {
        new File(REPORT_FILE).delete();

        int spamCount = 0, notSpamCount = 0;
        List<Integer> spamIds = new ArrayList<>(), notSpamIds = new ArrayList<>();
        Map<String, Integer> categoryCount = new LinkedHashMap<>();
        Map<String, Integer> keywordFreq = new HashMap<>();

        for (Email email : messages) {
            boolean isSpam = analyzeEmail(email, spamPatterns, ahoCorasick, rules);
            if (isSpam) {
                spamCount++;
                spamIds.add(email.getMessageId());
            } else {
                notSpamCount++;
                notSpamIds.add(email.getMessageId());
            }

            String text = TextPreprocessor.cleanText(email.getFullText());
            List<String> matches = getCombinedMatches(text, spamPatterns, ahoCorasick);
            String cat = detectCategory(matches);
            categoryCount.put(cat, categoryCount.getOrDefault(cat, 0) + 1);
            for (String kw : matches) keywordFreq.put(kw, keywordFreq.getOrDefault(kw, 0) + 1);
        }

        ReportGenerator.generateSummary(messages, spamCount, notSpamCount);
        System.out.println("Spam Message IDs     : " + spamIds);
        System.out.println("Not Spam Message IDs : " + notSpamIds);

        displayStatistics(messages.size(), spamCount, notSpamCount);
        displayCategoryStatistics(categoryCount);
        displayTopKeywords(keywordFreq);
        exportReport(messages, spamPatterns, ahoCorasick);

        ParallelEmailProcessor.processEmails(messages);
        System.out.println("Email analysis completed.");
    }

    private static boolean analyzeEmail(Email email, String[] spamPatterns,
                                        AhoCorasick ahoCorasick, Map<String, Set<String>> rules) {
        String text = TextPreprocessor.cleanText(email.getFullText());

        System.out.println("\n--------------------------------------------------");
        System.out.printf("Message #%d [%s]%n", email.getMessageId(), email.getEmailId());
        System.out.println("Subject: " + email.getSubject());
        System.out.println("Body   : " + email.getBody());
        System.out.println("--------------------------------------------------");

        // 1. KMP
        List<String> kmpMatches = new ArrayList<>();
        for (String p : spamPatterns) if (KMP.search(text, p)) kmpMatches.add(p);
        System.out.printf("1. KMP Search          : %d matches %s%n", kmpMatches.size(), kmpMatches);

        // 2. Rabin-Karp
        List<String> rkMatches = new ArrayList<>();
        for (String p : spamPatterns) if (RabinKarp.search(text, p)) rkMatches.add(p);
        System.out.printf("2. Rabin-Karp Search   : %d matches %s%n", rkMatches.size(), rkMatches);

        // 3. Z-Algorithm
        List<String> zMatches = new ArrayList<>();
        for (String p : spamPatterns) if (ZAlgorithm.search(text, p)) zMatches.add(p);
        System.out.printf("3. Z-Algorithm Search  : %d matches %s%n", zMatches.size(), zMatches);

        // 4. Aho-Corasick
        List<String> ahoMatches = ahoCorasick.search(text);
        System.out.printf("4. Aho-Corasick Search : %d matches %s%n", ahoMatches.size(), ahoMatches);

        Set<String> allDetected = new LinkedHashSet<>();
        allDetected.addAll(kmpMatches);
        allDetected.addAll(rkMatches);
        allDetected.addAll(zMatches);
        allDetected.addAll(ahoMatches);
        List<String> combined = new ArrayList<>(allDetected);

        // 5. Bitmask DP
        int featureCount = Math.min(3, combined.size());
        int[] scores = new int[featureCount];
        Arrays.fill(scores, 4);
        int bestDPScore = (scores.length > 0) ? BitmaskDP.findBestScore(scores, scores.length) : 0;
        System.out.printf("5. Bitmask DP Score    : %d (Evaluated %d features)%n", bestDPScore, combined.size());

        // 6. Edmonds-Karp
        int networkFlow = calculateNetworkFlow(combined);
        System.out.printf("6. Edmonds-Karp Flow   : %d max flow capacity%n", networkFlow);

        // 7. Greedy Set Cover
        List<String> selectedRules = SetCoverApproximation.selectRules(rules, new HashSet<>(combined));
        System.out.printf("7. Greedy Set Cover    : %d rules selected %s%n", selectedRules.size(), selectedRules);

        // 8. Randomized Hashing
        int randHash = new RandomizedHash().getHash(text);
        System.out.printf("8. Randomized Hash     : 0x%08X%n", randHash);

        String category = detectCategory(combined);
        double confidence = calculateConfidence(bestDPScore);
        String riskLevel = getRiskLevel(bestDPScore);
        List<String> urls = detectUrls(email.getFullText());
        List<String> suspiciousUrls = detectSuspiciousUrls(urls);
        boolean isSpam = SpamAnalyzer.isSpam(bestDPScore);

        System.out.printf("Category  : %-20s | Risk Level: %s%n", category, riskLevel);
        System.out.printf("Confidence: %.2f%%             | URLs Found: %d (%d suspicious)%n",
                confidence, urls.size(), suspiciousUrls.size());
        if (!suspiciousUrls.isEmpty()) System.out.println("Suspicious URLs: " + suspiciousUrls);

        System.out.printf("VERDICT   : [%s] (Spam Score: %d / Threshold: 8)%n",
                SpamAnalyzer.getResult(bestDPScore), bestDPScore);
        System.out.println("--------------------------------------------------");

        return isSpam;
    }

    private static List<String> getCombinedMatches(String text, String[] spamPatterns, AhoCorasick ahoCorasick) {
        Set<String> matches = new LinkedHashSet<>();
        for (String p : spamPatterns) {
            if (KMP.search(text, p) || RabinKarp.search(text, p) || ZAlgorithm.search(text, p)) matches.add(p);
        }
        matches.addAll(ahoCorasick.search(text));
        return new ArrayList<>(matches);
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
        Map<String, Set<String>> rules = new LinkedHashMap<>();
        rules.put("Prize Scam Rule", new HashSet<>(Arrays.asList("free prize", "lottery", "claim your reward")));
        rules.put("Account Scam Rule", new HashSet<>(Arrays.asList("password", "account verification", "urgent")));
        rules.put("Financial Scam Rule", new HashSet<>(Arrays.asList("bank details", "claim your reward")));
        rules.put("Click Scam Rule", new HashSet<>(Arrays.asList("click here", "urgent")));
        return rules;
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

    private static double calculateConfidence(int score) {
        return Math.min(100.0, Math.max(0.0, (score / 12.0) * 100.0));
    }

    private static String getRiskLevel(int score) {
        if (score >= 8) return "HIGH";
        if (score >= 4) return "MEDIUM";
        return "LOW";
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

    private static void displayStatistics(int total, int spam, int notSpam) {
        System.out.println("\nStatistical Breakdown:");
        System.out.printf("Total Messages : %d%n", total);
        System.out.printf("Spam Messages  : %d (%.2f%%)%n", spam, total > 0 ? (spam * 100.0) / total : 0.0);
        System.out.printf("Legitimate     : %d%n", notSpam);
    }

    private static void displayCategoryStatistics(Map<String, Integer> categoryCount) {
        System.out.println("\nCategory Distribution:");
        if (categoryCount.isEmpty()) { System.out.println("No category data."); return; }
        categoryCount.forEach((cat, cnt) -> System.out.printf("- %-26s : %d%n", cat, cnt));
    }

    private static void displayTopKeywords(Map<String, Integer> keywordFreq) {
        System.out.println("\nTop Detected Spam Keywords:");
        if (keywordFreq.isEmpty()) { System.out.println("No keywords detected."); return; }
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(keywordFreq.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        int limit = Math.min(10, entries.size());
        for (int i = 0; i < limit; i++) {
            System.out.printf("%2d. %-22s -> %d occurrences%n", i + 1, entries.get(i).getKey(), entries.get(i).getValue());
        }
    }

    private static void exportReport(List<Email> messages, String[] spamPatterns, AhoCorasick ahoCorasick) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(REPORT_FILE))) {
            writer.println("==============================================");
            writer.println("        EMAIL SPAM ANALYSIS REPORT");
            writer.println("==============================================\n");

            int spam = 0, notSpam = 0;
            for (Email email : messages) {
                String text = TextPreprocessor.cleanText(email.getFullText());
                List<String> matches = getCombinedMatches(text, spamPatterns, ahoCorasick);
                int score = Math.min(3, matches.size()) * 4;
                boolean isSpam = SpamAnalyzer.isSpam(score);
                if (isSpam) spam++; else notSpam++;

                writer.println("----------------------------------------------");
                writer.println("EMAIL : " + email.getEmailId());
                writer.println("Message ID : " + email.getMessageId());
                writer.println("Subject : " + email.getSubject());
                writer.println("Message : " + email.getBody() + "\n");
                writer.println("Classification : " + SpamAnalyzer.getResult(score));
                writer.println("Category : " + detectCategory(matches));
                writer.printf("Confidence : %.2f%%%n", calculateConfidence(score));
                writer.println("Risk Level : " + getRiskLevel(score));
                writer.println("Spam Score : " + score);
                writer.println("Keywords : " + matches);
                writer.println("URLs : " + detectUrls(email.getFullText()));
                writer.println("Suspicious URLs : " + detectSuspiciousUrls(detectUrls(email.getFullText())) + "\n");
            }

            writer.println("==============================================");
            writer.println("SUMMARY");
            writer.println("==============================================");
            writer.printf("Total Messages : %d | Spam : %d | Not Spam : %d%n", messages.size(), spam, notSpam);
            writer.printf("Spam Percentage : %.2f%%%n", messages.isEmpty() ? 0.0 : (spam * 100.0) / messages.size());
            System.out.println("Analysis report saved to: " + REPORT_FILE);
        } catch (IOException e) {
            System.out.println("Error exporting report: " + e.getMessage());
        }
    }

    private static void displayAccountList(Map<String, Integer> accounts) {
        System.out.println("\nAvailable Accounts:");
        int count = 0;
        for (Map.Entry<String, Integer> entry : accounts.entrySet()) {
            System.out.printf("  [%2d] %-32s (%d messages)%n", ++count, entry.getKey(), entry.getValue());
            if (count >= 20) {
                System.out.printf("  ... and %d more accounts.%n", accounts.size() - count);
                break;
            }
        }
    }

    private static void printBanner() {
        System.out.println("\n==================================================");
        System.out.println("           EMAIL SPAM ANALYSIS SYSTEM");
        System.out.println("==================================================");
    }
}