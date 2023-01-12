import javax.swing.UIManager;

public class App {
    public static void main(String[] args) throws Exception {
        new MyFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }
    }
}
