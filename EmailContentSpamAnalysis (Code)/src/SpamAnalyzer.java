public class SpamAnalyzer {

    public static boolean isSpam(int score) {

        return score >= 8;
    }

    public static String getResult(int score) {

        if (isSpam(score)) {
            return "SPAM";
        }

        return "NOT SPAM";
    }
}