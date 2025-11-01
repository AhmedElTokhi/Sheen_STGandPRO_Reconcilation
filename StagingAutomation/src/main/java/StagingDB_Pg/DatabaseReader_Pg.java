package StagingDB_Pg;

import java.sql.*;
import java.util.*;

public class DatabaseReader_Pg {
    public static List<Map<String, Object>> runQuery(Connection conn, String query) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= colCount; i++) {
                    String colName = meta.getColumnLabel(i);
                    Object value = rs.getObject(i);
                    row.put(colName, value);
                }
                results.add(row);
            }
        }
        return results;
    }
}
