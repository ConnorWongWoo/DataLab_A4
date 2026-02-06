package org.jfree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JFrame;


public class Main {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(
            new InputStreamReader(
                Main.class.getResourceAsStream("/CarDataset.csv")
            )
        );
        List<Car> carList = new ArrayList<>();

        // maps to aggregate by brand
        Map<String, Double> brandTotalPrice = new HashMap<>();
        Map<String, Integer> brandTotalSeats = new HashMap<>();
        Map<String, Integer> brandCount = new HashMap<>();

        // skip header
        String line = br.readLine();
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = parseCSVLine(line);
            if (parts.length < 11) continue; // skip malformed lines

            String company = parts[0].trim();
            String model = parts[1].trim();
            String engine = parts[2].trim();
            String cc = parts[3].trim();

            double priceValue = parsePrice(parts[7]);
            int priceInt = (int) Math.round(priceValue);

            int seats = parseSeats(parts[9]);

            int horsePower = parseIntSafe(parts[4]);
            int maxSpeed = parseIntSafe(parts[5]);
            int performance = parseIntSafe(parts[6]);
            String fuelType = parts[8].trim();
            String torque = parts[10].trim();

            Car car = new Car(company, model, engine, cc, horsePower, maxSpeed, performance, priceInt, fuelType, seats, torque);
            carList.add(car);

            // update aggregates
            brandTotalPrice.put(company, brandTotalPrice.getOrDefault(company, 0.0) + priceValue);
            brandTotalSeats.put(company, brandTotalSeats.getOrDefault(company, 0) + seats);
            brandCount.put(company, brandCount.getOrDefault(company, 0) + 1);
        }
        br.close();

        // print all cars (optional)
        for (Car c : carList) {
            System.out.println(c);
        }

        // compute score per brand: totalPrice / totalSeats (lower is better)
     List<Map.Entry<String, Double>> ranking = new ArrayList<>();

        for (String brand : brandTotalPrice.keySet()) {
            double totalPrice = brandTotalPrice.getOrDefault(brand, 0.0);
            int totalSeats = brandTotalSeats.getOrDefault(brand, 0);

            if (totalSeats <= 0) continue;

            double dollarsPerSeat = totalPrice / totalSeats;

            // 🔑 LOG AXIS SAFETY CHECK
            if (dollarsPerSeat <= 0) continue;

            ranking.add(Map.entry(brand, dollarsPerSeat));
        }

        // sort ascending by score (lower price per seat is better)
        ranking.sort(Comparator.comparingDouble(Map.Entry<String, Double>::getValue));
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> e : ranking) {
            dataset.addValue(
                e.getValue(),        // dollars per seat
                "Dollars per Seat",  // series name
                e.getKey()           // brand
            );
        }


        if (!ranking.isEmpty()) {
            Map.Entry<String, Double> best = ranking.get(0);
            String bestBrand = best.getKey();
            double bestScore = best.getValue();
            int seats = brandTotalSeats.getOrDefault(bestBrand, 0);
            double price = brandTotalPrice.getOrDefault(bestBrand, 0.0);
            int count = brandCount.getOrDefault(bestBrand, 0);

            System.out.printf("Best brand by dollars/seat: %s (score=$%.2f per seat)\n", bestBrand, bestScore);
            System.out.printf("  Models: %d, Total seats: %d, Total price: $%.2f\n", count, seats, price);
        } else {
            System.out.println("No brand data available to rank.");
        }

        System.out.println("\nFull ranking (brand : dollars-per-seat):");
        for (Map.Entry<String, Double> e : ranking) {
            System.out.printf("%s : $%.2f per seat\n", e.getKey(), e.getValue());
        }

        showChart(dataset);

    }

    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private static int parseIntSafe(String s) {
        double v = extractFirstNumber(s);
        return (int) Math.round(v);
    }

    private static double extractFirstNumber(String s) {
        if (s == null) return 0;
        String str = s.trim();
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        java.util.regex.Matcher m = p.matcher(str);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group());
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    // parse price: average all numbers found in the price field (handles ranges)
    private static double parsePrice(String s) {
        if (s == null) return 0;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        java.util.regex.Matcher m = p.matcher(s);
        double sum = 0;
        int count = 0;
        while (m.find()) {
            try {
                sum += Double.parseDouble(m.group());
                count++;
            } catch (Exception ex) {
                // ignore
            }
        }
        if (count == 0) return 0;
        return sum / count;
    }

    // parse seats: sum all integer values found (handles "2+2" -> 4)
    private static int parseSeats(String s) {
        if (s == null) return 0;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher m = p.matcher(s);
        int sum = 0;
        while (m.find()) {
            try {
                sum += Integer.parseInt(m.group());
            } catch (Exception ex) {
                // ignore
            }
        }
        return sum;
    }

    private static void showChart(DefaultCategoryDataset dataset) {

    JFreeChart chart = ChartFactory.createBarChart(
            "Average Cost per Seat by Car Brand",
            "Car Brand",
            "Dollars per Seat ($)",
            dataset
    );

    CategoryPlot plot = chart.getCategoryPlot();

    // Log scale for huge price range
    // LogarithmicAxis logAxis = new LogarithmicAxis("Dollars per Seat ($)");
    // plot.setRangeAxis(logAxis);

    // Rotate brand labels so they don’t overlap
    plot.getDomainAxis().setCategoryLabelPositions(
        org.jfree.chart.axis.CategoryLabelPositions.UP_45
    );

    JFrame frame = new JFrame("Car Seat Cost Analysis");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(new ChartPanel(chart));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}

}
