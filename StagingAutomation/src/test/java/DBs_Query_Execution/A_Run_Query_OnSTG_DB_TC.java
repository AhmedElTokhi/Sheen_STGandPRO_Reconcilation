package DBs_Query_Execution;

import Generic_Classes.Connect_DB_And_Execute_Query;
import Generic_Classes.Get_DB_Config_STG;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import utilities.Write_To_CSV;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.*;

public class A_Run_Query_OnSTG_DB_TC {

    // ✅ Logger
    private static final Logger log = LoggerFactory.getLogger(A_Run_Query_OnSTG_DB_TC.class);

    // ✅ Constants
    private static final String DB_CONFIG_FILE = "testDataFiles/DB_Credentials_STG.json";
    private static final String QUERY_FILE = "testDataFiles/DB_Query_STG.json";
    private static final String OUTPUT_FILE = System.getProperty("user.dir") + "/output/All_DB_Results.csv";


    /**
     * ✅ Generic Utility
     * Reads a JSON file from resources and converts it into the given type.
     */
    private <T> T loadJsonFile(String fileName, TypeReference<T> typeRef) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream IS = A_Run_Query_OnSTG_DB_TC.class.getClassLoader().getResourceAsStream(fileName);
        if (IS == null) {
            throw new RuntimeException(fileName + " not found in resources folder");
        }
        return mapper.readValue(IS, typeRef);
    }

    /**
     * ✅ Main Test
     * 1. Load DB configs
     * 2. Load SQL query
     * 3. Fetch data from multiple DBs
     * 4. Save results in CSV
     */
    @Test
    public void FetchData_And_Save_To_CSV() {
        log.info("🚀 Starting DB -> CSV Test...");

        try {
            // Load DB configs
            List<Get_DB_Config_STG> configs = loadJsonFile(DB_CONFIG_FILE, new TypeReference<List<Get_DB_Config_STG>>() {});

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
            Write_To_CSV.saveAsCSV(OUTPUT_FILE, combinedResults);

            log.info("✅ CSV file created successfully at: {}", OUTPUT_FILE);

        } catch (Exception ex) {
            log.error("❌ Error during CSV generation", ex);
        }

        log.info("🎯 Done Test.");
    }

    /**
     * 🔹 Connect to multiple databases and fetch query results.
     */
    private List<Map<String, Object>> fetchDataFromDatabases(List<Get_DB_Config_STG> configs, String query) {
        List<Map<String, Object>> combinedResults = new ArrayList<>();
        Map<String, Integer> summary = new LinkedHashMap<>(); // 📝 يخزن عدد الصفوف لكل DB

        for (Get_DB_Config_STG cfg : configs) {
            try (Connection conn = Connect_DB_And_Execute_Query.Connection_To_DB(cfg.getHostname(), cfg.getUsername(), cfg.getPassword())) {
                List<Map<String, Object>> rows = Connect_DB_And_Execute_Query.runQuery(conn, query);

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
