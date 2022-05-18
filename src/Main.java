import javax.swing.*;
import java.awt.*;

public class Main
{
    public int width = 1600, height = 900;
    
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(Main::new);
    }
    
    public Main()
    {
        createAndShowGUI();
    }
    
    private void createAndShowGUI()
    {
        JFrame window = new JFrame("3D Game");
        CanvasPanel canvas = new CanvasPanel(window, width, height);
        canvas.setFocusable(true);
        canvas.requestFocusInWindow();
        window.setLayout(new FlowLayout());
        window.getContentPane().add(canvas);
        window.pack();
        window.setVisible(true);
        window.setResizable(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
