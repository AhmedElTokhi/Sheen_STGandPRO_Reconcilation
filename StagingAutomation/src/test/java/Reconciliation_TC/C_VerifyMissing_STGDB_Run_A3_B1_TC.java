package Reconciliation_TC;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.CSVToExcelConverter;
import utilities.ExcelCompare;

import java.io.File;

import static utilities.CSVToExcelConverter.convertCSVToWorkbook;

public class C_VerifyMissing_STGDB_Run_A3_B1_TC {

    private static final Logger log = LoggerFactory.getLogger(C_VerifyMissing_STGDB_Run_A3_B1_TC.class);

    //private static final String OUTPUT_DIR = System.getProperty("user.dir")+"/src/main/resources/output";
    private static final String OUTPUT_DIR = System.getProperty("user.dir")+"/STGandPROReconcilation/StagingAutomation/src/main/resources/output";

    private static final String DB_CSV = OUTPUT_DIR + "/All_DB_Results.csv";
    private static final String MISSING_CSV = OUTPUT_DIR + "/Missing_PNR.csv";
    private static final String OUTPUT_FILE = OUTPUT_DIR + "/All_DB_Results_withExistence.xlsx";

    public static void main(String[] args) {
        log.info("🚀 Starting CSV → Excel Comparison...");

        try {
            // ✅ تأكد إن الملفات موجودة
            File dbFile = new File(DB_CSV);
            File missingFile = new File(MISSING_CSV);

            if (!dbFile.exists() || !missingFile.exists()) {
                throw new RuntimeException("❌ One of the CSV files not found in /output!");
            }

            // 🔄 حوّل CSV إلى Workbook
            Workbook dbWorkbook = convertCSVToWorkbook(DB_CSV);
            Workbook missingWorkbook = convertCSVToWorkbook(MISSING_CSV);

            // 🚀 نفذ المقارنة
            ExcelCompare.markExistence(
                    dbWorkbook.getSheetAt(0),  // sheet من db
                    missingWorkbook.getSheetAt(0), // sheet من missing
                    OUTPUT_FILE
            );

            log.info("🎉 Comparison completed successfully!");
            log.info("📊 Output Excel saved at: {}", OUTPUT_FILE);

        } catch (Exception e) {
            log.error("❌ Error during comparison", e);
        }
    }
}
