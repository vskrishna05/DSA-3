import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParallelEmailProcessor {

    public static void processEmails(List<Email> emails) {

        int numberOfThreads =
                Runtime.getRuntime().availableProcessors();

        ExecutorService executor =
                Executors.newFixedThreadPool(numberOfThreads);

        System.out.println(
                "Available Processors: "
                        + numberOfThreads
        );

        System.out.println(
                "Processing Emails in Parallel..."
        );

        for (Email email : emails) {

            executor.submit(() -> {

                TextPreprocessor.cleanText(
                        email.getFullText()
                );

                System.out.println(
                        "Thread: "
                                + Thread.currentThread().getName()
                                + " -> Email ID: "
                                + email.getEmailId()
                                + " processed"
                );
            });
        }

        executor.shutdown();

        try {

            if (!executor.awaitTermination(
                    10,
                    TimeUnit.SECONDS)) {

                executor.shutdownNow();
            }

        } catch (InterruptedException e) {

            executor.shutdownNow();

            Thread.currentThread().interrupt();
        }

        System.out.println(
                "Parallel Processing Completed."
        );
    }
}