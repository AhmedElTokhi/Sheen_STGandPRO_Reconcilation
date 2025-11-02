//package utilities;
//
//import com.shaft.driver.SHAFT;
//
//public class Connect_To_Production_DB {
//    public static SHAFT.GUI.WebDriver initializeDriver() {
//        return new SHAFT.GUI.WebDriver();
//    }
//
//    public static Execute_And_Save_To_CSV_Production_DB initializeDatabase() {
//        String url = "jdbc:mysql://192.168.1.93:3306/wonderdb?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";
//        String username = "root";
//        String password = "h@Kp#rB$32";
//        Execute_And_Save_To_CSV_Production_DB databasePage = new Execute_And_Save_To_CSV_Production_DB(url, username, password);
//        databasePage.connectToDatabase();
//        return databasePage;
//    }
//}

package Generic_Classes;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.shaft.driver.SHAFT;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connect_To_Production_DB {
    static Get_DB_Config_Production cfg = new Get_DB_Config_Production();

    private static Session sshSession;
    private static Connection dbConnection;

    // SSH Config
    private static final String SSH_HOST = "52.138.223.154";
    private static final int SSH_PORT = 22;
    private static final String SSH_USER = "tester";
    private static final String SSH_PASSWORD = "r49pPA5YjFB2";

    // DB Config
    private static final String DB_HOST = "10.40.84.5";
    private static final int DB_PORT = 3306;
    private static final String DB_USER = "ndc_developer_read-only";
    private static final String DB_PASSWORD = "g3RhDYHKts";
    private static final String DB_NAME = "wonderdb";

//    // SSH Config
//    private static final String SSH_HOST = cfg.getSSH_HOST();
////    private static final int SSH_PORT = cfg.getSSH_PORT();
//    private static final String SSH_USER = cfg.getSSH_USER();
//    private static final String SSH_PASSWORD = cfg.getSSH_PASSWOR();
//
//    // DB Config
//    private static final String DB_HOST = cfg.getDB_HOST();
//    private static final int DB_PORT = cfg.getDB_PORT();
//    private static final String DB_USER = cfg.getDB_USER();
//    private static final String DB_PASSWORD = cfg.getDB_PASSWORD();
//    private static final String DB_NAME = cfg.getDB_NAME();



    // Local forwarded port
    private static final int LOCAL_PORT = 3307; // أي بورت فاضي محلي

    public static SHAFT.GUI.WebDriver initializeDriver() {
        return new SHAFT.GUI.WebDriver();
    }

    public static Execute_And_Save_To_CSV_Production_DB initializeDatabase() {
        try {
            // 1- Create SSH tunnel
            JSch jsch = new JSch();
            sshSession = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
            sshSession.setPassword(SSH_PASSWORD);

            // Disable strict host key checking
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(config);

            sshSession.connect();
            System.out.println("✅ SSH Tunnel established");

            // Forward local port to DB Host
            sshSession.setPortForwardingL(LOCAL_PORT, DB_HOST, DB_PORT);

            // 2- Connect to DB via forwarded port
            String jdbcUrl = "jdbc:mysql://localhost:" + LOCAL_PORT + "/" + DB_NAME
                    + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";

            dbConnection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            System.out.println("✅ Database connection established");

            return new Execute_And_Save_To_CSV_Production_DB(dbConnection);

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to initialize database connection", e);
        }
    }

    public static void closeConnections() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
                System.out.println("✅ DB connection closed");
            }
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
                System.out.println("✅ SSH tunnel closed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}