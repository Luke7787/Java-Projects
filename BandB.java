// Name: Luke Zhuang
// Importing the necessary libraries
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

// Defining a Node class that represents nodes in the decision tree of the knapsack problem
class Node {
    int level;      // Node's level in the decision tree
    int profit;     // Profit up to this node
    int weight;     // Total weight up to this node
    double bound;   // Upper bound on profit in subtree rooted at this node
    Node parent;    // Parent node in the decision tree
    int itemIndex;  // Index of the item related to the node (used to trace back the items included)

    public Node(int level, int profit, int weight, double bound) {
        this.level = level;
        this.profit = profit;
        this.weight = weight;
        this.bound = bound;
    }
}

// Defining an Item class that holds information about a single item
class Item {
    int originalIndex; // Original index of the item before sorting based on profit/weight ratio
    int key;           // The identifier of the item
    int value;         // The value or profit of the item
    int weight;        // The weight of the item

    public Item(int originalIndex, int key, int value, int weight) {
        this.originalIndex = originalIndex;
        this.key = key;
        this.value = value;
        this.weight = weight;
    }
}

public class BandB {
    // bestLeaf holds the node of the decision tree which results in maximum profit
    static Node bestLeaf = null;

    // branchAndBound() is the main function that solves the problem using branch and bound technique
    static int branchAndBound(ArrayList<Item> items, int W) {
        // Transforming items into separate arrays for easy access
        int n = items.size(); // Number of items
        int[] p = new int[n]; // Array of profits
        int[] w = new int[n]; // Array of weights

        for (int i = 0; i < n; i++) {
            p[i] = items.get(i).value;
            w[i] = items.get(i).weight;
        }

        // Initializing root of the decision tree
        Node v = new Node(-1, 0, 0, 0);

        // maxProfit keeps track of the maximum profit achieved so far
        int maxProfit = 0;

        // Priority queue (implemented as max heap) to store live nodes of the decision tree
        PriorityQueue<Node> Q = new PriorityQueue<>(Comparator.comparingDouble(x -> -x.bound));

        // Initialize bound of root node
        v.bound = bound(v, W, n, w, p);

        // Add root to the queue
        Q.offer(v);

        // While there are live nodes in the queue
        while (!Q.isEmpty()) {
            // Remove a node from the queue
            v = Q.poll();

            // If it is promising
            if (v.bound > maxProfit && v.level < n - 1) {
                // Create a new node u
                Node u = new Node(v.level + 1, v.profit + p[v.level + 1], v.weight + w[v.level + 1], 0);
                u.parent = v; // Connect the node with its parent
                u.itemIndex = v.level + 1; // Update the item index

                // If the node is within the weight limit and increases profit, update maxProfit and bestLeaf
                if (u.weight <= W && u.profit > maxProfit) {
                    maxProfit = u.profit;
                    bestLeaf = u;
                }

                // Compute bound of u
                u.bound = bound(u, W, n, w, p);

                // If u is promising, add it to the queue
                if (u.bound > maxProfit) {
                    Q.offer(u);
                }

                // Create another node u2, which does not include the next item
                Node u2 = new Node(v.level + 1, v.profit, v.weight, 0);
                u2.parent = v; // Connect the node with its parent
                u2.itemIndex = -1; // Update the item index (no item added)

                // Compute bound of u2
                u2.bound = bound(u2, W, n, w, p);

                // If u2 is promising, add it to the queue
                if (u2.bound > maxProfit) {
                    Q.offer(u2);
                }
            }
        }

        // Return the maximum profit achieved
        return maxProfit;
    }

    // Function to calculate the upper bound on the maximum profit achievable
    public static double bound(Node u, int W, int n, int[] w, int[] p) {
        // If weight overcomes the knapsack capacity, return 0
        if (u.weight >= W) {
            return 0;
        }

        // Initialize result and total weight
        int j = u.level + 1;
        int totWeight = u.weight;
        double result = u.profit;

        // Start including items while weight is within limit
        while (j < n && totWeight + w[j] <= W) {
            totWeight += w[j];
            result += p[j];
            j++;
        }

        // At this point either j >= n or totWeight + w[j] > W
        // If j is not n, include fraction of p[j]/w[j] in result for remaining weight
        if (j < n && totWeight != W) {
            result += (W - totWeight) * (double) p[j] / w[j];
        }

        return result;
    }

    // Function to extract the items selected by the branch and bound algorithm
    public static ArrayList<Item> getSelectedItems(Node bestLeaf, ArrayList<Item> items) {
        ArrayList<Item> selectedItems = new ArrayList<>();
        Node currentNode = bestLeaf;

        // Trace back from the best leaf to the root
        while (currentNode.parent != null) {
            if (currentNode.itemIndex != -1) {
                // If currentNode has an item associated (itemIndex != -1), add it to selectedItems
                selectedItems.add(items.get(currentNode.itemIndex));
            }
            currentNode = currentNode.parent; // Move to the parent node
        }

        // Reverse the selectedItems list to get items in the order they were added
        Collections.reverse(selectedItems);
        return selectedItems;
    }

    public static void main(String[] args) {
        // Define the path to the input file
        String filePath = "hard50.txt";
        ArrayList<Item> items = new ArrayList<>();
        int capacity = 0;

        // Reading items data from the file
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            int n = Integer.parseInt(br.readLine().trim());

            for (int i = 0; i < n; i++) {
                String[] itemData = br.readLine().trim().split("\\s+");
                Item item = new Item(
                        i,
                        Integer.parseInt(itemData[0].trim()),
                        Integer.parseInt(itemData[1].trim()),
                        Integer.parseInt(itemData[2].trim())
                );
                items.add(item);
            }

            capacity = Integer.parseInt(br.readLine().trim());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sort the items based on their profit/weight ratio
        Collections.sort(items, (Item a, Item b) -> {
            double r1 = (double) a.value / a.weight;
            double r2 = (double) b.value / b.weight;
            return Double.compare(r2, r1);
        });

        // Start timing the execution
        long startTime = System.nanoTime();

        // Solve the problem
        int maxProfit = branchAndBound(items, capacity);

        // Get the selected items
        ArrayList<Item> selectedItems = getSelectedItems(bestLeaf, items);

        // Sort the selected items based on their original index
        selectedItems.sort(Comparator.comparingInt(item -> item.originalIndex));

        // Calculate the total weight of selected items
        int currentWeight = 0;
        for (Item item : selectedItems) {
            currentWeight += item.weight;
        }

        // End timing the execution
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000_000.0; // Calculate execution time in seconds

        // Display the results
        System.out.println("---------------------------------------------------");
        System.out.println("File Name: " + filePath);
        System.out.println("Branch and Bound solution: Value: " + maxProfit + ", Weight: " + currentWeight);
        System.out.print("Selected items: ");
        for (Item item : selectedItems) {
            System.out.print(item.key + " ");
        }
        System.out.println();
        System.out.printf("Execution time: %.3f seconds%n", executionTime);
        System.out.println("---------------------------------------------------");
    }
}
