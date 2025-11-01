package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonUtils {
    // Method to read query from JSON file
    public static String getQueryFromJson(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            QueryModel queryModel = mapper.readValue(new File(filePath), QueryModel.class);
            return queryModel.getQuery();
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to read query from JSON file: " + filePath, e);
        }
    }

    // Inner class to map the JSON structure
    public static class QueryModel {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}