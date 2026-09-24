import java.util.*;
import java.util.concurrent.*;

/**
 * Algorithm Comparison
 *
 * Compares all 9 algorithms using only the selected
 * user's email messages.
 *
 * Algorithms:
 * 1. KMP
 * 2. Rabin-Karp
 * 3. Z-Algorithm
 * 4. Aho-Corasick
 * 5. Bitmask DP
 * 6. Edmonds-Karp
 * 7. Greedy Set Cover
 * 8. Randomized Hashing
 * 9. Parallel Processing
 */
public class AlgorithmComparison {

    // =========================================================
    // RESULT CLASS
    // =========================================================

    private static class Result {

        String algorithm;
        double timeMs;
        String result;

        Result(
                String algorithm,
                double timeMs,
                String result) {

            this.algorithm = algorithm;
            this.timeMs = timeMs;
            this.result = result;
        }
    }

    // =========================================================
    // MAIN COMPARISON METHOD
    // =========================================================

    public static void compareAll(
            List<Email> userMessages,
            String[] spamKeywords) {

        if (userMessages == null || userMessages.isEmpty()) {

            System.out.println();
            System.out.println(
                    "No messages available for comparison."
            );

            return;
        }

        if (spamKeywords == null || spamKeywords.length == 0) {

            System.out.println();
            System.out.println(
                    "No spam keywords available for comparison."
            );

            return;
        }

        List<Result> results =
                new ArrayList<>();

        int messagesTested =
                userMessages.size();

        int patternsTested =
                spamKeywords.length;

        // =====================================================
        // HEADER
        // =====================================================

        System.out.println();

        System.out.println(
                "=============================================================="
        );

        System.out.println(
                "                    ALGORITHM COMPARISON"
        );

        System.out.println(
                "=============================================================="
        );

        System.out.println();

        System.out.println(
                "Messages tested : "
                        + messagesTested
        );

        System.out.println(
                "Patterns tested : "
                        + patternsTested
        );

        System.out.println();

        System.out.println(
                "Comparing 9 algorithms on the selected email data."
        );

        System.out.println();

        // =====================================================
        // 1. KMP
        // =====================================================

        results.add(
                benchmarkKMP(
                        userMessages,
                        spamKeywords
                )
        );

        // =====================================================
        // 2. RABIN-KARP
        // =====================================================

        results.add(
                benchmarkRabinKarp(
                        userMessages,
                        spamKeywords
                )
        );

        // =====================================================
        // 3. Z-ALGORITHM
        // =====================================================

        results.add(
                benchmarkZAlgorithm(
                        userMessages,
                        spamKeywords
                )
        );

        // =====================================================
        // 4. AHO-CORASICK
        // =====================================================

        results.add(
                benchmarkAhoCorasick(
                        userMessages,
                        spamKeywords
                )
        );

        // =====================================================
        // 5. BITMASK DP
        // =====================================================

        results.add(
                benchmarkBitmaskDP(
                        userMessages,
                        spamKeywords
                )
        );

        // =====================================================
        // 6. EDMONDS-KARP
        // =====================================================

        results.add(
                benchmarkEdmondsKarp(
                        userMessages
                )
        );

        // =====================================================
        // 7. GREEDY SET COVER
        // =====================================================

        results.add(
                benchmarkSetCover(
                        spamKeywords
                )
        );

        // =====================================================
        // 8. RANDOMIZED HASHING
        // =====================================================

        results.add(
                benchmarkRandomizedHash(
                        userMessages
                )
        );

        // =====================================================
        // 9. PARALLEL PROCESSING
        // =====================================================

        results.add(
                benchmarkParallelProcessing(
                        userMessages
                )
        );

        // =====================================================
        // DISPLAY RESULTS
        // =====================================================

        printResults(results);
    }

    // =========================================================
    // 1. KMP
    // =========================================================

    private static Result benchmarkKMP(
            List<Email> messages,
            String[] patterns) {

        long start =
                System.nanoTime();

        int matches = 0;

        for (Email email : messages) {

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            for (String pattern : patterns) {

                String cleanPattern =
                        TextPreprocessor.cleanText(
                                pattern
                        );

                if (KMP.search(
                        text,
                        cleanPattern)) {

                    matches++;
                }
            }
        }

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "KMP",
                timeMs,
                matches + " matches"
        );
    }

    // =========================================================
    // 2. RABIN-KARP
    // =========================================================

    private static Result benchmarkRabinKarp(
            List<Email> messages,
            String[] patterns) {

        long start =
                System.nanoTime();

        int matches = 0;

        for (Email email : messages) {

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            for (String pattern : patterns) {

                String cleanPattern =
                        TextPreprocessor.cleanText(
                                pattern
                        );

                if (RabinKarp.search(
                        text,
                        cleanPattern)) {

                    matches++;
                }
            }
        }

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Rabin-Karp",
                timeMs,
                matches + " matches"
        );
    }

    // =========================================================
    // 3. Z-ALGORITHM
    // =========================================================

    private static Result benchmarkZAlgorithm(
            List<Email> messages,
            String[] patterns) {

        long start =
                System.nanoTime();

        int matches = 0;

        for (Email email : messages) {

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            for (String pattern : patterns) {

                String cleanPattern =
                        TextPreprocessor.cleanText(
                                pattern
                        );

                if (ZAlgorithm.search(
                        text,
                        cleanPattern)) {

                    matches++;
                }
            }
        }

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Z-Algorithm",
                timeMs,
                matches + " matches"
        );
    }

    // =========================================================
    // 4. AHO-CORASICK
    // =========================================================

    private static Result benchmarkAhoCorasick(
            List<Email> messages,
            String[] patterns) {

        long start =
                System.nanoTime();

        AhoCorasick aho =
                new AhoCorasick(patterns);

        int totalMatches = 0;

        for (Email email : messages) {

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            List<String> found =
                    aho.search(text);

            totalMatches +=
                    found.size();
        }

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Aho-Corasick",
                timeMs,
                totalMatches + " matches"
        );
    }

    // =========================================================
    // 5. BITMASK DP
    // =========================================================

    private static Result benchmarkBitmaskDP(
            List<Email> messages,
            String[] patterns) {

        long start =
                System.nanoTime();

        /*
         * Bitmask DP has exponential complexity.
         * Therefore only the detected features from
         * the selected user's messages are considered.
         */

        Set<String> detected =
                new LinkedHashSet<>();

        AhoCorasick aho =
                new AhoCorasick(patterns);

        for (Email email : messages) {

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            detected.addAll(
                    aho.search(text)
            );
        }

        int n =
                Math.min(
                        20,
                        detected.size()
                );

        int[] scores =
                new int[n];

        int index = 0;

        for (String keyword : detected) {

            if (index >= n) {
                break;
            }

            scores[index] =
                    Math.max(
                            1,
                            keyword.length()
                    );

            index++;
        }

        int bestScore = 0;

        if (n > 0) {

            int limit =
                    Math.max(
                            1,
                            n / 2
                    );

            bestScore =
                    BitmaskDP.findBestScore(
                            scores,
                            limit
                    );
        }

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Bitmask DP",
                timeMs,
                "Best score = "
                        + bestScore
        );
    }

    // =========================================================
    // 6. EDMONDS-KARP
    // =========================================================

    private static Result benchmarkEdmondsKarp(
            List<Email> messages) {

        long start =
                System.nanoTime();

        int n =
                Math.min(
                        12,
                        Math.max(
                                6,
                                messages.size() + 2
                        )
                );

        int source = 0;
        int sink = n - 1;

        int[][] capacity =
                new int[n][n];

        /*
         * Source -> processing nodes
         */

        for (int i = 1; i < sink; i++) {

            capacity[source][i] =
                    5 + (i % 5);
        }

        /*
         * Processing nodes -> sink
         */

        for (int i = 1; i < sink; i++) {

            capacity[i][sink] =
                    3 + (i % 4);
        }

        /*
         * Connections between processing nodes
         */

        for (int i = 1; i < sink; i++) {

            for (int j = i + 1;
                 j < sink;
                 j++) {

                if ((i + j) % 3 == 0) {

                    capacity[i][j] =
                            2 + ((i + j) % 4);
                }
            }
        }

        EdmondsKarp algorithm =
                new EdmondsKarp(
                        capacity
                );

        int maxFlow =
                algorithm.maxFlow(
                        source,
                        sink
                );

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Edmonds-Karp",
                timeMs,
                "Max Flow = "
                        + maxFlow
        );
    }

    // =========================================================
    // 7. GREEDY SET COVER
    // =========================================================

    private static Result benchmarkSetCover(
            String[] patterns) {

        long start =
                System.nanoTime();

        Map<String, Set<String>> rules =
                new LinkedHashMap<>();

        Set<String> requiredFeatures =
                new LinkedHashSet<>();

        for (int i = 0;
             i < patterns.length;
             i++) {

            requiredFeatures.add(
                    "F" + i
            );
        }

        int ruleNumber = 1;

        for (int i = 0;
             i < patterns.length;
             i += 5) {

            Set<String> covered =
                    new LinkedHashSet<>();

            for (int j = i;
                 j < Math.min(
                         i + 5,
                         patterns.length
                 );
                 j++) {

                covered.add(
                        "F" + j
                );
            }

            rules.put(
                    "Rule-" + ruleNumber,
                    covered
            );

            ruleNumber++;
        }

        List<String> selectedRules =
                SetCoverApproximation.selectRules(
                        rules,
                        requiredFeatures
                );

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Greedy Set Cover",
                timeMs,
                "Rules selected = "
                        + selectedRules.size()
        );
    }

    // =========================================================
    // 8. RANDOMIZED HASHING
    // =========================================================

    private static Result benchmarkRandomizedHash(
            List<Email> messages) {

        long start =
                System.nanoTime();

        RandomizedHash hasher =
                new RandomizedHash();

        int processed = 0;

        long combinedHash = 0;

        for (Email email : messages) {

            String text =
                    TextPreprocessor.cleanText(
                            email.getFullText()
                    );

            int hash =
                    hasher.getHash(text);

            combinedHash ^=
                    hash;

            processed++;
        }

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Randomized Hashing",
                timeMs,
                "Emails hashed = "
                        + processed
                        + " | Hash = "
                        + combinedHash
        );
    }

    // =========================================================
    // 9. PARALLEL PROCESSING
    // =========================================================

    private static Result benchmarkParallelProcessing(
            List<Email> messages) {

        long start =
                System.nanoTime();

        int processors =
                Runtime.getRuntime()
                        .availableProcessors();

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        processors
                );

        List<Future<Integer>> tasks =
                new ArrayList<>();

        for (Email email : messages) {

            tasks.add(
                    executor.submit(() -> {

                        String text =
                                TextPreprocessor.cleanText(
                                        email.getFullText()
                                );

                        return text.length();
                    })
            );
        }

        int processed = 0;

        for (Future<Integer> task : tasks) {

            try {

                task.get();

                processed++;

            } catch (Exception e) {

                System.out.println(
                        "Parallel processing error: "
                                + e.getMessage()
                );
            }
        }

        executor.shutdown();

        long end =
                System.nanoTime();

        double timeMs =
                (end - start)
                        / 1_000_000.0;

        return new Result(
                "Parallel Processing",
                timeMs,
                "Emails processed = "
                        + processed
                        + " | Threads = "
                        + processors
        );
    }

    // =========================================================
    // PRINT COMPARISON
    // =========================================================

    private static void printResults(
            List<Result> results) {

        System.out.println(
                "--------------------------------------------------------------"
        );

        System.out.printf(
                "%-24s %-15s %-30s%n",
                "Algorithm",
                "Time (ms)",
                "Result"
        );

        System.out.println(
                "--------------------------------------------------------------"
        );

        for (Result result : results) {

            System.out.printf(
                    "%-24s %-15.3f %-30s%n",
                    result.algorithm,
                    result.timeMs,
                    result.result
            );
        }

        System.out.println(
                "--------------------------------------------------------------"
        );

        // Find the smallest measured execution time.
        Result fastest =
                Collections.min(
                        results,
                        Comparator.comparingDouble(
                                r -> r.timeMs
                        )
                );

        System.out.println();

        System.out.println(
                "FASTEST MEASURED EXECUTION : "
                        + fastest.algorithm
        );

        System.out.printf(
                "Measured execution time     : %.3f ms%n",
                fastest.timeMs
        );

        System.out.println(
                "Result                      : "
                        + fastest.result
        );

        System.out.println();

        System.out.println(
                "NOTE: The 9 algorithms solve different problems,"
        );

        System.out.println(
                "so their execution times are shown for reference."
        );
    }
}