package DBs_Query_Execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;
import Generic_Classes.Execute_And_Save_To_CSV_Production_DB;
import Generic_Classes.Connect_To_Production_DB;
import utilities.Read_Query_From_Json;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

public class B_Run_Query_OnProduction_DB_TC {

    private static final Logger log = LoggerFactory.getLogger(B_Run_Query_OnProduction_DB_TC.class);

    private Execute_And_Save_To_CSV_Production_DB database_OP;

    // ✅ Output directory inside a project
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + File.separator + "output";
    private static final String DB_CSV = OUTPUT_DIR + File.separator + "queryResults_PRO.csv";


    /**
     * ✅ Setup method
     * - Connects to PRO Database using Connect_To_Production_DB
     * - Ensures that the output directory exists
     */
    @BeforeClass
    public void setup() {
        log.info("🚀 Connecting to PRO Database...");
        // Connect to DB via Utilities
        database_OP = Connect_To_Production_DB.initializeDatabase();

        // Ensure output directory exists
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("📂 Created output directory at {}", OUTPUT_DIR);
        }
    }


    /**
     * ✅ Test: Execute DB Select Query and Export
     * Steps:
     * 1. Load SQL query from JSON file (DB_Query_Production.json)
     * 2. Execute query against PRO Database
     * 3. Save query results into CSV inside /output folder
     * 4. Log success or fail if error occurs
     */
    @Test
    public void FetchData_And_Save_To_CSV() {
        try {
            // ✅ Load query from JSON file
            String queryFilePath = "./src/test/resources/testDataFiles/DB_Query_Production.json";
            String query = Read_Query_From_Json.getQueryFromJson(queryFilePath);

            // ✅ Execute query
            ResultSet queryResult = database_OP.executeSelectQuery(query);

            // ✅ Export query result to CSV
            database_OP.saveResultSetToCSV("Production",queryResult, DB_CSV);
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
        if (database_OP != null) {
            database_OP.closeConnection();
            log.info("🔌 Database connection closed.");
        }
    }
}
