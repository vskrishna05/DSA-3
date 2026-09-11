import java.io.*;
import java.util.*;

public class EmailReader {

    public static List<Email> readEmails(String filePath) {

        List<Email> emails = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;
            int id = 0;
            String subject = "";
            String body = "";

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.startsWith("EMAIL_ID:")) {

                    id = Integer.parseInt(
                            line.substring(9).trim()
                    );

                } else if (line.startsWith("SUBJECT:")) {

                    subject = line.substring(8).trim();

                } else if (line.startsWith("BODY:")) {

                    body = line.substring(5).trim();

                    emails.add(
                            new Email(id, subject, body)
                    );

                    subject = "";
                    body = "";
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Error reading email file: "
                            + e.getMessage()
            );
        }

        return emails;
    }


    // ==========================================
    // READ SPAM KEYWORDS
    // ==========================================

    public static List<String> readSpamKeywords(
            String filePath) {

        List<String> keywords =
                new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (!line.isEmpty()) {

                    keywords.add(
                            line.toLowerCase()
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Error reading spam keywords: "
                            + e.getMessage()
            );
        }

        return keywords;
    }
}