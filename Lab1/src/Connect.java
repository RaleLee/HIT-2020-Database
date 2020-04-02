import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

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

            //Query
//            System.out.println("Instantiate statement object.");
//            Statement stmt = conn.createStatement();
//            String sql = "SELECT * FROM book";
//            ResultSet rs = stmt.executeQuery(sql);
//
//            while(rs.next()){
//                String bookname = rs.getString("name");
//                String author = rs.getString("author");
//
//                System.out.println("Name: "+ bookname);
//                System.out.println("Author: "+author);
//            }
//            rs.close();
//            stmt.close();
//            conn.close();


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
