import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.*;

// A client sends messages to the server, the server spawns a thread to communicate with the client.
// Each communication with a client is added to an array list so any message sent gets sent to every other client
// by looping through it.

public class Client extends JPanel
{
    static final int PORT = 14311;
    static final String HOST = "localhost";
    
    private JFrame frame;
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private boolean[] keysPressed;
    private Point mousePosition;
    private Robot robot;
    private ArrayList<Triangle> trianglesToDraw;
    private static InputMap inputMap;
    private static ActionMap actionMap;
    private int width, height, screenWidth, screenHeight;
    BufferedImage crosshair;
    
    public Client(Socket socket, String username, int width, int height)
    {
        try
        {
            this.socket = socket;
            this.username = username;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch (IOException e)
        {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        
        this.keysPressed = new boolean[6];
        this.mousePosition = new Point();
        try
        {
            this.robot = new Robot();
        }
        catch (AWTException e)
        {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        
        this.width = width;
        this.height = height;
        this.screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        this.screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.trianglesToDraw = new ArrayList<>();
        
        this.frame = new JFrame("3D Game | " + username);
        this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.frame.setSize(new Dimension(width, height));
        this.frame.setVisible(true);
        this.frame.add(this);
        this.frame.toFront();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        frame.getContentPane().setCursor(blankCursor);
        
        try
        {
            crosshair = ImageIO.read(new File("src/res/crosshair.png"));
        }
        catch (IOException e)
        {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
        
        inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "W_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "A_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "S_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "D_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "Space_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, false), "Ctrl_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "W_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "A_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "S_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "D_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "Space_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), "Ctrl_Released");
        
        actionMap = getActionMap();
        actionMap.put("W_Pressed", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[0] = true;
            }
        });
        actionMap.put("A_Pressed", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[1] = true;
            }
        });
        actionMap.put("S_Pressed", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[2] = true;
            }
        });
        actionMap.put("D_Pressed", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[3] = true;
            }
        });
        actionMap.put("Space_Pressed", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[4] = true;
            }
        });
        actionMap.put("Ctrl_Pressed", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[5] = true;
            }
        });
        actionMap.put("W_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[0] = false;
            }
        });
        actionMap.put("A_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[1] = false;
            }
        });
        actionMap.put("S_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[2] = false;
            }
        });
        actionMap.put("D_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[3] = false;
            }
        });
        actionMap.put("Space_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[4] = false;
            }
        });
        actionMap.put("Ctrl_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[5] = false;
            }
        });
        
        
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        if (trianglesToDraw.size() == 0)
        {
            return;
        }
        
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        
        try
        {
            for (Iterator<Triangle> iterator = trianglesToDraw.iterator(); iterator.hasNext(); )
            {
                Triangle t = iterator.next();
                g.setColor(t.color);
        
                int[] xValues = {(int) t.points[0].x, (int) t.points[1].x, (int) t.points[2].x};
                int[] yValues = {(int) t.points[0].y, (int) t.points[1].y, (int) t.points[2].y};
        
                g.fillPolygon(new Polygon(xValues, yValues, 3));
                g.setColor(Color.white);
                // g.drawPolygon(new Polygon(xValues, yValues, 3));
            }
        }
        catch (ConcurrentModificationException e)
        {
            e.printStackTrace();
        }
        
        // g.drawImage(crosshair, getWidth() / 2 - crosshair.getWidth() / 2, getHeight() / 2 - crosshair.getHeight() / 2, null);
    }
    
    public void sendMessage(String messageToSend)
    {
        try
        {
            bufferedWriter.write(messageToSend);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (IOException e)
        {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    
    public void listenForMessages()
    {
        new Thread(() ->
        {
            String msgFromServer;
            
            while (!socket.isClosed())
            {
                try
                {
                    msgFromServer = bufferedReader.readLine();
                    handleMessageFromServer(msgFromServer);
                }
                catch (IOException e)
                {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }
    
    public void handleMessageFromServer(String message) throws IOException
    {
//        System.out.println(message);
        
        if (message.contains("Update Mouse"))
        {
            mousePosition = MouseInfo.getPointerInfo().getLocation();
            
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            
            robot.mouseMove(centerX, centerY);
            
            float dx = mousePosition.x - centerX;
            float dy = mousePosition.y - centerY;
            
            sendMessage("mouse_update: " + dx + " " + dy);
        }
        else if (message.equals("clear triangles"))
        {
            trianglesToDraw.clear();
        }
        else if (message.startsWith("triangle"))
        {
            String[] triangleInfo = message.split(" ");
            Triangle t = new Triangle(
                new Vector(Float.parseFloat(triangleInfo[1]), Float.parseFloat(triangleInfo[2]), Float.parseFloat(triangleInfo[3])),
                new Vector(Float.parseFloat(triangleInfo[4]), Float.parseFloat(triangleInfo[5]), Float.parseFloat(triangleInfo[6])),
                new Vector(Float.parseFloat(triangleInfo[7]), Float.parseFloat(triangleInfo[8]), Float.parseFloat(triangleInfo[9])),
                new Color(Integer.parseInt(triangleInfo[10]), Integer.parseInt(triangleInfo[11]), Integer.parseInt(triangleInfo[12]))
            );
            
            trianglesToDraw.add(t);
        }
    }
    
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter)
    {
        try
        {
            if (bufferedReader != null)
            {
                bufferedReader.close();
            }
            if (bufferedWriter != null)
            {
                bufferedWriter.close();
            }
            if (socket != null)
            {
                socket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        
        // Get a username for the user and a socket connection.
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scan.nextLine();
        
        Socket socket = new Socket(HOST, PORT);
        
        System.out.println("Enter desired screen size (x, y) in pixels:");
        String size = scan.nextLine();
        String[] sizeInfo = size.replaceAll(",", "").split(" ");
        
        int width = Integer.parseInt(sizeInfo[0]);
        int height = Integer.parseInt(sizeInfo[1]);
        
        Client client = new Client(socket, username, width, height);
        
        // Infinite loop to read messages.
        client.listenForMessages();
        
        // Initially send Username
        client.sendMessage(username);
        client.sendMessage("size: " + width + " " + height);
        
        // Main client loop
        while (!socket.isClosed())
        {
            String keyInfo = Arrays.toString(client.keysPressed).replaceAll("[\\[\\]]", "");
            client.sendMessage("keys: " + keyInfo);
            
            client.repaint();
            client.grabFocus();
            client.frame.toFront();
        }
    }
}
