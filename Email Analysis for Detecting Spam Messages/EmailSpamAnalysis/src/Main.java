import java.util.*;

public class Main {

    public static void main(String[] args) {

        String filePath = "data/emails.txt";

        List<Email> emails =
                EmailReader.readEmails(filePath);

        // ==========================================
        // SPAM PATTERNS
        // ==========================================

        List<String> keywordList =
        EmailReader.readSpamKeywords(
                "data/spam_keywords.txt"
        );

String[] spamPatterns =
        keywordList.toArray(new String[0]);

        int[] featureScores = {
            5, 4, 5, 3, 5, 5, 4, 4
        };

        AhoCorasick ahoCorasick =
                new AhoCorasick(spamPatterns);

        Map<String, Set<String>> rules =
                createSpamRules();

        // ==========================================
        // PROJECT HEADER
        // ==========================================

        System.out.println();
        System.out.println("==============================================");
        System.out.println("        EMAIL SPAM ANALYSIS SYSTEM");
        System.out.println("==============================================");
        System.out.println(
                "Total Emails : " + emails.size()
        );

        // ==========================================
        // ALGORITHMS USED
        // ==========================================

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("ALGORITHMS USED");
        System.out.println("----------------------------------------------");

        System.out.println("1. KMP");
        System.out.println("2. Rabin-Karp");
        System.out.println("3. Z-Algorithm");
        System.out.println("4. Aho-Corasick");
        System.out.println("5. Bitmask DP");
        System.out.println("6. Edmonds-Karp");
        System.out.println("7. Greedy Set Cover");
        System.out.println("8. Randomized Hashing");
        System.out.println("9. Parallel Processing");

        // ==========================================
        // SELECT ANALYSIS MODE
        // ==========================================

        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("SELECT ANALYSIS MODE");
        System.out.println("----------------------------------------------");

        // Display all emails dynamically
        for (int i = 1; i <= emails.size(); i++) {

            System.out.println(
                    i + ". Analyze Email " + i
            );
        }

        System.out.println(
                "A. Analyze All Emails"
        );

        System.out.println(
                "M. Select Multiple Emails"
        );

        System.out.println(
                "0. Exit"
        );

        System.out.println("----------------------------------------------");

        System.out.print(
                "Enter your choice : "
        );

        String choice =
                scanner.nextLine().trim();

        // ==========================================
        // EXIT
        // ==========================================

        if (choice.equals("0")) {

            System.out.println();
            System.out.println(
                    "Thank you for using Email Spam Analysis System."
            );

            scanner.close();
            return;
        }

        // ==========================================
        // SELECT EMAILS
        // ==========================================

        List<Email> selectedEmails =
                new ArrayList<>();

        // ==========================================
        // SINGLE EMAIL
        // ==========================================

        try {

            int emailNumber =
                    Integer.parseInt(choice);

            if (emailNumber >= 1 &&
                    emailNumber <= emails.size()) {

                selectedEmails.add(
                        emails.get(emailNumber - 1)
                );

            } else {

                System.out.println();
                System.out.println(
                        "Invalid choice."
                );

                System.out.println(
                        "Please enter a number from 1 to "
                                + emails.size()
                                + ", A, M, or 0."
                );

                scanner.close();
                return;
            }

        } catch (NumberFormatException e) {

            // ==========================================
            // ANALYZE ALL EMAILS
            // ==========================================

            if (choice.equalsIgnoreCase("A")) {

                selectedEmails.addAll(emails);
            }

            // ==========================================
            // SELECT MULTIPLE EMAILS
            // ==========================================

            else if (choice.equalsIgnoreCase("M")) {

                System.out.println();
                System.out.println(
                        "Enter email numbers separated by commas."
                );

                System.out.println(
                        "Example: 1,3,6,8"
                );

                System.out.print(
                        "Enter email numbers : "
                );

                String input =
                        scanner.nextLine().trim();

                String[] numbers =
                        input.split(",");

                Set<Integer> selectedNumbers =
                        new LinkedHashSet<>();

                boolean invalidInput = false;

                for (String number : numbers) {

                    try {

                        int emailNumber =
                                Integer.parseInt(
                                        number.trim()
                                );

                        if (emailNumber >= 1 &&
                                emailNumber <= emails.size()) {

                            selectedNumbers.add(
                                    emailNumber
                            );

                        } else {

                            invalidInput = true;
                        }

                    } catch (NumberFormatException ex) {

                        invalidInput = true;
                    }
                }

                if (invalidInput ||
                        selectedNumbers.isEmpty()) {

                    System.out.println();
                    System.out.println(
                            "Invalid email selection."
                    );

                    System.out.println(
                            "Please enter numbers from 1 to "
                                    + emails.size()
                    );

                    scanner.close();
                    return;
                }

                for (int emailNumber :
                        selectedNumbers) {

                    selectedEmails.add(
                            emails.get(emailNumber - 1)
                    );
                }

            }

            // ==========================================
            // INVALID CHOICE
            // ==========================================

            else {

                System.out.println();
                System.out.println(
                        "Invalid choice."
                );

                System.out.println(
                        "Please enter a number from 1 to "
                                + emails.size()
                                + ", A, M, or 0."
                );

                scanner.close();
                return;
            }
        }

        scanner.close();

        // ==========================================
        // SELECTED EMAIL SUMMARY
        // ==========================================

        System.out.println();
        System.out.println("==============================================");
        System.out.println("SELECTED EMAILS");
        System.out.println("==============================================");

        for (Email email : selectedEmails) {

            System.out.println(
                    "Email " + email.getId()
            );
        }

        System.out.println(
                "Total Selected : "
                        + selectedEmails.size()
        );

        // ==========================================
        // ANALYSIS
        // ==========================================

        int spamCount = 0;
        int notSpamCount = 0;

        for (Email email : selectedEmails) {

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

            } else {

                notSpamCount++;
            }
        }

        // ==========================================
        // FINAL REPORT
        // ==========================================

        System.out.println();
        System.out.println();

        ReportGenerator.generateSummary(
                selectedEmails,
                spamCount,
                notSpamCount
        );

        // ==========================================
        // PARALLEL PROCESSING
        // ==========================================

        System.out.println();
        System.out.println("----------------------------------------------");
        System.out.println("PARALLEL PROCESSING");
        System.out.println("----------------------------------------------");

        ParallelEmailProcessor.processEmails(
                selectedEmails
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
                "EMAIL " + email.getId()
        );
        System.out.println("==============================================");

        System.out.println(
                "Subject : " + email.getSubject()
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

                    detectedScores[i] =
                            featureScores[j];

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
    // DISPLAY MATCHES
    // ==========================================

    private static String getMatchesText(
            List<String> matches) {

        if (matches.isEmpty()) {

            return "None";
        }

        return matches.toString();
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