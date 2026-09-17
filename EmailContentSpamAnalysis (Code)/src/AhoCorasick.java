import java.util.*;

public class AhoCorasick {

    private static class Node {

        Map<Character, Integer> children = new HashMap<>();
        int failure = 0;
        List<String> outputs = new ArrayList<>();
    }

    private final List<Node> trie = new ArrayList<>();

    public AhoCorasick(String[] patterns) {

        trie.add(new Node());

        // Build Trie
        for (String pattern : patterns) {

            int current = 0;

            for (char ch : pattern.toCharArray()) {

                if (!trie.get(current).children.containsKey(ch)) {

                    trie.get(current).children.put(ch, trie.size());
                    trie.add(new Node());
                }

                current = trie.get(current).children.get(ch);
            }

            trie.get(current).outputs.add(pattern);
        }

        buildFailureLinks();
    }

    private void buildFailureLinks() {

        Queue<Integer> queue = new LinkedList<>();

        // First level nodes
        for (int next : trie.get(0).children.values()) {
            queue.add(next);
            trie.get(next).failure = 0;
        }

        while (!queue.isEmpty()) {

            int current = queue.poll();

            for (Map.Entry<Character, Integer> entry :
                    trie.get(current).children.entrySet()) {

                char ch = entry.getKey();
                int next = entry.getValue();

                queue.add(next);

                int failure = trie.get(current).failure;

                while (failure != 0 &&
                        !trie.get(failure).children.containsKey(ch)) {

                    failure = trie.get(failure).failure;
                }

                if (trie.get(failure).children.containsKey(ch)
                        && trie.get(failure).children.get(ch) != next) {

                    trie.get(next).failure =
                            trie.get(failure).children.get(ch);
                } else {
                    trie.get(next).failure = 0;
                }

                trie.get(next).outputs.addAll(
                        trie.get(trie.get(next).failure).outputs
                );
            }
        }
    }

    public List<String> search(String text) {

        List<String> foundPatterns = new ArrayList<>();

        int current = 0;

        for (char ch : text.toCharArray()) {

            while (current != 0 &&
                    !trie.get(current).children.containsKey(ch)) {

                current = trie.get(current).failure;
            }

            if (trie.get(current).children.containsKey(ch)) {
                current = trie.get(current).children.get(ch);
            }

            for (String pattern : trie.get(current).outputs) {

                if (!foundPatterns.contains(pattern)) {
                    foundPatterns.add(pattern);
                }
            }
        }

        return foundPatterns;
    }
}