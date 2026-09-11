public class BitmaskDP {

    public static int findBestScore(int[] scores, int limit) {

        int n = scores.length;
        int totalMasks = 1 << n;

        int bestScore = 0;

        for (int mask = 0; mask < totalMasks; mask++) {

            int currentScore = 0;
            int selectedCount = 0;

            for (int i = 0; i < n; i++) {

                if ((mask & (1 << i)) != 0) {
                    currentScore += scores[i];
                    selectedCount++;
                }
            }

            if (selectedCount <= limit &&
                    currentScore > bestScore) {

                bestScore = currentScore;
            }
        }

        return bestScore;
    }
}