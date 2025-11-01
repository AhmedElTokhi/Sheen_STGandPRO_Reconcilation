package Reconciliation_TC;

import StagingDB.ExcelWriter;
import io.restassured.RestAssured;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utilities.retrivePnrUtilis;   // ✅ Utility لقراءة الـ JSON

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ✅ This class:
 * - Reads PNR & traveler data from Excel file (created by DatabaseTest)
 * - Builds SOAP requests for Travelport API
 * - Sends requests using RestAssured
 * - Extracts ProviderLocatorCode, CreatedDate, TicketStatus from the response
 * - Saves results into a new Excel file
 */
public class retriveFilteredPNRFromAPI_TC {

    // ✅ Get Base URL from retrivePNR.json
    private static final String BASE_URL = retrivePnrUtilis.getBaseUrl();

    private final String locate = "Envelope.Body.UniversalRecordSearchRsp.UniversalRecordSearchResult.";

    // ✅ Set to make sure ProviderLocatorCode values are unique (no duplicates)
    private final Set<String> globalSeenProviderCodes = new HashSet<>();

    private boolean isUniqueProviderCode(String providerCode) {
        if (providerCode == null || providerCode.isEmpty()) {
            return false;
        }
        return globalSeenProviderCodes.add(providerCode);
    }

    /**
     * ✅ Main method to process Excel file and call API.
     * Steps:
     * 1. Read each row from Excel
     * 2. Build SOAP request
     * 3. Send request with each credential (UAE, EGY, KSA)
     * 4. Parse response
     * 5. Collect results and save them into a new Excel file
     *
     * @param dbExcelPath Path to DB Excel file (output of DatabaseTest)
     * @return Path to output API results Excel file
     */
    public String processRecordsFromExcel(String dbExcelPath) {
        String outFile = "";
        try (FileInputStream fis = new FileInputStream(dbExcelPath);
             Workbook dbWorkbook = new XSSFWorkbook(fis)) {

            Sheet sheet = dbWorkbook.getSheetAt(0); // First sheet
            int rowCount = sheet.getPhysicalNumberOfRows();

            List<Map<String, Object>> results = new ArrayList<>();

            // 🔁 Loop through rows (skip header row)
            for (int r = 1; r < rowCount; r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                // Read values by column name
                String creationDate = getCellValue(row, "CREATION_TIME");
                String depDate = getCellValue(row, "DEPT_DATE");
                String arrDate = getCellValue(row, "ARR_DATE");
                String firstName = getCellValue(row, "FIRST_NAME");
                String lastName = getCellValue(row, "LAST_NAME");
                String origin = getCellValue(row, "ORIGIN");
                String pnr = getCellValue(row, "SP_PNR");

                // Build SOAP request
                String soapRequest = buildSoapRequest(creationDate, depDate, arrDate, firstName, lastName, origin);

                // ✅ Loop over all countries from retrivePNR
                for (String country : new String[]{"UAE", "EGY", "KSA"}) {
                    String username = retrivePnrUtilis.getUsername(country);
                    String password = retrivePnrUtilis.getPassword(country);

                    Response response = RestAssured.given()
                            .auth().preemptive().basic(username, password)
                            .header("Content-Type", "text/xml;charset=UTF-8")
                            .body(soapRequest)
                            .post(BASE_URL);

                    if (response.getStatusCode() != 200) {
                        System.out.println("❌ Request failed for " + country + " with status: " + response.getStatusCode());
                        continue;
                    }

                    XmlPath xmlPath = new XmlPath(response.asString());
                    List<Map<String, Object>> rowResults = parseResponse(xmlPath, pnr, country);
                    results.addAll(rowResults);
                }
            }

            // Save results into new Excel file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            outFile = System.getProperty("user.home") + "/Downloads/API_Results_" + timestamp + ".xlsx";
            ExcelWriter.writeToExcel(outFile, results);

            System.out.println("✅ API results saved to Excel: " + outFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return outFile;
    }

    /**
     * ✅ Build SOAP request with given row data.
     */
    private String buildSoapRequest(String creationDate, String depDate, String arrDate,
                                    String firstName, String lastName, String origin) {
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                "xmlns:com=\"http://www.travelport.com/schema/common_v50_0\" " +
                "xmlns:univ=\"http://www.travelport.com/schema/universal_v50_0\">" +
                "<soap:Body>" +
                "<univ:UniversalRecordSearchReq ActionDate=\"" + creationDate + "\" ProviderCode=\"1G\" PseudoCityCode=\"\">" +
                "<com:BillingPointOfSaleInfo OriginApplication=\"UAPI\"/>" +
                "<univ:UniversalRecordSearchModifiers ExcludeHotel=\"true\" ExcludeVehicle=\"true\" IncludeAgentInfo=\"true\" IncludeAllNames=\"true\"/>" +
                "<univ:TravelerCriteria>" +
                "<univ:NameCriteria FirstName=\"" + firstName + "\" LastName=\"" + lastName + "\"/>" +
                "</univ:TravelerCriteria>" +
                "<univ:AirReservationCriteria Origin=\"" + origin + "\">" +
                "<univ:DepartureDate><univ:SpecificDate>" + depDate + "</univ:SpecificDate></univ:DepartureDate>" +
                "<univ:ArrivalDate><univ:SpecificDate>" + arrDate + "</univ:SpecificDate></univ:ArrivalDate>" +
                "</univ:AirReservationCriteria>" +
                "</univ:UniversalRecordSearchReq>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }

    /**
     * ✅ Parse API XML response.
     */
    private List<Map<String, Object>> parseResponse(XmlPath xmlPath, String pnr, String credential) {
        List<Map<String, Object>> records = new ArrayList<>();

        List<String> createdDates = xmlPath.getList(locate + "@CreatedDate");
        List<String> ticketStatuses = xmlPath.getList(locate + "@TicketStatus");
        List<List<String>> providerCodesList = xmlPath.getList(locate + ".ProductInfo.@ProviderLocatorCode");

        if (createdDates == null) return records;

        for (int i = 0; i < createdDates.size(); i++) {
            String createdDate = createdDates.get(i);
            String ticketStatus = ticketStatuses.get(i);
            List<String> providerCodes = providerCodesList.get(i);

            for (String providerCode : providerCodes) {
                if (!isUniqueProviderCode(providerCode)) continue;

                Map<String, Object> row = new LinkedHashMap<>();
                row.put("PNR", pnr != null ? pnr : "N/A");
                row.put("Credential", credential);
                row.put("ProviderLocatorCode", providerCode);
                row.put("CreatedDate", createdDate);
                row.put("TicketStatus", ticketStatus != null ? ticketStatus : "Unknown");
                records.add(row);
            }
        }
        return records;
    }

    /**
     * ✅ Utility method to get a cell value by column name.
     */
    private String getCellValue(Row row, String columnName) {
        Row headerRow = row.getSheet().getRow(0);
        if (headerRow == null) return "";
        int colCount = headerRow.getPhysicalNumberOfCells();

        for (int i = 0; i < colCount; i++) {
            if (headerRow.getCell(i).getStringCellValue().equalsIgnoreCase(columnName)) {
                Cell cell = row.getCell(i);
                return cell != null ? cell.toString() : "";
            }
        }
        return "";
    }
}
