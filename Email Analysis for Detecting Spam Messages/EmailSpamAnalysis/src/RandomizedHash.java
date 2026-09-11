import java.util.Random;

public class RandomizedHash {

    private final int base;

    public RandomizedHash() {

        Random random = new Random();

        // Random base between 256 and 1000
        base = 256 + random.nextInt(745);
    }

    public int getHash(String text) {

        int hash = 0;

        for (char ch : text.toCharArray()) {

            hash = hash * base + ch;
        }

        return hash;
    }
}