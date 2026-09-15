public class Email {

    private String emailId;
    private int messageId;
    private String subject;
    private String body;

    public Email(String emailId, int messageId,
                 String subject, String body) {

        this.emailId = emailId;
        this.messageId = messageId;
        this.subject = subject;
        this.body = body;
    }

    public String getEmailId() {
        return emailId;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getFullText() {
        return subject + " " + body;
    }

    @Override
    public String toString() {

        return "Message " + messageId +
                "\nSubject: " + subject +
                "\nBody: " + body;
    }
}