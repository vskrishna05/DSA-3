import java.util.*;

public class EdmondsKarp {

    private int[][] capacity;
    private int[][] flow;
    private int n;

    public EdmondsKarp(int[][] capacity) {

        this.capacity = capacity;
        this.n = capacity.length;
        this.flow = new int[n][n];
    }

    public int maxFlow(int source, int sink) {

        int totalFlow = 0;

        while (true) {

            int[] parent = new int[n];
            Arrays.fill(parent, -1);

            parent[source] = source;

            Queue<Integer> queue = new LinkedList<>();
            queue.add(source);

            // BFS to find augmenting path
            while (!queue.isEmpty() && parent[sink] == -1) {

                int current = queue.poll();

                for (int next = 0; next < n; next++) {

                    int residualCapacity =
                            capacity[current][next]
                            - flow[current][next];

                    if (parent[next] == -1
                            && residualCapacity > 0) {

                        parent[next] = current;
                        queue.add(next);
                    }
                }
            }

            // No augmenting path
            if (parent[sink] == -1) {
                break;
            }

            // Find bottleneck capacity
            int pathFlow = Integer.MAX_VALUE;

            int current = sink;

            while (current != source) {

                int previous = parent[current];

                pathFlow = Math.min(
                        pathFlow,
                        capacity[previous][current]
                        - flow[previous][current]
                );

                current = previous;
            }

            // Update flow
            current = sink;

            while (current != source) {

                int previous = parent[current];

                flow[previous][current] += pathFlow;
                flow[current][previous] -= pathFlow;

                current = previous;
            }

            totalFlow += pathFlow;
        }

        return totalFlow;
    }
}