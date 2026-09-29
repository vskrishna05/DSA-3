import java.util.*;

public class SetCoverApproximation {

    /*
     * Greedy Set Cover Approximation
     *
     * Selects a small number of rules that
     * cover all required spam features.
     */

    public static List<String> selectRules(
            Map<String, Set<String>> rules,
            Set<String> requiredFeatures) {

        Set<String> uncovered =
                new HashSet<>(requiredFeatures);

        List<String> selectedRules =
                new ArrayList<>();

        while (!uncovered.isEmpty()) {

            String bestRule = null;
            int bestCoverage = 0;

            for (Map.Entry<String, Set<String>> entry
                    : rules.entrySet()) {

                Set<String> covered =
                        new HashSet<>(entry.getValue());

                covered.retainAll(uncovered);

                if (covered.size() > bestCoverage) {

                    bestCoverage = covered.size();
                    bestRule = entry.getKey();
                }
            }

            // No rule can cover remaining features
            if (bestRule == null) {
                break;
            }

            selectedRules.add(bestRule);

            uncovered.removeAll(
                    rules.get(bestRule)
            );
        }

        return selectedRules;
    }
}