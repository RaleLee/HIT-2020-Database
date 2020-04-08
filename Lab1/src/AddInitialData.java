import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddInitialData {
    private String pathpre = "data/";
    private String fileType = ".csv";
    public static final String[] tableNames = {"Department", "Doctor", "Nurse", "Sickbed", "Patient", "Surgery", "Diagnosis", "Care", "Help", "Bill"};

    public AddInitialData(){ }

    public boolean buildAddDataSql(String tableName, Connect connect) throws FileNotFoundException, SQLException {
        File file = new File(pathpre + tableName+ fileType);
        Scanner sc = new Scanner(file);
        String line = sc.nextLine();
        String[] attName = line.split(",");
        int num = attName.length;
        Pattern pattern = Pattern.compile("[0-9]{1,}");

        while(sc.hasNextLine()){
            line = sc.nextLine();
            if(line.length() == 0){
                continue;
            }
            String[] tuple = line.split(",");
            for(String s: tuple)
                System.out.println(s);
            String sql = "Insert into " + tableName + " values " + "(";
            for(int i = 0; i < num; i++){
                sql += "?,";
            }
            sql = sql.substring(0, sql.length()-1) + ");";
            PreparedStatement pstmt = connect.getConn().prepareStatement(sql);
            if(num != tuple.length){
                System.out.println(tableName);
                throw new RuntimeException("Fxxk");
            }
            for(int i = 0; i < num; i++){
                String s = tuple[i];
                Matcher matcher = pattern.matcher((CharSequence)s);
                boolean result = matcher.matches();
                if(result){
                    pstmt.setInt(i+1, Integer.valueOf(s));
                }else{
                    pstmt.setString(i+1, s);
                }
            }
            pstmt.executeUpdate();
            pstmt.close();
        }
        sc.close();
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException, SQLException {
        Connect conn = new Connect();
        AddInitialData aid = new AddInitialData();
        int len = AddInitialData.tableNames.length;

        for(int i = 9; i < len; i++){
            aid.buildAddDataSql(AddInitialData.tableNames[i], conn);
        }
        conn.getConn().close();
        return;
    }
}
