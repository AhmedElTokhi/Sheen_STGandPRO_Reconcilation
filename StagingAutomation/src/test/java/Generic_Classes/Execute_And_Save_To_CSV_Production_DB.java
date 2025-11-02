package Generic_Classes;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;

public class Execute_And_Save_To_CSV_Production_DB {
    private Connection connection;

    // Constructor بياخد الـ Connection مباشرة
    public Execute_And_Save_To_CSV_Production_DB(Connection connection) {
        this.connection = connection;
    }

    // 1️⃣ Execute query and return ResultSet
    public ResultSet executeSelectQuery(String query) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("❌ Database connection is not established.");
        }

        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        return statement.executeQuery(query);
    }

    // 2️⃣ Save ResultSet into CSV file
    public void saveResultSetToCSV(String DBSource, ResultSet resultSet, String csvFilePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath))) {

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Write header row
            writer.print("SourceDB,");
            for (int i = 1; i <= columnCount; i++) {
                writer.print(metaData.getColumnName(i));
                if (i < columnCount) writer.print(",");
            }
            writer.println();

            // Write data rows

            while (resultSet.next()) {
                writer.print(DBSource+",");
                for (int i = 1; i <= columnCount; i++) {
                    String value = resultSet.getString(i);
                    writer.print(value != null ? value : "");
                    if (i < columnCount) writer.print(",");
                }
                writer.println();
            }

            System.out.println("✅ Data saved to CSV: " + csvFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    Statement stmt = resultSet.getStatement();
                    resultSet.close();
                    if (stmt != null) stmt.close();
                }
            } catch (SQLException ignore) {}
        }
    }



    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✅ Database connection closed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}