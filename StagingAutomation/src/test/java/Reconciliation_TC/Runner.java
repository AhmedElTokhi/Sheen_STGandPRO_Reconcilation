package Reconciliation_TC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.apache.poi.ss.usermodel.Workbook;
import utilities.ExcelCompare;

import java.io.File;

import static utilities.CSVToExcelConverter.convertCSVToWorkbook;

/**
 * 🔹 Runner Class
 * - Contains multiple reconciliation methods (Method1, Method3, Method4).
 * - Each method can run independently using TestNG (@Test).
 * - Output files are stored in /output folder.
 */
public class Runner {

    // ------------------------------------------------------
    // 🔹 Logger for tracking steps and errors
    // ------------------------------------------------------
    private static final Logger log = LoggerFactory.getLogger(Runner.class);

    // ------------------------------------------------------
    // 🔹 Common Paths for Input / Output Files
    // ------------------------------------------------------
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/src/main/resources/output";

    private static final String API_CSV = OUTPUT_DIR + "/provider_codes_by_credential.csv";   // Output from A1
    private static final String DB_CSV = OUTPUT_DIR + "/queryResults_PRO.csv";         // Output from A2
    private static final String MISSING_PNRS_CSV = OUTPUT_DIR + "/missingPNRs.csv";           // Output from A3
    private static final String ALL_DB_RESULTS = OUTPUT_DIR + "/All_DB_Results.csv";          // Output from B1
    private static final String MISSING_PNR = OUTPUT_DIR + "/Missing_PNR.csv";                // Output from A3 (staging)
    private static final String DB_CSV_B1 = OUTPUT_DIR + "/All_DB_Results.csv";               // DB results from B1
    private static final String OUTPUT_FILE_A3_B1 = OUTPUT_DIR + "/All_DB_Results_withExistence.xlsx"; // Output from Method4

    // ----------------------------------------------------------------
    // 🔹 METHOD 1: (A1 + A2 + A3)
    // ----------------------------------------------------------------

    /**
     * ▶️ Step 1: Run A1
     * - Executes A1_GetGalileoPNR_TC
     * - Fetches PNRs from Galileo API
     * - Saves results in provider_codes_by_credential.csv
     */
    private static void runA1() {
        log.info("▶️ Running A1 (API Test)...");

        // ✅ Ensure output directory exists
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
            log.info("📁 Created output directory at: {}", OUTPUT_DIR);
        }

        try {
            A1_GetGalileoPNR_TC a1 = new A1_GetGalileoPNR_TC();
            a1.fetchAndSaveRecordsByCredential(); // run test
            log.info("✅ A1 completed successfully. File saved: {}", API_CSV);
        } catch (Exception e) {
            log.error("❌ A1 failed: {}", e.getMessage(), e);
        }
    }

    /**
     * ▶️ Step 2: Run A2
     * - Executes A2_DatabasePRO_TC
     * - Connects to PRO database
     * - Runs query and saves results in queryResults_PRO.csv
     */
    private static void runA2() {
        log.info("▶️ Running A2 (Database Test)...");
        try {
            A2_DatabasePRO_TC a2 = new A2_DatabasePRO_TC();
            a2.setup(); // open DB connection
            a2.testDatabaseSelectQueryAndExport(); // run query & export CSV
            a2.tearDown(); // close DB connection
            log.info("✅ A2 completed successfully. File saved: {}", DB_CSV);
        } catch (Exception e) {
            log.error("❌ A2 failed: {}", e.getMessage(), e);
        }
    }

    /**
     * ▶️ Step 3: Run A3
     * - Executes A3_APIAndDBProCompare_A1_A2_TC
     * - Compares API results vs DB results
     * - Saves missing PNRs in missingPNRs.csv
     */
    private static void runComparison() {
        log.info("▶️ Running API vs DB Comparison (Reconciliation)...");
        try {
            A3_APIAndDBProCompare_A1_A2_TC.runComparison(API_CSV, DB_CSV);
            log.info("✅ Comparison completed. Missing PNRs saved at {}", MISSING_PNRS_CSV);
        } catch (Exception e) {
            log.error("❌ Comparison failed: {}", e.getMessage(), e);
        }
    }

    /**
     * 🚀 METHOD 1 RUNNER
     * Steps:
     * 1. Run API test (A1)
     * 2. Run Database test (A2)
     * 3. Run Comparison (A3)
     */
    @Test
    public void method1_RunA1A2A3() {
        log.info("🚀 Starting Method1 (A1 + A2 + A3)...");
   //     runA1();
        //runA2();
        runComparison();

        // ✅ Assertions to validate output files
        Assert.assertTrue(new File(API_CSV).exists(), "❌ API CSV should be generated in Method1");
        Assert.assertTrue(new File(DB_CSV).exists(), "❌ DB CSV should be generated in Method1");
        Assert.assertTrue(new File(MISSING_PNRS_CSV).exists(), "❌ Missing PNRs CSV should be generated in Method1");

        log.info("🎯 Method1 Completed.");
    }

    // ----------------------------------------------------------------
    // 🔹 METHOD 3: (B1 + B2)
    // ----------------------------------------------------------------

    /**
     * ▶️ Step 1: Run B1
     * - Executes B1_DB_STG_TC
     * - Fetches data from multiple staging DBs
     * - Saves results into All_DB_Results.csv
     *
     * ▶️ Step 2: Run B2
     * - Executes B2_retriveFilteredPNRFromAPI_TC
     * - Reads DB CSV from B1
     * - Calls API and saves API results
     */
    private static void runB1B2() {
        log.info("▶️ Running Method3 (B1 + B2)...");
        try {
            // Step 1 → Run B1
            B1_DB_STG_TC dbTest = new B1_DB_STG_TC();
            dbTest.saveCSV(); // Creates All_DB_Results.csv
            String dbCsvPath = ALL_DB_RESULTS;

            // Step 2 → Run B2
            B2_retriveFilteredPNRFromAPI_TC apiProcessor = new B2_retriveFilteredPNRFromAPI_TC();
            String apiResultsPath = apiProcessor.processRecordsFromExcel(dbCsvPath);

            log.info("🎉 Method3 completed successfully!");
            log.info("📂 DB CSV saved at: {}", dbCsvPath);
            log.info("📂 API Results CSV saved at: {}", apiResultsPath);

        } catch (Exception e) {
            log.error("❌ Method3 (B1+B2) failed", e);
        }
    }

    /**
     * 🚀 METHOD 3 RUNNER
     */
    @Test
    public void method3_RunB1B2() {
        log.info("🚀 Starting Method3 (B1 + B2)...");
       // runB1B2();

        // ✅ Assertions to validate output files
        Assert.assertTrue(new File(ALL_DB_RESULTS).exists(), "❌ DB CSV should be generated in Method3");

        log.info("🎯 Method3 Completed.");
    }

    // ----------------------------------------------------------------
    // 🔹 METHOD 4: Compare Missing (A3) with B1 Output
    // ----------------------------------------------------------------

    /**
     * ▶️ Step 1: Load Missing PNRs (from A3) CSV → convert to Workbook
     * ▶️ Step 2: Load DB Results (from B1) CSV → convert to Workbook
     * ▶️ Step 3: Compare both sheets
     * - Marks existence in DB results
     * - Saves output as All_DB_Results_withExistence.xlsx
     */
    private static void runCompareMissingWithB1() {
        log.info("▶️ Running Method4 (Compare Missing PNR with B1 output)...");
        try {
            // ✅ Check files exist
            File dbFile = new File(DB_CSV_B1);
            File missingFile = new File(MISSING_PNR);

            if (!dbFile.exists() || !missingFile.exists()) {
                throw new RuntimeException("❌ One of the CSV files not found in /output!");
            }

            // 🔄 Convert CSV → Workbook
            Workbook dbWorkbook = convertCSVToWorkbook(DB_CSV_B1);
            Workbook missingWorkbook = convertCSVToWorkbook(MISSING_PNR);

            // 🚀 Perform comparison
            ExcelCompare.markExistence(
                    dbWorkbook.getSheetAt(0),  // DB sheet
                    missingWorkbook.getSheetAt(0), // Missing PNR sheet
                    OUTPUT_FILE_A3_B1
            );

            log.info("🎉 Method4 comparison completed successfully!");
            log.info("📊 Output Excel saved at: {}", OUTPUT_FILE_A3_B1);

        } catch (Exception e) {
            log.error("❌ Method4 failed", e);
        }
    }

    /**
     * 🚀 METHOD 4 RUNNER
     */
    @Test
    public void method4_CompareMissingWithB1() {
        log.info("🚀 Starting Method4 (Missing PNR vs B1 Output)...");
        runCompareMissingWithB1();

        // ✅ Assertions to validate output file
        Assert.assertTrue(new File(OUTPUT_FILE_A3_B1).exists(), "❌ Excel comparison file should be generated in Method4");

        log.info("🎯 Method4 Completed.");
    }
}









// Method
// run A1 ,A2
//A3 'as compare only



////    Method2: BY AHMED AYMAN
// Compare Missing in Email + Zoho Export
//    Misssing _manula
//        A4
//    A5 Compare only
//
//    Method 3: B1 +B2   DONE
//    as areplacement for B0

//    Method 4: Compare Emil Missing with B1 output



































