package Reconciliation_TC;

import org.testng.annotations.*;
import utilities.DatabasePage;
import utilities.DatabaseUtils;
import utilities.JsonUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabasePRO_TC {

    private DatabasePage databasePage;

    @BeforeClass
    public void setup() {
      //  driver = DatabaseUtils.initializeDriver();

        // Connect to DB via Utilities
        databasePage = DatabaseUtils.initializeDatabase();
    }

    @Test
    public void testDatabaseSelectQueryAndExport() throws SQLException {
        // ✅ Load query from JSON file
        String queryFilePath = "./src/test/resources/testDataFiles/DBPro.json";
        String query = JsonUtils.getQueryFromJson(queryFilePath);

        // ✅ Export query result to CSV
        String CSVPath = System.getProperty("user.home") + "/Desktop/queryResults.CSV";   // #### need update after partition d issue fix
        ResultSet rs = databasePage.executeSelectQuery(query);
        databasePage.saveResultSetToCSV(rs, CSVPath);

    }

    @AfterClass
    public void tearDown() {
        databasePage.closeConnection();
    }
}


