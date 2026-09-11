public class RabinKarp {

    private static final int PRIME = 101;

    public static boolean search(String text, String pattern) {

        int textLength = text.length();
        int patternLength = pattern.length();

        if (patternLength == 0) {
            return true;
        }

        if (patternLength > textLength) {
            return false;
        }

        int patternHash = 0;
        int textHash = 0;
        int highestPower = 1;

        // Calculate PRIME^(patternLength-1)
        for (int i = 0; i < patternLength - 1; i++) {
            highestPower = (highestPower * 256) % PRIME;
        }

        // Calculate initial hashes
        for (int i = 0; i < patternLength; i++) {
            patternHash =
                    (256 * patternHash + pattern.charAt(i)) % PRIME;

            textHash =
                    (256 * textHash + text.charAt(i)) % PRIME;
        }

        // Slide the pattern across the text
        for (int i = 0; i <= textLength - patternLength; i++) {

            // If hash values match, verify characters
            if (patternHash == textHash) {

                boolean match = true;

                for (int j = 0; j < patternLength; j++) {

                    if (text.charAt(i + j) != pattern.charAt(j)) {
                        match = false;
                        break;
                    }
                }

                if (match) {
                    return true;
                }
            }

            // Calculate hash for next window
            if (i < textLength - patternLength) {

                textHash =
                        (256 * (textHash
                        - text.charAt(i) * highestPower)
                        + text.charAt(i + patternLength)) % PRIME;

                if (textHash < 0) {
                    textHash += PRIME;
                }
            }
        }

        return false;
    }
}