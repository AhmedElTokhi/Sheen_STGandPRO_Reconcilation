package utilities;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnectionPage {
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

    // Local forwarded port
    private static final int LOCAL_PORT = 3307;

    public static Connection initializeConnection() {
        try {
            // 1- Create SSH tunnel
            JSch jsch = new JSch();
            sshSession = jsch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
            sshSession.setPassword(SSH_PASSWORD);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(config);

            sshSession.connect();
            System.out.println("✅ SSH Tunnel established");

            // Forward local port
            sshSession.setPortForwardingL(LOCAL_PORT, DB_HOST, DB_PORT);

            // 2- Connect to DB
            String jdbcUrl = "jdbc:mysql://localhost:" + LOCAL_PORT + "/" + DB_NAME
                    + "?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8";

            dbConnection = DriverManager.getConnection(jdbcUrl, DB_USER, DB_PASSWORD);
            System.out.println("✅ Database connection established");

            return dbConnection;

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
