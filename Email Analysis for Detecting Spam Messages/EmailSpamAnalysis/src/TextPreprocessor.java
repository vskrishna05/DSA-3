public class TextPreprocessor {

    public static String cleanText(String text) {

        // Convert to lowercase
        text = text.toLowerCase();

        // Remove punctuation and special characters
        text = text.replaceAll("[^a-z0-9\\s]", " ");

        // Remove extra spaces
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}