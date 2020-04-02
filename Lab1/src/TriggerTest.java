import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TriggerTest extends GUI{
    private static java.util.List<String> query = new ArrayList<String>();

    public static void main(String[] args){
        Connect conn = new Connect();
        TriggerTest gui = new TriggerTest("TriggerTest", conn);
        return;
    }
    public TriggerTest(String title, Connect connect) {
        // build sql sentence
        buildSQL();
        // 创建 JFrame 实例
        JFrame frame = new JFrame(title);
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

    private void buildSQL(){
        // 插入一条患者性别不是M或F的记录，将会报错
        String sql1 = "Insert into patient \n" +
                "values(500031,'ads','K',22,1046);";
        // 插入一条患者床位已经被占用的记录，将会报错
        String sql2 = "Insert into patient \n" +
                "values(500031,'ads','F',22,1045);";
        query.add(sql1);
        query.add(sql2);
    }

    private static java.util.List<String> exeQuery(Connect connect, int index){
        List<String> ret = new ArrayList<String>();
        try {
            Statement stmt = connect.getConn().createStatement();
            boolean rs = stmt.execute(query.get(index));
        } catch (SQLException e) {
            ret.add("ERROR ");
            ret.add(e.getErrorCode() + " ");
            ret.add("(" + e.getSQLState() + "):");
            ret.add(e.getMessage());
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
        inputTextArea.setText("点击Test即可进行内置默认Test\n");
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


        JButton query1 = new JButton("TriggerTest1");
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

        JButton query2 = new JButton("TriggerTest2");
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
    }
}
