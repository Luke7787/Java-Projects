// Name: Luke Zhuang
// Importing necessary libraries
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynProg {
    // This function returns maximum of two integers
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    // This is a helper class to store the result of the knapsack algorithm: value and weight
    public static class KnapsackResult {
        int value;
        int weight;

        public KnapsackResult(int value, int weight) {
            this.value = value;
            this.weight = weight;
        }
    }

    // This function implements the dynamic programming solution for 0-1 knapsack problem
    public static KnapsackResult knapSack(int W, int wt[], int val[], int n, List<Integer> selectedItems) {
        int i, w;
        int K[][] = new int[n + 1][W + 1]; // This table will store the maximum value that can be achieved with a knapsack of weight w using the first i items

        // Building the K[][] table in a bottom-up manner
        for (i = 0; i <= n; i++) {
            for (w = 0; w <= W; w++) {
                if (i == 0 || w == 0)
                    K[i][w] = 0; // Base case: If number of items or total weight is 0, maximum value is also 0
                else if (wt[i - 1] <= w)
                    // If weight of the current item is less than or equal to w, take the maximum of two cases: 
                    // (1) including the current item, (2) excluding the current item
                    K[i][w] = max(val[i - 1] + K[i - 1][w - wt[i - 1]], K[i - 1][w]);
                else
                    // If weight of the current item is more than w, this item cannot be included in the optimal solution
                    K[i][w] = K[i - 1][w];
            }
        }

        // Stores the result of the Knapsack
        int res = K[n][W]; 
        w = W;
        int totalWeight = 0;
        // This loop finds the items that are included in the optimal solution and their total weight
        for (i = n; i > 0 && res > 0; i--) {
            if (res != K[i - 1][w]) {
                selectedItems.add(i); // This item is included
                totalWeight += wt[i - 1];
                res -= val[i - 1];
                w -= wt[i - 1];
            }
        }

        // Return the maximum value that can be put in a knapsack of capacity W and the total weight of items selected
        return new KnapsackResult(K[n][W], totalWeight);
    }

    // Driver code
public static void main(String[] args) {
    String fileName = "hard200.txt"; // Store the file name
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        // Read the number of items from the first line of the file
        int numItems = Integer.parseInt(reader.readLine().trim());

        int[] values = new int[numItems];  // Array to store values of the items
        int[] weights = new int[numItems]; // Array to store weights of the items

        // Read values and weights of the items from the file
        for (int i = 0; i < numItems; i++) {
            String[] itemData = reader.readLine().trim().split("\\s+");
            int index = Integer.parseInt(itemData[0]);
            values[index - 1] = Integer.parseInt(itemData[1]);
            weights[index - 1] = Integer.parseInt(itemData[2]);
        }

        // Read the capacity of the knapsack from the file
        int capacity = Integer.parseInt(reader.readLine().trim());

        // List to store the items selected in the knapsack
        List<Integer> selectedItems = new ArrayList<>();

        long startTime = System.nanoTime(); // Start the timer

        // Call the function knapSack
        KnapsackResult result = knapSack(capacity, weights, values, numItems, selectedItems);

        long endTime = System.nanoTime(); // Stop the timer
        double executionTime = (endTime - startTime) / 1_000_000_000.0; // Calculate execution time in seconds

        // Print the results
        System.out.println("---------------------------------------------------");
        System.out.println("File Name: " + fileName + "\nDynamic Programming solution: Value: " + result.value + ", Weight: " + result.weight);
        System.out.print("Items: ");
        for (int i = selectedItems.size() - 1; i >= 0; i--) {
            System.out.print(selectedItems.get(i) + " ");
        }
        System.out.println();
        System.out.printf("Execution time: %.3f seconds%n", executionTime);
        System.out.println("---------------------------------------------------");

    } catch (IOException e) {
        // Print the error message if an exception occurs while reading the file
        System.out.println("Error reading file: " + e.getMessage());
    }
}

}