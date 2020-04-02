import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ViewQuery extends GUI{
    private static List<String> query = new ArrayList<String>();

    public static void main(String[] args){
        Connect conn = new Connect();
        ViewQuery gui = new ViewQuery("ViewQuery", conn);
        return;
    }
    public ViewQuery(String title, Connect connect) {
        // build sql sentence
        buildSQL();
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
//        frame.addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosed(WindowEvent e) {
//
//            }
//        });
    }

    private void buildSQL(){
        // 查询账单费用总计在1000元以上的患者信息及总费用
        String sql1 = "Select * From patient_bill;";
        // 查询医生的平均年龄大于30的科室名
        String sql2 = "Select * From patient_department;";
        // 查询医生平均年龄最大的科室名
        String sql3 = "Select * From nurse_patient;";
        query.add(sql1);
        query.add(sql2);
        query.add(sql3);
    }

    private static List<String> exeQuery(Connect connect, int index){
        List<String> ret = new ArrayList<String>();
        try {
            Statement stmt = connect.getConn().createStatement();
            ResultSet rs = stmt.executeQuery(query.get(index));
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnnum = rsmd.getColumnCount();
            while(rs.next()){
                String tmp = "";
                for(int i = 1; i <= columnnum; i++){
                    tmp += rs.getString(i)+"\t";
                }
                tmp = tmp.substring(0, tmp.length()-1) + "\n";
                ret.add(tmp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
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
        inputTextArea.setText("点击Query即可进行内置默认查询\n或者在此输入框中输入一句查询语句。");
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


        JButton query1 = new JButton("Query1");
        query1.setFont(buttonFont);
        query1.setBounds(xBlankLen,
                outputLabel.getY() + outputLabel.getHeight() + yBlankLen, buttonWidth, buttonHeight);
        query1.addActionListener(actionEvent -> {
            inputTextArea
                    .setText(query.get(0));
            inputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            outputTextArea
                    .setText(String.join("", exeQuery(connect, 0)));
            outputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        });
        panel.add(query1);

        JButton query2 = new JButton("Query2");
        query2.setFont(buttonFont);
        query2
                .setBounds(xBlankLen + query1.getX() + query1.getWidth(),
                        outputLabel.getY() + outputLabel.getHeight() + yBlankLen, buttonWidth, buttonHeight);
        query2.addActionListener(actionEvent -> {
            inputTextArea
                    .setText(query.get(1));
            inputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            outputTextArea
                    .setText(String.join("",exeQuery(connect, 1)));
            outputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        });
        panel.add(query2);

        JButton query3 = new JButton("Query3");
        query3.setFont(buttonFont);
        query3
                .setBounds(xBlankLen + query2.getX() + query2.getWidth(),
                        outputLabel.getY() + outputLabel.getHeight() + yBlankLen, buttonWidth, buttonHeight);
        query3.addActionListener(actionEvent -> {
            inputTextArea
                    .setText(query.get(2));
            inputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
            outputTextArea
                    .setText(String.join("",exeQuery(connect, 2)));
            inputTextArea.setFont(new Font("Times New Roman", Font.PLAIN, 30));
        });
        panel.add(query3);

        JButton query4 = new JButton("自定义查询");
        query4.setFont(buttonFont);
        query4
                .setBounds(xBlankLen + query3.getX() + query3.getWidth(),
                        outputLabel.getY() + outputLabel.getHeight() + yBlankLen, buttonWidth, buttonHeight);
        query4.addActionListener(actionEvent -> {
            String input = inputTextArea.getText();
            outputTextArea
                    .setText(String.join("",exeQuery(connect, 2)));
        });
        panel.add(query4);
    }
}
