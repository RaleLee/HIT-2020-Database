import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GUI {
    public static final int xBlankLen = 80;//水平间隔长度
    public static final int yBlankLen = 20;//竖直间隔长度
    public static final int textAreaWidth = xBlankLen * 9;
    public static final int textAreaHeight = yBlankLen * 15;
    public static final int labelWidth = xBlankLen * 2;
    public static final Font labelFont = new Font("微软雅黑", Font.PLAIN, 30);
    public static final Font buttonFont = new Font("微软雅黑", Font.BOLD, 25);
    public static final int buttonWidth = xBlankLen * 3;
    public static final int buttonHeight = yBlankLen * 2;

    public GUI(){}
    public GUI(String title){
        // 创建 JFrame 实例
        JFrame frame = new JFrame(title);
//        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setBounds(40, 40, 1024, 768);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 添加 JPanel
        JPanel panel = new JPanel();
        frame.add(panel);
        // 放置组件
        placeComponents(panel);
        // 设置界面可见
        frame.setVisible(true);
    }



    public static void main(String[] args) {
        GUI gui = new GUI("test");
    }

    protected static JLabel generateLabel(String labelName, int labelX, int labelY) {
        JLabel retLabel = new JLabel(labelName);
        retLabel.setFont(labelFont);
        retLabel.setBounds(labelX, labelY, labelWidth, textAreaHeight);
        return retLabel;
    }

    protected void placeComponents(JPanel panel) {


    }
}
