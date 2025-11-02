package Generic_Classes;



public class Get_DB_Config_STG {
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
        return "Get_DB_Config_STG{" + "hostname='" + hostname + '\'' + ", username='" + username + '\'' + '}';
    }

}
