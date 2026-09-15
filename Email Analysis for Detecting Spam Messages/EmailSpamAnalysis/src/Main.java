import java.util.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        String filePath = "data/emails.txt";
        String keywordPath = "data/spam_keywords.txt";

        Scanner scanner = new Scanner(System.in);

        // ==========================================
        // LOAD SPAM KEYWORDS AND ANALYSIS DATA
        // ==========================================

        List<String> keywordList =
                EmailReader.readSpamKeywords(keywordPath);

        String[] spamPatterns =
                keywordList.toArray(new String[0]);

        int[] featureScores =
                new int[spamPatterns.length];

        Arrays.fill(featureScores, 4);

        AhoCorasick ahoCorasick =
                new AhoCorasick(spamPatterns);

        Map<String, Set<String>> rules =
                createSpamRules();

        // ==========================================
        // MAIN MENU
        // ==========================================

        while (true) {

            List<Email> emails =
                    EmailReader.readEmails(filePath);

            System.out.println();
            System.out.println("==============================================");
            System.out.println("        EMAIL SPAM ANALYSIS SYSTEM");
            System.out.println("==============================================");

            System.out.print("Enter Email ID: ");

            String emailId =
                    scanner.nextLine().trim();

            // ==========================================
            // EXIT PROGRAM
            // ==========================================

            if (emailId.equalsIgnoreCase("0")) {

                System.out.println();
                System.out.println(
                        "Thank you for using Email Spam Analysis System."
                );

                scanner.close();
                return;
            }

            // ==========================================
            // FIND EMAIL ID
            // ==========================================

            List<Email> userMessages =
                    EmailReader.getMessagesForEmail(
                            emails,
                            emailId
                    );

            if (userMessages.isEmpty()) {

                System.out.println();
                System.out.println("Email ID not found.");
                System.out.println(
                        "Please enter an existing Email ID."
                );

                continue;
            }

            // ==========================================
            // EMAIL MENU
            // ==========================================

            while (true) {

                System.out.println();
                System.out.println("----------------------------------------------");
                System.out.println(
                        "EMAIL: " + emailId
                );
                System.out.println("----------------------------------------------");

                System.out.println("1. Enter New Message");
                System.out.println("2. Display Existing Messages");
                System.out.println("3. Check Existing Messages");
                System.out.println("0. Exit to Main Menu");

                System.out.print("Enter your choice : ");

                String choice =
                        scanner.nextLine().trim();

                // ======================================
                // EXIT TO MAIN MENU
                // ======================================

                if (choice.equals("0")) {
                    break;
                }

                // ======================================
                // ENTER NEW MESSAGE
                // ======================================

                if (choice.equals("1")) {

                    System.out.println();
                    System.out.println("----------------------------------------------");
                    System.out.println("ENTER NEW MESSAGE");
                    System.out.println("----------------------------------------------");

                    System.out.print("Enter Subject : ");

                    String subject =
                            scanner.nextLine().trim();

                    System.out.print("Enter Message : ");

                    String body =
                            scanner.nextLine().trim();

                    int nextMessageId =
                            EmailReader.getNextMessageId(
                                    emails,
                                    emailId
                            );

                    EmailReader.addMessage(
                            filePath,
                            emailId,
                            subject,
                            body,
                            nextMessageId
                    );

                    emails =
                            EmailReader.readEmails(filePath);

                    userMessages =
                            EmailReader.getMessagesForEmail(
                                    emails,
                                    emailId
                            );

                    System.out.println();
                    System.out.println("----------------------------------------------");
                    System.out.println("MESSAGE STORED SUCCESSFULLY");
                    System.out.println("----------------------------------------------");

                    System.out.println(
                            "Message ID : " + nextMessageId
                    );

                    System.out.println(
                            "Total Messages : "
                                    + userMessages.size()
                    );

                    /*
                     * NEW FEATURE:
                     * Automatically analyse the newly added
                     * message instead of requiring another menu.
                     */

                    System.out.println();
                    System.out.println(
                            "Running automatic spam analysis..."
                    );

                    Email newEmail = null;

                    for (Email email : userMessages) {

                        if (email.getMessageId() == nextMessageId) {
                            newEmail = email;
                            break;
                        }
                    }

                    if (newEmail != null) {

                        analyzeEmail(
                                newEmail,
                                spamPatterns,
                                featureScores,
                                ahoCorasick,
                                rules
                        );
                    }

                    break;
                }

                // ======================================
                // DISPLAY EXISTING MESSAGES
                // ======================================

                else if (choice.equals("2")) {

                    displayMessages(userMessages);

                    while (true) {

                        System.out.println();
                        System.out.println("----------------------------------------------");
                        System.out.println("1. Check Messages");
                        System.out.println("2. Return to Email Menu");
                        System.out.println("----------------------------------------------");

                        System.out.print(
                                "Enter your choice : "
                        );

                        String displayChoice =
                                scanner.nextLine().trim();

                        // ==================================
                        // CHECK MESSAGES
                        // ==================================

                        if (displayChoice.equals("1")) {

                            analyzeAllMessages(
                                    userMessages,
                                    spamPatterns,
                                    featureScores,
                                    ahoCorasick,
                                    rules
                            );

                            break;
                        }

                        // ==================================
                        // RETURN
                        // ==================================

                        else if (displayChoice.equals("2")) {
                            break;
                        }

                        else {
                            System.out.println(
                                    "Invalid choice."
                            );
                        }
                    }
                }

                // ======================================
                // CHECK EXISTING MESSAGES
                // ======================================

                else if (choice.equals("3")) {

                    System.out.println();
                    System.out.println("----------------------------------------------");
                    System.out.println("NOTICE");
                    System.out.println("----------------------------------------------");
                    System.out.println("Existing messages will be analyzed.");
                    System.out.println("The previous analysis report will be replaced.");

                    analyzeAllMessages(
                            userMessages,
                            spamPatterns,
                            featureScores,
                            ahoCorasick,
                            rules
                    );
                }

                // ======================================
                // INVALID OPTION
                // ======================================

                else {

                    System.out.println();
                    System.out.println(
                            "Invalid choice."
                    );
                }
            }
        }
    }

    // ==========================================
    // DISPLAY MESSAGES
    // ==========================================

    private static void displayMessages(
            List<Email> messages) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("             EXISTING MESSAGES");
        System.out.println("==============================================");

        if (messages.isEmpty()) {

            System.out.println("No messages found.");
            return;
        }

        System.out.println(
                "Email ID : "
                        + messages.get(0).getEmailId()
        );

        System.out.println(
                "Total Messages : "
                        + messages.size()
        );

        for (Email email : messages) {

            System.out.println();
            System.out.println("----------------------------------------------");

            System.out.println(
                    "Message " + email.getMessageId()
            );

            System.out.println(
                    "Subject : "
                            + email.getSubject()
            );

            System.out.println(
                    "Message : "
                            + email.getBody()
            );
        }

        System.out.println(
                "----------------------------------------------"
        );
    }

    // ==========================================
    // ANALYZE ALL MESSAGES
    // ==========================================

    private static void analyzeAllMessages(
            List<Email> messages,
            String[] spamPatterns,
            int[] featureScores,
            AhoCorasick ahoCorasick,
            Map<String, Set<String>> rules) {

        // Clear the previous report before generating a fresh report.
        String oldReportPath = "data/spam_analysis_report.txt";
        File oldReport = new File(oldReportPath);
        if (oldReport.exists()) {
            if (oldReport.delete()) {
                System.out.println("Previous analysis report cleared.");
            } else {
                System.out.println("Warning: Could not clear previous analysis report.");
            }
        }

        int spamCount = 0;
        int notSpamCount = 0;

        List<Integer> spamMessageIds =
                new ArrayList<>();

        List<Integer> notSpamMessageIds =
                new ArrayList<>();

        // NEW FEATURE DATA

        Map<String, Integer> categoryCount =
                new LinkedHashMap<>();

        Map<String, Integer> keywordFrequency =
                new HashMap<>();

        for (Email email : messages) {

            boolean isSpam =
                    analyzeEmail(
                            email,
                            spamPatterns,
                            featureScores,
                            ahoCorasick,
                            rules
                    );

            if (isSpam) {

                spamCount++;

                spamMessageIds.add(
                        email.getMessageId()
                );

            } else {

                notSpamCount++;

                notSpamMessageIds.add(
                        email.getMessageId()
                );
            }

            // Collect statistics

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            List<String> matches =
                    ahoCorasick.search(text);

            String category =
                    detectCategory(matches);

            categoryCount.put(
                    category,
                    categoryCount.getOrDefault(category, 0) + 1
            );

            for (String keyword : matches) {

                keywordFrequency.put(
                        keyword,
                        keywordFrequency.getOrDefault(keyword, 0) + 1
                );
            }
        }

        // ==========================================
        // FINAL EMAIL REPORT
        // ==========================================

        System.out.println();
        System.out.println();

        ReportGenerator.generateSummary(
                messages,
                spamCount,
                notSpamCount
        );

        // ==========================================
        // MESSAGE IDs
        // ==========================================

        System.out.println();

        System.out.println(
                "Spam Message IDs     : "
                        + getMessageIdsText(
                                spamMessageIds
                        )
        );

        System.out.println(
                "Not Spam Message IDs : "
                        + getMessageIdsText(
                                notSpamMessageIds
                        )
        );

        // ==========================================
        // NEW FEATURE 1 - STATISTICS
        // ==========================================

        displayStatistics(
                messages.size(),
                spamCount,
                notSpamCount
        );

        // ==========================================
        // NEW FEATURE 2 - CATEGORY DISTRIBUTION
        // ==========================================

        displayCategoryStatistics(categoryCount);

        // ==========================================
        // NEW FEATURE 3 - TOP SPAM KEYWORDS
        // ==========================================

        displayTopKeywords(keywordFrequency);

        // ==========================================
        // NEW FEATURE 4 - AUTOMATIC REPORT EXPORT
        // ==========================================

        exportReport(
                messages,
                spamPatterns,
                ahoCorasick
        );

        System.out.println();
        System.out.println(
                "=============================================="
        );

        // ==========================================
        // PARALLEL PROCESSING
        // ==========================================

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("PARALLEL PROCESSING");
        System.out.println("----------------------------------------------");

        ParallelEmailProcessor.processEmails(
                messages
        );

        // ==========================================
        // ANALYSIS COMPLETED
        // ==========================================

        System.out.println();
        System.out.println("==============================================");
        System.out.println("             ANALYSIS COMPLETED");
        System.out.println("==============================================");
    }

    // ==========================================
    // EMAIL ANALYSIS
    // ==========================================

    private static boolean analyzeEmail(
            Email email,
            String[] spamPatterns,
            int[] featureScores,
            AhoCorasick ahoCorasick,
            Map<String, Set<String>> rules) {

        String text =
                TextPreprocessor.cleanText(
                        email.getFullText()
                );

        // ======================================
        // EMAIL DETAILS
        // ======================================

        System.out.println();
        System.out.println();
        System.out.println("==============================================");

        System.out.println(
                "EMAIL " + email.getEmailId()
        );

        System.out.println("==============================================");

        System.out.println(
                "Message ID : "
                        + email.getMessageId()
        );

        System.out.println(
                "Subject : "
                        + email.getSubject()
        );

        System.out.println();
        System.out.println("Email Text :");
        System.out.println(email.getBody());

        // ======================================
        // 1. KMP
        // ======================================

        List<String> kmpMatches =
                new ArrayList<>();

        for (String pattern : spamPatterns) {

            if (KMP.search(text, pattern)) {
                kmpMatches.add(pattern);
            }
        }

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("1. KMP");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Match Count : "
                        + kmpMatches.size()
        );

        System.out.println(
                "Matched Patterns : "
                        + getMatchesText(kmpMatches)
        );

        // ======================================
        // 2. RABIN-KARP
        // ======================================

        List<String> rabinKarpMatches =
                new ArrayList<>();

        for (String pattern : spamPatterns) {

            if (RabinKarp.search(text, pattern)) {
                rabinKarpMatches.add(pattern);
            }
        }

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("2. RABIN-KARP");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Match Count : "
                        + rabinKarpMatches.size()
        );

        System.out.println(
                "Matched Patterns : "
                        + getMatchesText(rabinKarpMatches)
        );

        // ======================================
        // 3. Z-ALGORITHM
        // ======================================

        List<String> zMatches =
                new ArrayList<>();

        for (String pattern : spamPatterns) {

            if (ZAlgorithm.search(text, pattern)) {
                zMatches.add(pattern);
            }
        }

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("3. Z-ALGORITHM");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Match Count : "
                        + zMatches.size()
        );

        System.out.println(
                "Matched Patterns : "
                        + getMatchesText(zMatches)
        );

        // ======================================
        // 4. AHO-CORASICK
        // ======================================

        List<String> ahoMatches =
                ahoCorasick.search(text);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("4. AHO-CORASICK");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Match Count : "
                        + ahoMatches.size()
        );

        System.out.println(
                "Matched Patterns : "
                        + getMatchesText(ahoMatches)
        );

        // ======================================
        // 5. BITMASK DP
        // ======================================

        int[] detectedScores =
                new int[ahoMatches.size()];

        for (int i = 0;
                i < ahoMatches.size();
                i++) {

            for (int j = 0;
                    j < spamPatterns.length;
                    j++) {

                if (ahoMatches.get(i)
                        .equals(spamPatterns[j])) {

                    if (j < featureScores.length) {

                        detectedScores[i] =
                                featureScores[j];

                    } else {

                        detectedScores[i] = 4;
                    }

                    break;
                }
            }
        }

        int bestDPScore = 0;

        if (detectedScores.length > 0) {

            int limit =
                    Math.min(
                            3,
                            detectedScores.length
                    );

            bestDPScore =
                    BitmaskDP.findBestScore(
                            detectedScores,
                            limit
                    );
        }

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("5. BITMASK DP");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Best Feature Score : "
                        + bestDPScore
        );

        System.out.println(
                "Features Considered : "
                        + ahoMatches.size()
        );

        // ======================================
        // 6. EDMONDS-KARP
        // ======================================

        int networkFlow =
                calculateNetworkFlow(
                        ahoMatches
                );

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("6. EDMONDS-KARP");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Maximum Network Flow : "
                        + networkFlow
        );

        // ======================================
        // 7. GREEDY SET COVER
        // ======================================

        Set<String> requiredFeatures =
                new HashSet<>(ahoMatches);

        List<String> selectedRules =
                SetCoverApproximation.selectRules(
                        rules,
                        requiredFeatures
                );

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("7. GREEDY SET COVER");
        System.out.println("----------------------------------------------");

        if (selectedRules.isEmpty()) {

            System.out.println(
                    "Selected Rules : None"
            );

        } else {

            System.out.println(
                    "Selected Rules : "
                            + selectedRules
            );
        }

        System.out.println(
                "Rules Selected : "
                        + selectedRules.size()
        );

        // ======================================
        // 8. RANDOMIZED HASHING
        // ======================================

        RandomizedHash randomHasher =
                new RandomizedHash();

        int randomizedHash =
                randomHasher.getHash(text);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("8. RANDOMIZED HASHING");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Hash Value : "
                        + randomizedHash
        );

        // ======================================
        // 9. SPAM CATEGORY
        // ======================================

        String category =
                detectCategory(ahoMatches);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("9. SPAM CATEGORY");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Detected Category : "
                        + category
        );

        // ======================================
        // 10. CONFIDENCE SCORE
        // ======================================

        double confidence =
                calculateConfidence(
                        bestDPScore
                );

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("10. CONFIDENCE SCORE");
        System.out.println("----------------------------------------------");

        System.out.printf(
                "Confidence : %.2f%%%n",
                confidence
        );

        // ======================================
        // 11. RISK LEVEL
        // ======================================

        String riskLevel =
                getRiskLevel(bestDPScore);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("11. RISK LEVEL");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Risk Level : "
                        + riskLevel
        );

        // ======================================
        // 12. URL DETECTION
        // ======================================

        List<String> urls =
                detectUrls(email.getFullText());

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("12. URL DETECTION");
        System.out.println("----------------------------------------------");

        System.out.println(
                "URLs Found : "
                        + urls.size()
        );

        if (urls.isEmpty()) {

            System.out.println(
                    "URLs : None"
            );

        } else {

            for (String url : urls) {
                System.out.println(
                        "  - " + url
                );
            }
        }

        // ======================================
        // 13. SUSPICIOUS URL DETECTION
        // ======================================

        List<String> suspiciousUrls =
                detectSuspiciousUrls(urls);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("13. SUSPICIOUS URL DETECTION");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Suspicious URLs : "
                        + suspiciousUrls.size()
        );

        if (suspiciousUrls.isEmpty()) {

            System.out.println(
                    "Suspicious URLs : None"
            );

        } else {

            for (String url : suspiciousUrls) {

                System.out.println(
                        "  - " + url
                );
            }
        }

        // ======================================
        // 14. DETAILED ANALYSIS
        // ======================================

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("14. DETAILED ANALYSIS");
        System.out.println("----------------------------------------------");

        displayDetailedAnalysis(
                ahoMatches,
                category,
                confidence,
                riskLevel,
                urls,
                suspiciousUrls,
                networkFlow,
                selectedRules
        );

        // ======================================
        // FINAL CLASSIFICATION
        // ======================================

        String finalResult =
                SpamAnalyzer.getResult(
                        bestDPScore
                );

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("FINAL CLASSIFICATION");
        System.out.println("----------------------------------------------");

        System.out.println(
                "Result : " + finalResult
        );

        System.out.println();

        if (SpamAnalyzer.isSpam(bestDPScore)) {

            System.out.println("Why SPAM?");

            System.out.println(
                    "  - Suspicious patterns detected : "
                            + ahoMatches.size()
            );

            System.out.println(
                    "  - Detected patterns : "
                            + getMatchesText(ahoMatches)
            );

            System.out.println(
                    "  - Feature score : "
                            + bestDPScore
            );

            System.out.println(
                    "  - Spam threshold : 8"
            );

            System.out.println(
                    "  - Risk level : "
                            + riskLevel
            );

            System.out.printf(
                    "  - Confidence : %.2f%%%n",
                    confidence
            );

            if (!suspiciousUrls.isEmpty()) {

                System.out.println(
                        "  - Suspicious URLs detected."
                );
            }

            System.out.println(
                    "  - Score reached the spam threshold."
            );

        } else {

            System.out.println("Why NOT SPAM?");

            if (ahoMatches.isEmpty()) {

                System.out.println(
                        "  - No suspicious patterns detected."
                );

            } else {

                System.out.println(
                        "  - Suspicious pattern score is below threshold."
                );
            }

            System.out.println(
                    "  - Feature score : "
                            + bestDPScore
            );

            System.out.println(
                    "  - Spam threshold : 8"
            );

            System.out.printf(
                    "  - Confidence : %.2f%%%n",
                    confidence
            );

            System.out.println(
                    "  - Score did not reach the spam threshold."
            );
        }

        System.out.println(
                "=============================================="
        );

        return SpamAnalyzer.isSpam(
                bestDPScore
        );
    }

    // ==========================================
    // FEATURE 1 - CATEGORY DETECTION
    // ==========================================

    private static String detectCategory(
            List<String> matches) {

        if (matches.isEmpty()) {
            return "NOT SPAM";
        }

        for (String match : matches) {

            if (match.equals("free prize")
                    || match.equals("lottery")
                    || match.equals("claim your reward")) {

                return "PRIZE SCAM";
            }
        }

        for (String match : matches) {

            if (match.equals("password")
                    || match.equals("account verification")
                    || match.equals("urgent")) {

                return "ACCOUNT SCAM";
            }
        }

        for (String match : matches) {

            if (match.equals("bank details")
                    || match.equals("claim your reward")) {

                return "FINANCIAL SCAM";
            }
        }

        for (String match : matches) {

            if (match.equals("click here")
                    || match.equals("urgent")) {

                return "PROMOTIONAL / CLICK SPAM";
            }
        }

        return "GENERAL SPAM";
    }

    // ==========================================
    // FEATURE 2 - CONFIDENCE SCORE
    // ==========================================

    private static double calculateConfidence(
            int score) {

        /*
         * Maximum Bitmask DP score:
         * 3 selected features x 4 points = 12.
         */

        double confidence =
                (score / 12.0) * 100.0;

        if (confidence > 100) {
            confidence = 100;
        }

        if (confidence < 0) {
            confidence = 0;
        }

        return confidence;
    }

    // ==========================================
    // FEATURE 3 - RISK LEVEL
    // ==========================================

    private static String getRiskLevel(
            int score) {

        if (score >= 8) {
            return "HIGH";
        }

        if (score >= 4) {
            return "MEDIUM";
        }

        return "LOW";
    }

    // ==========================================
    // FEATURE 4 - URL DETECTION
    // ==========================================

    private static List<String> detectUrls(
            String text) {

        List<String> urls =
                new ArrayList<>();

        String regex =
                "(https?://[^\\s]+|www\\.[^\\s]+)";

        java.util.regex.Pattern pattern =
                java.util.regex.Pattern.compile(
                        regex,
                        java.util.regex.Pattern.CASE_INSENSITIVE
                );

        java.util.regex.Matcher matcher =
                pattern.matcher(text);

        while (matcher.find()) {

            String url =
                    matcher.group();

            // Remove common punctuation
            url = url.replaceAll(
                    "[.,!?;:]+$",
                    ""
            );

            if (!urls.contains(url)) {
                urls.add(url);
            }
        }

        return urls;
    }

    // ==========================================
    // FEATURE 5 - SUSPICIOUS URL DETECTION
    // ==========================================

    private static List<String> detectSuspiciousUrls(
            List<String> urls) {

        List<String> suspicious =
                new ArrayList<>();

        String[] suspiciousWords = {
                "login",
                "verify",
                "verification",
                "password",
                "account",
                "bank",
                "reward",
                "prize",
                "free",
                "claim",
                "urgent",
                "secure"
        };

        for (String url : urls) {

            String lower =
                    url.toLowerCase();

            boolean found = false;

            for (String word : suspiciousWords) {

                if (lower.contains(word)) {

                    found = true;
                    break;
                }
            }

            if (found && !suspicious.contains(url)) {

                suspicious.add(url);
            }
        }

        return suspicious;
    }

    // ==========================================
    // FEATURE 6 - DETAILED ANALYSIS
    // ==========================================

    private static void displayDetailedAnalysis(
            List<String> matches,
            String category,
            double confidence,
            String riskLevel,
            List<String> urls,
            List<String> suspiciousUrls,
            int networkFlow,
            List<String> selectedRules) {

        System.out.println(
                "Spam Category      : " + category
        );

        System.out.printf(
                "Confidence         : %.2f%%%n",
                confidence
        );

        System.out.println(
                "Risk Level         : " + riskLevel
        );

        System.out.println(
                "Matched Keywords   : " + matches.size()
        );

        System.out.println(
                "URLs Detected      : " + urls.size()
        );

        System.out.println(
                "Suspicious URLs    : "
                        + suspiciousUrls.size()
        );

        System.out.println(
                "Network Flow       : " + networkFlow
        );

        System.out.println(
                "Rules Selected     : "
                        + selectedRules.size()
        );
    }

    // ==========================================
    // FEATURE 7 - STATISTICS
    // ==========================================

    private static void displayStatistics(
            int total,
            int spam,
            int notSpam) {

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("AUTOMATIC STATISTICS");
        System.out.println("----------------------------------------------");

        double spamPercentage = 0;
        double notSpamPercentage = 0;

        if (total > 0) {

            spamPercentage =
                    (spam * 100.0) / total;

            notSpamPercentage =
                    (notSpam * 100.0) / total;
        }

        System.out.println(
                "Total Messages : " + total
        );

        System.out.println(
                "Spam Messages  : " + spam
        );

        System.out.println(
                "Not Spam       : " + notSpam
        );

        System.out.printf(
                "Spam Percentage : %.2f%%%n",
                spamPercentage
        );

        System.out.printf(
                "Not Spam Percentage : %.2f%%%n",
                notSpamPercentage
        );
    }

    // ==========================================
    // FEATURE 8 - CATEGORY STATISTICS
    // ==========================================

    private static void displayCategoryStatistics(
            Map<String, Integer> categoryCount) {

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("CATEGORY DISTRIBUTION");
        System.out.println("----------------------------------------------");

        if (categoryCount.isEmpty()) {

            System.out.println(
                    "No categories available."
            );

            return;
        }

        for (Map.Entry<String, Integer> entry :
                categoryCount.entrySet()) {

            System.out.println(
                    entry.getKey()
                            + " : "
                            + entry.getValue()
            );
        }
    }

    // ==========================================
    // FEATURE 9 - TOP SPAM KEYWORDS
    // ==========================================

    private static void displayTopKeywords(
            Map<String, Integer> keywordFrequency) {

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("TOP SPAM KEYWORDS");
        System.out.println("----------------------------------------------");

        if (keywordFrequency.isEmpty()) {

            System.out.println(
                    "No spam keywords detected."
            );

            return;
        }

        List<Map.Entry<String, Integer>> entries =
                new ArrayList<>(
                        keywordFrequency.entrySet()
                );

        entries.sort(
                (a, b) ->
                        Integer.compare(
                                b.getValue(),
                                a.getValue()
                        )
        );

        int limit =
                Math.min(5, entries.size());

        for (int i = 0; i < limit; i++) {

            Map.Entry<String, Integer> entry =
                    entries.get(i);

            System.out.println(
                    (i + 1)
                            + ". "
                            + entry.getKey()
                            + " -> "
                            + entry.getValue()
                            + " occurrence(s)"
            );
        }
    }

    // ==========================================
    // FEATURE 10 - EXPORT REPORT
    // ==========================================

    private static void exportReport(
            List<Email> messages,
            String[] spamPatterns,
            AhoCorasick ahoCorasick) {

        String reportPath =
                "data/spam_analysis_report.txt";

        int spamCount = 0;
        int notSpamCount = 0;

        try {

            FileWriter writer =
                    new FileWriter(reportPath);

            writer.write(
                    "==============================================\n"
            );

            writer.write(
                    "        EMAIL SPAM ANALYSIS REPORT\n"
            );

            writer.write(
                    "==============================================\n\n"
            );

            for (Email email : messages) {

                String text =
                        TextPreprocessor.cleanText(
                                email.getFullText()
                        );

                List<String> matches =
                        ahoCorasick.search(text);

                int score =
                        Math.min(
                                matches.size() * 4,
                                12
                        );

                boolean spam =
                        SpamAnalyzer.isSpam(score);

                if (spam) {
                    spamCount++;
                } else {
                    notSpamCount++;
                }

                String category =
                        detectCategory(matches);

                double confidence =
                        calculateConfidence(score);

                String risk =
                        getRiskLevel(score);

                List<String> urls =
                        detectUrls(email.getFullText());

                List<String> suspiciousUrls =
                        detectSuspiciousUrls(urls);

                writer.write(
                        "----------------------------------------------\n"
                );

                writer.write(
                        "Email ID      : "
                                + email.getEmailId()
                                + "\n"
                );

                writer.write(
                        "Message ID    : "
                                + email.getMessageId()
                                + "\n"
                );

                writer.write(
                        "Subject       : "
                                + email.getSubject()
                                + "\n"
                );

                writer.write(
                        "Classification: "
                                + SpamAnalyzer.getResult(score)
                                + "\n"
                );

                writer.write(
                        "Category      : "
                                + category
                                + "\n"
                );

                writer.write(
                        String.format(
                                "Confidence    : %.2f%%%n",
                                confidence
                        )
                );

                writer.write(
                        "Risk Level    : "
                                + risk
                                + "\n"
                );

                writer.write(
                        "Spam Score    : "
                                + score
                                + "\n"
                );

                writer.write(
                        "Keywords      : "
                                + getMatchesText(matches)
                                + "\n"
                );

                writer.write(
                        "URLs           : "
                                + getMatchesText(urls)
                                + "\n"
                );

                writer.write(
                        "Suspicious URLs: "
                                + getMatchesText(
                                        suspiciousUrls
                                )
                                + "\n"
                );

                writer.write("\n");
            }

            writer.write(
                    "==============================================\n"
            );

            writer.write(
                    "SUMMARY\n"
            );

            writer.write(
                    "==============================================\n"
            );

            writer.write(
                    "Total Messages : "
                            + messages.size()
                            + "\n"
            );

            writer.write(
                    "Spam Messages  : "
                            + spamCount
                            + "\n"
            );

            writer.write(
                    "Not Spam       : "
                            + notSpamCount
                            + "\n"
            );

            double percentage = 0;

            if (!messages.isEmpty()) {

                percentage =
                        (spamCount * 100.0)
                                / messages.size();
            }

            writer.write(
                    String.format(
                            "Spam Percentage: %.2f%%%n",
                            percentage
                    )
            );

            writer.close();

            System.out.println();
            System.out.println("----------------------------------------------");
            System.out.println("REPORT EXPORT");
            System.out.println("----------------------------------------------");

            System.out.println(
                    "Report successfully exported to:"
            );

            System.out.println(
                    reportPath
            );

        } catch (IOException e) {

            System.out.println(
                    "Unable to export report."
            );

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }

    // ==========================================
    // DISPLAY MATCHED PATTERNS
    // ==========================================

    private static String getMatchesText(
            List<String> matches) {

        if (matches.isEmpty()) {
            return "None";
        }

        return matches.toString();
    }

    // ==========================================
    // DISPLAY MESSAGE IDs
    // ==========================================

    private static String getMessageIdsText(
            List<Integer> messageIds) {

        if (messageIds.isEmpty()) {
            return "None";
        }

        return messageIds.toString();
    }

    // ==========================================
    // CO4 - NETWORK FLOW
    // ==========================================

    private static int calculateNetworkFlow(
            List<String> detectedFeatures) {

        int[][] capacity =
                new int[6][6];

        // Source -> Categories

        capacity[0][1] = 5;
        capacity[0][2] = 5;
        capacity[0][3] = 5;
        capacity[0][4] = 5;

        // Categories -> Sink

        capacity[1][5] = 5;
        capacity[2][5] = 5;
        capacity[3][5] = 5;
        capacity[4][5] = 5;

        // Add detected features

        for (String feature :
                detectedFeatures) {

            if (feature.equals(
                    "bank details")) {

                capacity[0][1]++;

            } else if (
                    feature.equals("password")
                            ||
                            feature.equals(
                                    "account verification")) {

                capacity[0][2]++;

            } else if (
                    feature.equals("free prize")
                            ||
                            feature.equals("lottery")
                            ||
                            feature.equals(
                                    "claim your reward")) {

                capacity[0][3]++;

            } else if (
                    feature.equals("click here")
                            ||
                            feature.equals("urgent")) {

                capacity[0][4]++;
            }
        }

        EdmondsKarp network =
                new EdmondsKarp(capacity);

        return network.maxFlow(0, 5);
    }

    // ==========================================
    // CO5 - SPAM RULE SETS
    // ==========================================

    private static Map<String, Set<String>>
    createSpamRules() {

        Map<String, Set<String>> rules =
                new LinkedHashMap<>();

        rules.put(
                "Prize Scam Rule",
                new HashSet<>(Arrays.asList(
                        "free prize",
                        "lottery",
                        "claim your reward"
                ))
        );

        rules.put(
                "Account Scam Rule",
                new HashSet<>(Arrays.asList(
                        "password",
                        "account verification",
                        "urgent"
                ))
        );

        rules.put(
                "Financial Scam Rule",
                new HashSet<>(Arrays.asList(
                        "bank details",
                        "claim your reward"
                ))
        );

        rules.put(
                "Click Scam Rule",
                new HashSet<>(Arrays.asList(
                        "click here",
                        "urgent"
                ))
        );

        return rules;
    }
}