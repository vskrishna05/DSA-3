import java.util.*;

public class ReportGenerator {

    public static void generateSummary(
            List<Email> emails,
            int spamCount,
            int notSpamCount) {

        System.out.println("\n======================================");
        System.out.println("          FINAL EMAIL REPORT");
        System.out.println("======================================");

        System.out.println("Total Emails      : " + emails.size());
        System.out.println("Spam Emails       : " + spamCount);
        System.out.println("Not Spam Emails   : " + notSpamCount);

        if (!emails.isEmpty()) {

            double spamPercentage =
                    (spamCount * 100.0) / emails.size();

            System.out.printf(
                    "Spam Percentage   : %.2f%%\n",
                    spamPercentage
            );
        }

        System.out.println("======================================");
    }
}