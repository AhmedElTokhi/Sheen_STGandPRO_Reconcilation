package StagingDB;

public class DbConfig {
    private String hostname;
    private String username;
    private String password;

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return "DbConfig{" +
                "hostname='" + hostname + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
