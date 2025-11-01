package Reconciliation_TC;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.path.xml.XmlPath;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileWriter;
import java.util.*;

import static io.restassured.RestAssured.given;

public class GetGalileoPNR_TC {

    // ✅ Global set to track provider codes across ALL credentials
    private final Set<String> globalSeenProviderCodes = new HashSet<>();

    /**
     * ✅ Helper function to check uniqueness of ProviderLocatorCode
     */
    private boolean isUniqueProviderCode(String providerCode) {
        if (providerCode == null || providerCode.isEmpty()) {
            return false;
        }
        return globalSeenProviderCodes.add(providerCode);
    }
    String locate = "Envelope.Body.UniversalRecordSearchRsp.UniversalRecordSearchResult[' + idx + ']";

    @Test
    public void fetchAndSaveRecordsByCredential() throws IOException {
        // ✅ Load config from resources
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ReconciliationConfig.json");
        if (inputStream == null) {
            throw new IOException("❌ Could not find ReconciliationConfig.json in resources folder!");
        }
        JsonNode config = mapper.readTree(inputStream);

        RestAssured.baseURI = config.get("baseURI").asText();
        String actionDate = config.get("actionDate").asText();
        int maxResults = config.get("maxResults").asInt();

        // ✅ Load credentials array from JSON
        List<JsonNode> credentials = config.get("credentials").findValues(null);

        String csvFilePath = System.getProperty("user.dir") + "/provider_codes_by_credential.csv";
        try (FileWriter writer = new FileWriter(csvFilePath)) {
            // ✅ Write CSV header
            writer.write("Credential,ProviderLocatorCode,CreatedDate,TicketStatus\n");

            // 🔁 Loop credentials
            for (JsonNode cred : config.get("credentials")) {
                String label = cred.get("label").asText();
                String username = cred.get("username").asText();
                String password = cred.get("password").asText();

                System.out.println("🔐 Fetching records using: " + label);

                for (int i = 0; i <= 100000; i += 98) {
                    int startFrom = i;

                    String requestBody =
                            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
                                    "xmlns:univ=\"http://www.travelport.com/schema/universal_v52_0\" " +
                                    "xmlns:com=\"http://www.travelport.com/schema/common_v52_0\">" +
                                    "<soapenv:Header/>" +
                                    "<soapenv:Body>" +
                                    "<univ:UniversalRecordSearchReq ActionDate=\"" + actionDate + "\">" +
                                    "<univ:UniversalRecordSearchModifiers MaxResults=\"" + maxResults + "\" StartFromResult=\"" + startFrom + "\"/>" +
                                    "</univ:UniversalRecordSearchReq>" +
                                    "</soapenv:Body>" +
                                    "</soapenv:Envelope>";

                    Response response = given()
                            .header("Content-Type", "text/xml")
                            .header("Accept", "text/xml")
                            .auth().preemptive().basic(username, password)
                            .body(requestBody)
                            .post();

                    XmlPath xmlPath = new XmlPath(response.asString());

                    // ✅ Extract each UniversalRecordSearchResult
                    List<Object> results = xmlPath.getList("Envelope.Body.UniversalRecordSearchRsp.UniversalRecordSearchResult");

                    if (results == null || results.isEmpty()) {
                        break;
                    }

                    for (int idx = 0; idx < results.size(); idx++) {
                        String createdDate = xmlPath.getString(locate+".@CreatedDate");

                        String ticketStatus = xmlPath.getString(locate+".@TicketStatus");

                        // ✅ Extract all provider codes as list
                        List<String> providerCodes = xmlPath.getList(
                                locate+".ProductInfo.@ProviderLocatorCode"
                        );

                        if (providerCodes == null || providerCodes.isEmpty()) {
                            providerCodes = Collections.singletonList("");
                        }

                        // ✅ Loop through provider codes
                        for (String providerCode : providerCodes) {
                            if (providerCode == null) providerCode = "";

                            if (isUniqueProviderCode(providerCode)) {
                                String row = label + "," + providerCode + "," + createdDate + "," + ticketStatus;
                                writer.write(row + "\n");
                            }
                        }
                    }

                    // ✅ Stop loop if no more results
                    String moreResults = xmlPath.getString("Envelope.Body.UniversalRecordSearchRsp.@MoreResults");
                    System.out.println("🔍 StartFrom=" + startFrom + " | MoreResults=" + moreResults);
                    if (!"true".equalsIgnoreCase(moreResults)) {
                        System.out.println("🚫 No more results for user: " + label);
                        break;
                    }
                }
            }
        }

        System.out.println("✅ Unique records saved with ProviderLocatorCode, CreatedDate, TicketStatus");
        System.out.println("📄 CSV saved at: " + csvFilePath);
    }
}
