package StagingDB;

import utilities.ExcelCompare;

import java.io.InputStream;

public class Runner {
    public static void main(String[] args) {
        // اقرأ ملف DB Excel من resources
        InputStream dbExcel = Runner.class.getClassLoader()
                .getResourceAsStream("testDataFiles/db8-09.xlsx");

        // اقرأ ملف MissingPNR Excel من resources
        InputStream missingPnrExcel = Runner.class.getClassLoader()
                .getResourceAsStream("testDataFiles/missingpnr8-09.xlsx");

        if (dbExcel == null || missingPnrExcel == null) {
            throw new RuntimeException("❌ One of the Excel files not found in resources!");
        }

        // حدد مكان الfile
        String outputFile = System.getProperty("user.home") + "/Downloads/All_DB_Results_withExistence.xlsx";

        // نفذ المقارنة
        ExcelCompare.markExistence(dbExcel, "Sheet1", missingPnrExcel, "Sheet1", outputFile);
    }





}
