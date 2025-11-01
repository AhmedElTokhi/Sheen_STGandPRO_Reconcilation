package Reconciliation_TC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ✅ DBSTG_APIRun_TC
 * - Runs DB_STG_TC → generates DB results CSV from multiple databases
 * - Runs retriveFilteredPNRFromAPI_TC → reads DB CSV, calls API, generates API results CSV
 * - Saves all output files in /output directory
 */
public class B0_runB1_B2_TC {

    // ✅ Logger for tracking workflow progress
    private static final Logger log = LoggerFactory.getLogger(B0_runB1_B2_TC.class);

    // ✅ Output directory where all files will be saved
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/output";

    /**
     * ▶️ Main runner method
     * Steps:
     * 1. Run DB_STG_TC → fetch data from DBs and save to CSV
     * 2. Run retriveFilteredPNRFromAPI_TC → read DB CSV, call API, save API results to CSV
     * 3. Log file locations and workflow completion
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        log.info("🚀 Starting RunAPI Workflow...");

        try {
            // 🔹 Step 1: Run DB_STG_TC → generates DB CSV
            B1_DB_STG_TC dbTest = new B1_DB_STG_TC();
            dbTest.saveCSV(); // دا بيعمل CSV جوه output folder
            String dbCsvPath = OUTPUT_DIR + "/output/All_DB_Results.csv";

            // 🔹 Step 2: Run retriveFilteredPNRFromAPI_TC → reads DB CSV, calls API, saves API results CSV
            B2_retriveFilteredPNRFromAPI_TC apiProcessor = new B2_retriveFilteredPNRFromAPI_TC();
            String apiResultsPath = apiProcessor.processRecordsFromExcel(dbCsvPath);

            // 🔹 Logs after successful completion
            log.info("🎉 Workflow completed successfully!");
            log.info("📂 DB CSV saved at: {}", dbCsvPath);
            log.info("📂 API Results CSV saved at: {}", apiResultsPath);

        } catch (Exception e) {
            log.error("❌ Error during RunAPI workflow", e);
        }
    }
}
