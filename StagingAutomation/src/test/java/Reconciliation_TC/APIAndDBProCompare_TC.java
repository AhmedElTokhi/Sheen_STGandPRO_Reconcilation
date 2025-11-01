package Reconciliation_TC;

import org.testng.annotations.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class APIAndDBProCompare_TC {

    /**
     * 🔹 Helper Method
     * Checks if TicketStatus and RecordStatus are valid:
     * - TicketStatus should be "Ticketed" OR "Partially Ticketed"
     * - RecordStatus should be "Current"
     */
    private static boolean isValidStatus(String ticketStatus, String recordStatus) {
        return ("Ticketed".equalsIgnoreCase(ticketStatus) ||
                "Partially Ticketed".equalsIgnoreCase(ticketStatus))
                && "Current".equalsIgnoreCase(recordStatus);
    }

    /**
     * 📘 readPNRsFromCSV
     * - Reads the DB CSV file.
     * - Collects both SP_PNR (column 0) and AIRLINE_PNR (column 1).
     * - Returns a Set to avoid duplicates.
     */
    public static Set<String> readPNRsFromCSV(String csvPath) throws IOException {
        Set<String> dbPNRs = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String spPnr = parts[0].trim();
                    String airlinePnr = parts[1].trim();
                    if (!spPnr.isEmpty()) dbPNRs.add(spPnr);
                    if (!airlinePnr.isEmpty()) dbPNRs.add(airlinePnr);
                }
            }
        }
        return dbPNRs;
    }

    /**
     * 📘 readProviderCodesFromCSV
     * - Reads the API CSV file.
     * - Extracts ProviderLocatorCodes only when:
     *   (TicketStatus = "Ticketed" OR "Partially Ticketed")
     *   AND RecordStatus = "Current".
     */
    public static List<String> readProviderCodesFromCSV(String csvPath) throws IOException {
        List<String> providerCodes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String providerCode = parts[1].trim();
                    String ticketStatus = parts[3].trim();
                    String recordStatus = parts[4].trim();

                    if (isValidStatus(ticketStatus, recordStatus)) {
                        providerCodes.add(providerCode);
                    }
                }
            }
        }
        return providerCodes;
    }

    /**
     * 📘 findMissingPNRs
     * - Compares ProviderLocatorCodes from API with PNRs from DB.
     */
    public static List<String> findMissingPNRs(List<String> providerCodes, Set<String> dbPNRs) {
        return providerCodes.stream()
                .filter(code -> !dbPNRs.contains(code))
                .collect(Collectors.toList());
    }

    /**
     * ✅ Write missing PNRs to a CSV file
     */
    public static void writeMissingPNRsToCSV(List<String> missingPNRs, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("MissingPNR");
            for (String pnr : missingPNRs) {
                writer.println(pnr);
            }
            System.out.println("✅ Missing PNRs written to CSV: " + filePath);
        } catch (IOException e) {
            System.err.println("❌ Error writing CSV: " + e.getMessage());
        }
    }

    /**
     * ▶️ Test
     */
    @Test
    public static void APIAndDBComparison() throws IOException {
        String csvFile = "./provider_codes_by_credential.csv";   // API CSV
        String dbCSVFile = System.getProperty("user.home") + "/Desktop/queryResults.csv"; // DB CSV

        List<String> providerCodes = readProviderCodesFromCSV(csvFile);
        Set<String> dbPNRs = readPNRsFromCSV(dbCSVFile);

        List<String> missingPNRs = findMissingPNRs(providerCodes, dbPNRs);

        System.out.println("Total ProviderLocatorCodes from API: " + providerCodes.size());
        System.out.println("Total PNRs from DB (SP+AIRLINE): " + dbPNRs.size());
        System.out.println("Missing PNRs: " + missingPNRs.size());

        writeMissingPNRsToCSV(missingPNRs, "./missingPNRs.csv");
    }
}
