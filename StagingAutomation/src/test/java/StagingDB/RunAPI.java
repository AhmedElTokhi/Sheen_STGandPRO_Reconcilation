//package StagingDB;
//
///**
// * ✅ RunAPI:
// * - Runs DatabaseTest → generates DB Excel
// * - Runs retrivePNRFromAPI → reads DB Excel, calls API, generates API results Excel
// */
//public class RunAPI {
//    public static void main(String[] args) {
//        // Step 1: Run DatabaseTest → generates DB Excel
//        DBStg_TC dbTest = new DBStg_TC();
//        String dbExcelPath = dbTest.exportDBtoExcel();
//
//        // Step 2: Run retrivePNRFromAPI → reads DB Excel, calls API, saves API results Excel
//        retrivePNRFromAPI apiProcessor = new retrivePNRFromAPI();
//        String apiResultsPath = apiProcessor.processRecordsFromExcel(dbExcelPath);
//
//        System.out.println("🎉 Workflow completed!");
//        System.out.println("DB Excel: " + dbExcelPath);
//        System.out.println("API Results Excel: " + apiResultsPath);
//    }
//}
