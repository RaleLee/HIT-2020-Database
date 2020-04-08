import java.sql.Connection;
import java.sql.DriverManager;

public class Connect {
    // MySQL8.0 - JDBC Driver
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Database username and password
    private String userName = "root";
    private String password = "123456";

    private Connection conn;

    public Connect(){
        try{
            // Register JDBC Driver
            Class.forName(JDBC_DRIVER);
            // Open url
            System.out.println("Connecting...");
            String url = "jdbc:mysql://localhost:3306/hospital?useSSL=false&serverTimezone=UTC";
            conn = DriverManager.getConnection(url, this.userName, this.password);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConn() {
        return conn;
    }

    public static void main(String[] args) {
        Connect connn = new Connect();
    }
}
