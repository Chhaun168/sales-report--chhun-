import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SalesReport {

    public static void main(String[] args) {
        String targetMonth = args.length > 0 ? args[0] : "2026-09";
        Path dataDir = Path.of("data", targetMonth);

        if (!Files.exists(dataDir)) {
            System.err.println("Directory not found: " + dataDir);
            return;
        }

        double totalRevenue = 0;
        int totalItemsSold = 0;
        int validRowsCount = 0;
        List<String> errors = new ArrayList<>();
        Map<String, Double> branchRevenue = new HashMap<>();
        Map<String, Integer> categoryUnits = new HashMap<>();

        try (Stream<Path> paths = Files.walk(dataDir)) {
            List<Path> csvFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .collect(Collectors.toList());

            for (Path file : csvFiles) {
                List<String> lines = Files.readAllLines(file);
                if (lines.isEmpty()) continue;

                for (int i = 1; i < lines.size(); i++) {
                    String line = lines.get(i);
                    String[] parts = line.split(",");

                    if (parts.length < 10) {
                        errors.add("File " + file.getFileName() + " Line " + (i + 1) + ": Missing columns -> " + line);
                        continue;
                    }

                    try {
                        String branch = parts[0];
                        String category = parts[5];
                        int quantity = Integer.parseInt(parts[6]);
                        double unitPrice = Double.parseDouble(parts[7]);
                        double discount = Double.parseDouble(parts[8]);

                        double rowTotal = quantity * unitPrice * (1.0 - discount);

                        totalRevenue += rowTotal;
                        totalItemsSold += quantity;
                        validRowsCount++;

                        branchRevenue.put(branch, branchRevenue.getOrDefault(branch, 0.0) + rowTotal);
                        categoryUnits.put(category, categoryUnits.getOrDefault(category, 0) + quantity);

                    } catch (NumberFormatException e) {
                        errors.add("File " + file.getFileName() + " Line " + (i + 1) + ": Invalid number format -> " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            return;
        }

        System.out.println("==========================================");
        System.out.println("          SALES SUMMARY REPORT            ");
        System.out.println("          Month: " + targetMonth);
        System.out.println("==========================================");
        System.out.printf("Total Revenue:        $%.2f%n", totalRevenue);
        System.out.println("Valid Transactions:   " + validRowsCount);
        System.out.println("Total Items Sold:     " + totalItemsSold);
        System.out.println("------------------------------------------");
        System.out.println("REVENUE BY BRANCH:");
        branchRevenue.forEach((b, rev) -> System.out.printf("  %-5s : $%.2f%n", b, rev));
        System.out.println("------------------------------------------");
        System.out.println("UNITS SOLD BY CATEGORY:");
        categoryUnits.forEach((cat, qty) -> System.out.printf("  %-15s : %d units%n", cat, qty));
        System.out.println("------------------------------------------");
        System.out.println("PROCESSING ERRORS LOGGED: " + errors.size());
        if (!errors.isEmpty()) {
            errors.forEach(err -> System.out.println("  [ERROR] " + err));
        }
        System.out.println("==========================================");
    }
}
