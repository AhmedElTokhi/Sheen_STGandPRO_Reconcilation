
package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;

public class retrivePnrUtilis {
    private static JsonNode config;

    static {
        try {
            ObjectMapper mapper = new ObjectMapper();
            config = mapper.readTree(new File("retrivePNR.json"));  // تأكد إن المسار صحيح
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("❌ Failed to load retrivePNR.json");
        }
    }

    public static String getBaseUrl() {
        return config.get("baseUrl").asText();
    }

    public static String getUsername(String country) {
        return config.get("credentials").get(country).get("username").asText();
    }

    public static String getPassword(String country) {
        return config.get("credentials").get(country).get("password").asText();
    }
}
