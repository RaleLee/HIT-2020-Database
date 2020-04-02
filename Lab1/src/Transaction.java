import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Transaction extends GUI {
    private static List<String> query = new ArrayList<String>();

    public static void main(String[] args){
        Connect conn = new Connect();
        Transaction gui = new Transaction("Transaction", conn);
        return;
    }
    public Transaction(String title, Connect connect) {
        // 创建 JFrame 实例
        JFrame frame = new JFrame(title);
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setBounds(40, 40, 1024, 768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 添加 JPanel
        JPanel panel = new JPanel();
        frame.add(panel);
        // 放置组件
        placeComponents(panel, connect);
        // 设置界面可见
        frame.setVisible(true);
    }

    private static List<String> exeTransaction(Connect connect){
        List<String> ret = new ArrayList<String>();
        try {
            connect.getConn().setAutoCommit(false);
            String qur = "SELECT * FROM bill where biID=10000003";
            Statement stmt = connect.getConn().createStatement();
            ResultSet rs1 = stmt.executeQuery(qur);
            ResultSetMetaData rsmd = rs1.getMetaData();
            int columnnum = rsmd.getColumnCount();
            while(rs1.next()){
                String tmp = "";
                for(int i = 1; i <= columnnum; i++){
                    tmp += rs1.getString(i)+"\t";
                }
                tmp = tmp.substring(0, tmp.length()-1) + "\n";
                ret.add(tmp);
            }
            // 模拟患者交费用，账单金额清零
            String sql = "Update bill set bill.price=0 where biID=10000003";
            PreparedStatement pstmt = connect.getConn().prepareStatement(sql);

            int rs = pstmt.executeUpdate();

            ResultSet rs2 = stmt.executeQuery(qur);
            ResultSetMetaData rsmd2 = rs2.getMetaData();
            int columnnum1 = rsmd2.getColumnCount();
            while(rs2.next()){
                String tmp = "";
                for(int i = 1; i <= columnnum1; i++){
                    tmp += rs2.getString(i)+"\t";
                }
                tmp = tmp.substring(0, tmp.length()-1) + "\n";
                ret.add(tmp);
            }
            throw new Exception("Something wrong!");
        } catch (Exception e) {
            try{
                connect.getConn().rollback();
                String qur = "SELECT * FROM bill where biID=10000003";
                Statement stmt = connect.getConn().createStatement();
                ResultSet rs1 = stmt.executeQuery(qur);
                ResultSetMetaData rsmd = rs1.getMetaData();
                int columnnum = rsmd.getColumnCount();
                while(rs1.next()){
                    String tmp = "";
                    for(int i = 1; i <= columnnum; i++){
                        tmp += rs1.getString(i)+"\t";
                    }
                    tmp = tmp.substring(0, tmp.length()-1) + "\n";
                    ret.add(tmp);
                }
            }
            catch(SQLException e1){
                e1.printStackTrace();
            }
        }
        return ret;
    }

    protected void placeComponents(JPanel panel, Connect connect){
        /* 布局部分我们这边不多做介绍
         * 这边设置布局为 null
         */
        panel.setLayout(null);

        /*

         */
        JLabel inputLabel = generateLabel("SQL语句", xBlankLen, 0);
        panel.add(inputLabel);

        JTextArea inputTextArea = new JTextArea();
        inputTextArea.setText("点击Transaction开始事务模拟");
        inputTextArea.setFont(new Font("微软雅黑", Font.PLAIN, 25));
        inputTextArea.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                inputTextArea.setText("");
                inputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            }
        });
        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane
                .setBounds(inputLabel.getX() + inputLabel.getWidth(), inputLabel.getY(), textAreaWidth,
                        textAreaHeight);
        panel.add(inputScrollPane);

        /*

         */
        JLabel outputLabel = generateLabel("输出", xBlankLen, inputLabel.getY() + inputLabel.getHeight());
        panel.add(outputLabel);
        JTextArea outputTextArea = new JTextArea();
        JScrollPane outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setBounds(inputScrollPane.getX(),
                inputScrollPane.getY() + inputScrollPane.getHeight() + yBlankLen,
                textAreaWidth, textAreaHeight);
        panel.add(outputScrollPane);


        JButton query1 = new JButton("Transaction");
        query1.setFont(buttonFont);
        query1.setBounds(xBlankLen,
                outputLabel.getY() + outputLabel.getHeight() + yBlankLen, buttonWidth, buttonHeight);
        query1.addActionListener(actionEvent -> {
            inputTextArea
                    .setText("Update bill set bill.price=0 where biID=10000003);");
            inputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            outputTextArea
                    .setText(String.join("", exeTransaction(connect)));
            outputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        });
        panel.add(query1);
    }
}
