import java.io.*;
import java.util.*;

public class EmailReader {

    // ==========================================
    // READ ALL EMAIL MESSAGES
    // ==========================================

    public static List<Email> readEmails(String filePath) {

        List<Email> emails = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(filePath))) {

            String line;

            String emailId = "";
            int messageId = 0;
            String subject = "";
            String body = "";

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.startsWith("EMAIL_ID:")) {

                    emailId = line.substring(9).trim();

                } else if (line.startsWith("MESSAGE_ID:")) {

                    messageId = Integer.parseInt(
                            line.substring(11).trim()
                    );

                } else if (line.startsWith("SUBJECT:")) {

                    subject = line.substring(8).trim();

                } else if (line.startsWith("BODY:")) {

                    body = line.substring(5).trim();

                    emails.add(
                            new Email(
                                    emailId,
                                    messageId,
                                    subject,
                                    body
                            )
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


    // ==========================================
    // GET MESSAGES OF ONE EMAIL ID
    // ==========================================

    public static List<Email> getMessagesForEmail(
            List<Email> emails,
            String emailId) {

        List<Email> result = new ArrayList<>();

        for (Email email : emails) {

            if (email.getEmailId()
                    .equalsIgnoreCase(emailId)) {

                result.add(email);
            }
        }

        return result;
    }


    // ==========================================
    // CHECK WHETHER EMAIL ID EXISTS
    // ==========================================

    public static boolean emailExists(
            List<Email> emails,
            String emailId) {

        for (Email email : emails) {

            if (email.getEmailId()
                    .equalsIgnoreCase(emailId)) {

                return true;
            }
        }

        return false;
    }


    // ==========================================
    // GET NEXT MESSAGE ID
    // ==========================================

    public static int getNextMessageId(
            List<Email> emails,
            String emailId) {

        int maxId = 0;

        for (Email email : emails) {

            if (email.getEmailId()
                    .equalsIgnoreCase(emailId)) {

                if (email.getMessageId() > maxId) {

                    maxId = email.getMessageId();
                }
            }
        }

        return maxId + 1;
    }


    // ==========================================
    // ADD NEW MESSAGE TO emails.txt
    // ==========================================

    public static void addMessage(
            String filePath,
            String emailId,
            String subject,
            String body,
            int messageId) {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(
                                     filePath,
                                     true))) {

            writer.newLine();

            writer.write("EMAIL_ID: " + emailId);
            writer.newLine();

            writer.write("MESSAGE_ID: " + messageId);
            writer.newLine();

            writer.write("SUBJECT: " + subject);
            writer.newLine();

            writer.write("BODY: " + body);
            writer.newLine();

            writer.newLine();

            System.out.println(
                    "\nMessage stored successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Error storing message: "
                            + e.getMessage()
            );
        }
    }
}