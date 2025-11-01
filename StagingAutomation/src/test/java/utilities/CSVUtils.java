package utilities;


import java.io.PrintWriter;
import java.util.*;

public class CSVUtils {

    /**
     * ✅ Utility Method
     * Saves a list of maps (rows) into a CSV file.
     *
     * @param filePath output file path
     * @param data list of rows (each row = Map<columnName, value>)
     * @throws Exception if file cannot be written
     */
    public static void saveAsCSV(String filePath, List<Map<String, Object>> data) throws Exception {
        if (data.isEmpty()) {
            System.out.println("⚠️ No data to write.");
            return;
        }

        try (PrintWriter pw = new PrintWriter(filePath)) {
            // Header
            Set<String> headers = data.get(0).keySet();
            pw.println(String.join(",", headers));

            // Rows
            for (Map<String, Object> row : data) {
                List<String> values = new ArrayList<>();
                for (String h : headers) {
                    Object val = row.get(h);
                    String safeVal = val == null ? "" : "\"" + val.toString().replace("\"", "\"\"") + "\"";
                    values.add(safeVal);
                }
                pw.println(String.join(",", values));
            }
        }
    }
}
