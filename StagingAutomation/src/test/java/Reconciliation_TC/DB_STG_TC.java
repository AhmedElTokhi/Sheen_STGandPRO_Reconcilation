package Reconciliation_TC;

import StagingDB.DBConnection;
import StagingDB.DatabaseReader;
import StagingDB.DbConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;
import utilities.CSVUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.util.*;

public class DB_STG_TC {

    // ✅ Constants
    private static final String DB_CONFIG_FILE = "dbConfig.json";
    private static final String QUERY_FILE = "DB_Query.json";
    private static final String OUTPUT_FILE = System.getProperty("user.home") + "/Downloads/All_DB_Results.csv";

    /**
     * ✅ Generic Utility
     * Reads a JSON file from resources and converts it into the given type.
     */
    private <T> T loadJsonFile(String fileName, TypeReference<T> typeRef) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = DB_STG_TC.class.getClassLoader().getResourceAsStream(fileName);
        if (is == null) {
            throw new RuntimeException(fileName + " not found in resources folder");
        }
        return mapper.readValue(is, typeRef);
    }

    /**
     * ✅ Main Test
     * 1. Load DB configs
     * 2. Load SQL query
     * 3. Fetch data from multiple DBs
     * 4. Save results in CSV
     */
    @Test
    public void saveCSV() {
        System.out.println("Starting DB -> CSV Test...");

        try {
            // Load DB configs
            List<DbConfig> configs = loadJsonFile(DB_CONFIG_FILE, new TypeReference<List<DbConfig>>() {});

            // Load query
            String QUERY = loadJsonFile(QUERY_FILE, new TypeReference<Map<String, String>>() {}).get("query");

            // Fetch data
            List<Map<String, Object>> combinedResults = fetchDataFromDatabases(configs, QUERY);

            // Save to CSV
            CSVUtils.saveAsCSV(OUTPUT_FILE, combinedResults);

            System.out.println("✅ CSV file created: " + OUTPUT_FILE);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("Done Test.");
    }

    /**
     * 🔹 Connect to multiple databases and fetch query results.
     */
    private List<Map<String, Object>> fetchDataFromDatabases(List<DbConfig> configs, String query) {
        List<Map<String, Object>> combinedResults = new ArrayList<>();
        Map<String, Integer> summary = new LinkedHashMap<>(); // 📝 يخزن عدد الصفوف لكل DB

        for (DbConfig cfg : configs) {
            try (Connection conn = DBConnection.getConnection(cfg.getHostname(), cfg.getUsername(), cfg.getPassword())) {
                List<Map<String, Object>> rows = DatabaseReader.runQuery(conn, query);

                System.out.println("--------------------------------------------------");
                System.out.println("✅ Connected to DB: " + cfg.getHostname());
                System.out.println("📊 Rows fetched: " + rows.size());
                System.out.println("--------------------------------------------------\n");

                summary.put(cfg.getHostname(), rows.size());

                for (Map<String, Object> r : rows) {
                    Map<String, Object> newRow = new LinkedHashMap<>();
                    newRow.put("SourceDB", cfg.getHostname());
                    newRow.putAll(r);
                    combinedResults.add(newRow);
                }
            } catch (Exception e) {
                System.err.println("  !! Error connecting/querying " + cfg.getHostname() + " : " + e.getMessage());
                summary.put(cfg.getHostname(), 0);
            }
        }

        // 📝 طباعة الـ Summary بعد ما يخلص كل الـ DBs
        System.out.println("\n========= 📊 QUERY SUMMARY 📊 =========");
        int total = 0;
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            System.out.println("DB: " + entry.getKey() + " -> Rows: " + entry.getValue());
            total += entry.getValue();
        }
        System.out.println("--------------------------------------");
        System.out.println("TOTAL Rows from all DBs = " + total);
        System.out.println("======================================");

        return combinedResults;
    }

}
