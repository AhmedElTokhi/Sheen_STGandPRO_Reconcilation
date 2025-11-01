
package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ZohoUtils {

    public static ConfigModel getConfig(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(filePath), ConfigModel.class);
        } catch (IOException e) {
            throw new RuntimeException("❌ Failed to read config from JSON: " + filePath, e);
        }
    }

    public static class ConfigModel {
        private String exportUrl;
        private String username;
        private String password;

        public String getExportUrl() { return exportUrl; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }

        public void setExportUrl(String exportUrl) { this.exportUrl = exportUrl; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
    }
}
