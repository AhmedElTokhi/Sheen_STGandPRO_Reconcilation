package StagingDB;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.*;

public class ExcelWriter {
    public static void writeToExcel(String filePath, List<Map<String, Object>> combinedResults) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("PnrStaging");

            if (combinedResults.isEmpty()) {
                sheet.createRow(0).createCell(0).setCellValue("No Data");
            } else {
                LinkedHashSet<String> headerSet = new LinkedHashSet<>();
                for (Map<String, Object> row : combinedResults) {
                    headerSet.addAll(row.keySet());
                }
                List<String> headers = new ArrayList<>(headerSet);

                Row headerRow = sheet.createRow(0);
                for (int c = 0; c < headers.size(); c++) {
                    headerRow.createCell(c).setCellValue(headers.get(c));
                }

                int r = 1;
                for (Map<String, Object> rowMap : combinedResults) {
                    Row row = sheet.createRow(r++);
                    for (int c = 0; c < headers.size(); c++) {
                        String col = headers.get(c);
                        Cell cell = row.createCell(c);
                        Object val = rowMap.get(col);
                        if (val == null) {
                            cell.setCellValue("");
                        } else if (val instanceof Number) {
                            cell.setCellValue(((Number) val).doubleValue());
                        } else if (val instanceof Boolean) {
                            cell.setCellValue((Boolean) val);
                        } else {
                            cell.setCellValue(val.toString());
                        }
                    }
                }

                for (int c = 0; c < Math.min(headers.size(), 20); c++) {
                    sheet.autoSizeColumn(c);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }
}
