package Reconciliation_TC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import utilities.DatabasePage;
import utilities.DatabaseUtils;
import utilities.JsonUtils;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class A2_DatabasePRO_TC {

    private static final Logger log = LoggerFactory.getLogger(A2_DatabasePRO_TC.class);

    private DatabasePage databasePage;

    // ✅ Output directory inside project
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + File.separator + "output";
    private static final String DB_CSV = OUTPUT_DIR + File.separator + "queryResults_PRO.csv";


    /**
     * ✅ Setup method
     * - Connects to PRO Database using DatabaseUtils
     * - Ensures that the output directory exists
     */
    @BeforeClass
    public void setup() {
        log.info("🚀 Connecting to PRO Database...");
        // Connect to DB via Utilities
        databasePage = DatabaseUtils.initializeDatabase();

        // Ensure output directory exists
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("📂 Created output directory at {}", OUTPUT_DIR);
        }
    }

    /**
     * ✅ Test: Execute DB Select Query and Export
     * Steps:
     * 1. Load SQL query from JSON file (DBPro.json)
     * 2. Execute query against PRO Database
     * 3. Save query results into CSV inside /output folder
     * 4. Log success or fail if error occurs
     */
    @Test
    public void testDatabaseSelectQueryAndExport() {
        try {
            // ✅ Load query from JSON file
            String queryFilePath = "./src/main/resources/DBPro.json";
            String query = JsonUtils.getQueryFromJson(queryFilePath);

            // ✅ Execute query
            ResultSet rs = databasePage.executeSelectQuery(query);

            // ✅ Export query result to CSV
            databasePage.saveResultSetToCSV(rs, DB_CSV);
            log.info("✅ Query results saved at {}", DB_CSV);

        } catch (SQLException e) {
            log.error("❌ Database query failed", e);
            Assert.fail("Database query failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error during test", e);
            Assert.fail("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * ✅ TearDown method
     * - Closes the PRO Database connection after tests
     */
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (databasePage != null) {
            databasePage.closeConnection();
            log.info("🔌 Database connection closed.");
        }
    }
}
