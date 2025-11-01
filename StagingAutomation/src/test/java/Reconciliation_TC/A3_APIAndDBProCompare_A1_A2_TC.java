package Reconciliation_TC;

import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class A3_APIAndDBProCompare_A1_A2_TC {

    private static final Logger log = LoggerFactory.getLogger(A3_APIAndDBProCompare_A1_A2_TC.class);

    // ✅ Output directory inside resources
    private static final String OUTPUT_DIR = System.getProperty("user.dir")
            + File.separator + "src" + File.separator + "main"
            + File.separator + "resources" + File.separator + "output";

    private static boolean isValidStatus(String ticketStatus, String recordStatus) {
        return ("Ticketed".equalsIgnoreCase(ticketStatus) ||
                "Partially Ticketed".equalsIgnoreCase(ticketStatus))
                && "Current".equalsIgnoreCase(recordStatus);
    }

    public static Set<String> readPNRsFromCSV(String csvPath) throws Exception {
        Set<String> dbPNRs = new HashSet<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(csvPath))) {
            csvReader.readNext();
            String[] parts;
            while ((parts = csvReader.readNext()) != null) {
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

    public static List<String> readProviderCodesFromCSV(String csvPath) throws Exception {
        List<String> providerCodes = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(csvPath))) {
            csvReader.readNext();
            String[] parts;
            while ((parts = csvReader.readNext()) != null) {
                if (parts.length >= 4) { // ✅ يكفي 4 أعمدة بدل 5
                    String providerCode = parts[1].trim();
                    String ticketStatus = parts[3].trim();
                    String recordStatus = (parts.length > 4) ? parts[4].trim() : "Current"; // ✅ لو مش موجود نخليه Current
                    if (isValidStatus(ticketStatus, recordStatus)) {
                        providerCodes.add(providerCode);
                    }
                }
            }
        }
        return providerCodes;
    }

    public static List<String> findMissingPNRs(List<String> providerCodes, Set<String> dbPNRs) {
        return providerCodes.stream()
                .filter(code -> !dbPNRs.contains(code))
                .collect(Collectors.toList());
    }

    public static void writeMissingPNRsToCSV(List<String> missingPNRs, String filePath) {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs(); // ✅ Ensure directory exists

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("MissingPNR");
            for (String pnr : missingPNRs) {
                writer.println(pnr);
            }
            log.info("✅ Missing PNRs written to CSV: {}", filePath);
        } catch (IOException e) {
            log.error("❌ Error writing CSV: {}", e.getMessage());
        }
    }

    public static void runComparison(String apiCsvPath, String dbCsvPath) {
        String missingPnrsCsvPath = OUTPUT_DIR + File.separator + "MissingPNR.csv";
        try {
            List<String> providerCodes = readProviderCodesFromCSV(apiCsvPath);
            Set<String> dbPNRs = readPNRsFromCSV(dbCsvPath);

            List<String> missingPNRs = findMissingPNRs(providerCodes, dbPNRs);

            log.info("Total ProviderLocatorCodes from API: {}", providerCodes.size());
            log.info("Total PNRs from DB (SP+AIRLINE): {}", dbPNRs.size());
            log.info("Missing PNRs: {}", missingPNRs.size());

            writeMissingPNRsToCSV(missingPNRs, missingPnrsCsvPath);

            if (missingPNRs.isEmpty()) {
                log.info("✅ No missing PNRs found. DB and API are consistent.");
            } else {
                log.info("Sample Missing PNRs: {}", missingPNRs.stream().limit(5).toList());
            }

            log.info("🎯 Comparison Done.");

        } catch (Exception e) {
            log.error("❌ Comparison failed due to error", e);
        }
    }
}
