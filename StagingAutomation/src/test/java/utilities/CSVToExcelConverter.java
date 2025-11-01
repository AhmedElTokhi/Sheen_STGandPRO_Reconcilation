package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class CSVToExcelConverter {

    public static Workbook convertCSVToWorkbook(String csvPath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            int rowIndex = 0;
            while ((line = br.readLine()) != null) {
                Row row = sheet.createRow(rowIndex++);
                String[] values = line.split(",");
                for (int i = 0; i < values.length; i++) {
                    row.createCell(i).setCellValue(values[i].trim());
                }
            }
        }
        return workbook;
    }
}
