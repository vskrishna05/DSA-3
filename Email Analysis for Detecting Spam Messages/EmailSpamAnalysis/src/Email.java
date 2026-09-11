public class Email {
    private int id;
    private String subject;
    private String body;

    public Email(int id, String subject, String body) {
        this.id = id;
        this.subject = subject;
        this.body = body;
    }

    public int getId() {
        return id;
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
        return "Email ID: " + id +
               "\nSubject: " + subject +
               "\nBody: " + body;
    }
}