import java.sql.*;
import java.util.Scanner;

public class FactorialApp {
    private static final String DB_URL = "jdbc:sqlite:factorial.db";

    public static void main(String[] args) {
        initializeDatabase();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Factorial Calculator ---");
            System.out.println("1. Calculate Factorial (Iterative)");
            System.out.println("2. Calculate Factorial (Recursive)");
            System.out.println("3. View Calculation History");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice = getValidIntegerInput(scanner);

            switch (choice) {
                case 1 -> calculateFactorial(scanner, false);
                case 2 -> calculateFactorial(scanner, true);
                case 3 -> showCalculationHistory();
                case 4 -> {
                    System.out.println("Exiting application. Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // Initializes the SQLite database
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        number INTEGER NOT NULL,
                        method TEXT NOT NULL,
                        result TEXT NOT NULL,
                        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                    """;
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    // Gets valid integer input from the user
    private static int getValidIntegerInput(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter an integer: ");
            }
        }
    }

    // Handles factorial calculation
    private static void calculateFactorial(Scanner scanner, boolean isRecursive) {
        System.out.print("Enter a non-negative integer: ");
        int number = getValidIntegerInput(scanner);

        if (number < 0) {
            System.out.println("Factorial of negative numbers is undefined.");
            return;
        }

        long result;
        String method = isRecursive ? "Recursive" : "Iterative";

        if (isRecursive) {
            result = calculateFactorialRecursive(number);
        } else {
            result = calculateFactorialIterative(number);
        }

        System.out.printf("The %s factorial of %d is: %d%n", method, number, result);
        saveToDatabase(number, method, result);
    }

    // Iterative factorial calculation
    private static long calculateFactorialIterative(int number) {
        long result = 1;
        for (int i = 1; i <= number; i++) {
            result *= i;
        }
        return result;
    }

    // Recursive factorial calculation
    private static long calculateFactorialRecursive(int number) {
        if (number == 0 || number == 1) {
            return 1;
        }
        return number * calculateFactorialRecursive(number - 1);
    }

    // Saves calculation result to the SQLite database
    private static void saveToDatabase(int number, String method, long result) {
        String insertSQL = "INSERT INTO history (number, method, result) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, number);
            pstmt.setString(2, method);
            pstmt.setString(3, String.valueOf(result));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving to database: " + e.getMessage());
        }
    }

    // Displays calculation history from the SQLite database
    private static void showCalculationHistory() {
        String selectSQL = "SELECT id, number, method, result, timestamp FROM history ORDER BY timestamp DESC";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            System.out.println("\n--- Calculation History ---");
            while (rs.next()) {
                System.out.printf("ID: %d | Number: %d | Method: %s | Result: %s | Time: %s%n",
                        rs.getInt("id"),
                        rs.getInt("number"),
                        rs.getString("method"),
                        rs.getString("result"),
                        rs.getString("timestamp"));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving history: " + e.getMessage());
        }
    }
}
