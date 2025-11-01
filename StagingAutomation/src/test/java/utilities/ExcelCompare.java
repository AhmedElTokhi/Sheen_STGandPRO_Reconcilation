package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class ExcelCompare {

    /**
     * ✅ النسخة القديمة (InputStreams + SheetNames)
     */
    public static void markExistence(
            InputStream dbExcel, String dbSheetName,
            InputStream missingExcel, String missingSheetName,
            String outputFile) {

        try (Workbook dbWb = new XSSFWorkbook(dbExcel);
             Workbook missingWb = new XSSFWorkbook(missingExcel)) {

            Sheet dbSheet = dbWb.getSheet(dbSheetName);
            Sheet missingSheet = missingWb.getSheet(missingSheetName);

            markExistence(dbSheet, missingSheet, outputFile); // نستخدم النسخة الجديدة

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Error while comparing Excel files: " + e.getMessage());
        }
    }

    /**
     * ✅ النسخة الجديدة (Overload يقبل Sheets مباشرة)
     */
    public static void markExistence(Sheet dbSheet, Sheet missingSheet, String outputFile) {
        try {
            Workbook dbWb = dbSheet.getWorkbook();

            // جهز Styles للتلوين
            CellStyle greenStyle = dbWb.createCellStyle();
            Font greenFont = dbWb.createFont();
            greenFont.setColor(IndexedColors.GREEN.getIndex());
            greenStyle.setFont(greenFont);

            CellStyle redStyle = dbWb.createCellStyle();
            Font redFont = dbWb.createFont();
            redFont.setColor(IndexedColors.RED.getIndex());
            redStyle.setFont(redFont);

            // حط الـ Missing PNRs في Set
            Set<String> missingPnrs = new HashSet<>();
            for (int i = 1; i <= missingSheet.getLastRowNum(); i++) {
                Row row = missingSheet.getRow(i);
                if (row != null) {
                    Cell cell = row.getCell(0); // العمود الأول (PNR)
                    if (cell != null) {
                        missingPnrs.add(cell.getStringCellValue().trim());
                    }
                }
            }

            // ضيف عمود جديد في DB Sheet اسمه ExistsInDB
            Row headerRow = dbSheet.getRow(0);
            int existsColIndex = headerRow.getLastCellNum();
            headerRow.createCell(existsColIndex).setCellValue("ExistsInDB");

            // لف على كل الـ Missing PNRs وشيك إذا كانت في DB
            for (String pnr : missingPnrs) {
                boolean found = false;

                for (int i = 1; i <= dbSheet.getLastRowNum(); i++) {
                    Row row = dbSheet.getRow(i);
                    if (row != null) {
                        Cell cell = row.getCell(0); // نفترض إن PNR موجود في العمود الأول
                        if (cell != null && pnr.equalsIgnoreCase(cell.getStringCellValue().trim())) {
                            // لو لقيته → Exist in DB
                            Cell resultCell = row.createCell(existsColIndex);
                            resultCell.setCellValue("Exist in DB");
                            resultCell.setCellStyle(greenStyle);
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    // لو مش موجود في DB → ضيفه في سطر جديد مع Not exist in DB
                    Row newRow = dbSheet.createRow(dbSheet.getLastRowNum() + 1);
                    newRow.createCell(0).setCellValue(pnr);
                    Cell resultCell = newRow.createCell(existsColIndex);
                    resultCell.setCellValue("Not exist in DB");
                    resultCell.setCellStyle(redStyle);
                }
            }

            // اكتب الملف الناتج
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                dbWb.write(fos);
            }

            dbWb.close();

            System.out.println("✅ Excel comparison done! Output: " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Error while comparing Excel files: " + e.getMessage());
        }
    }
}
