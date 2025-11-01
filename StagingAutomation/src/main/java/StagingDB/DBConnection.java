package StagingDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection(String hostname, String username, String password) throws SQLException {
        String url = "jdbc:mysql://" + hostname + ":3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(url, username, password);
    }
}
