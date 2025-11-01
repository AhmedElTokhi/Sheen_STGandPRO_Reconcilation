//package StagingDB;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.InputStream;
//import java.sql.Connection;
//import java.util.*;
//
//public class Main {
//    private static final String QUERY = "SELECT * FROM wonderdb.FAILURE_MESSAGES;";
//
//    public static void main(String[] args) {
//        System.out.println("Starting DB -> Excel process...");
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            InputStream is = Main.class.getClassLoader().getResourceAsStream("dbConfig.json");
//            if (is == null) {
//                throw new RuntimeException("dbConfig.json not found in resources folder");
//            }
//            List<DbConfig> configs = mapper.readValue(is, new TypeReference<List<DbConfig>>() {});
//
//            List<Map<String, Object>> combinedResults = new ArrayList<>();
//
//            for (DbConfig cfg : configs) {
//                System.out.println("Connecting to: " + cfg.getHostname());
//                try (Connection conn = DBConnection.getConnection(cfg.getHostname(), cfg.getUsername(), cfg.getPassword())) {
//                    List<Map<String, Object>> rows = DatabaseReader.runQuery(conn, QUERY);
//                    System.out.println("  -> rows fetched: " + rows.size());
//
//                    for (Map<String, Object> r : rows) {
//                        Map<String, Object> newRow = new LinkedHashMap<>();
//                        newRow.put("SourceDB", cfg.getHostname());
//                        newRow.putAll(r);
//                        combinedResults.add(newRow);
//                    }
//                } catch (Exception e) {
//                    System.err.println("  !! Error connecting/querying " + cfg.getHostname() + " : " + e.getMessage());
//                }
//            }
//
//            String outFile = "All_DB_Results.xlsx";
//            ExcelWriter.writeToExcel(outFile, combinedResults);
//            System.out.println("Excel file created: " + outFile + " with sheet PnrStaging");
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        System.out.println("Done.");
//    }
//}
