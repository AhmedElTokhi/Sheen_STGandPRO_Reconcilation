package Reconciliation_TC;

import StagingDB_Pg.DBConnection_Pg;
import StagingDB_Pg.DatabaseReader_Pg;
import StagingDB_Pg.DbConfig_Pg;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import utilities.CSVUtils;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;

public class B1_DB_STG_TC {

    // ✅ Logger
    private static final Logger log = LoggerFactory.getLogger(B1_DB_STG_TC.class);

    // ✅ Constants
    private static final String DB_CONFIG_FILE = "dbConfig.json";
    private static final String QUERY_FILE = "DB_Query.json";
    private static final String OUTPUT_FILE =
            System.getProperty("user.dir") + "/output/All_DB_Results.csv";

    /**
     * ✅ Generic Utility
     * Reads a JSON file from resources and converts it into the given type.
     */
    private <T> T loadJsonFile(String fileName, TypeReference<T> typeRef) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = B1_DB_STG_TC.class.getClassLoader().getResourceAsStream(fileName);
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
        log.info("🚀 Starting DB -> CSV Test...");

        try {
            // Load DB configs
            List<DbConfig_Pg> configs = loadJsonFile(DB_CONFIG_FILE, new TypeReference<List<DbConfig_Pg>>() {});

            // Load query
            String QUERY = loadJsonFile(QUERY_FILE, new TypeReference<Map<String, String>>() {}).get("query");

            // Fetch data
            List<Map<String, Object>> combinedResults = fetchDataFromDatabases(configs, QUERY);

            // Ensure output directory exists
            File outputDir = new File(System.getProperty("user.dir") + "/output");
            if (!outputDir.exists()) {
                boolean created = outputDir.mkdirs();
                if (created) {
                    log.info("📂 Created output directory at {}", outputDir.getAbsolutePath());
                } else {
                    log.warn("⚠️ Failed to create output directory, check permissions!");
                }
            }

            // Save to CSV
            CSVUtils.saveAsCSV(OUTPUT_FILE, combinedResults);

            log.info("✅ CSV file created successfully at: {}", OUTPUT_FILE);

        } catch (Exception ex) {
            log.error("❌ Error during CSV generation", ex);
        }

        log.info("🎯 Done Test.");
    }

    /**
     * 🔹 Connect to multiple databases and fetch query results.
     */
    private List<Map<String, Object>> fetchDataFromDatabases(List<DbConfig_Pg> configs, String query) {
        List<Map<String, Object>> combinedResults = new ArrayList<>();
        Map<String, Integer> summary = new LinkedHashMap<>(); // 📝 يخزن عدد الصفوف لكل DB

        for (DbConfig_Pg cfg : configs) {
            try (Connection conn = DBConnection_Pg.getConnection(cfg.getHostname(), cfg.getUsername(), cfg.getPassword())) {
                List<Map<String, Object>> rows = DatabaseReader_Pg.runQuery(conn, query);

                log.info("--------------------------------------------------");
                log.info("✅ Connected to DB: {}", cfg.getHostname());
                log.info("📊 Rows fetched: {}", rows.size());
                log.info("--------------------------------------------------\n");

                summary.put(cfg.getHostname(), rows.size());

                for (Map<String, Object> r : rows) {
                    Map<String, Object> newRow = new LinkedHashMap<>();
                    newRow.put("SourceDB", cfg.getHostname());
                    newRow.putAll(r);
                    combinedResults.add(newRow);
                }
            } catch (Exception e) {
                log.error("❌ Error connecting/querying DB {}: {}", cfg.getHostname(), e.getMessage());
                summary.put(cfg.getHostname(), 0);
            }
        }

        // 📝 طباعة الـ Summary بعد ما يخلص كل الـ DBs
        log.info("\n========= 📊 QUERY SUMMARY 📊 =========");
        int total = 0;
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            log.info("DB: {} -> Rows: {}", entry.getKey(), entry.getValue());
            total += entry.getValue();
        }
        log.info("--------------------------------------");
        log.info("TOTAL Rows from all DBs = {}", total);
        log.info("======================================");

        return combinedResults;
    }

}
