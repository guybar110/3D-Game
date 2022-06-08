import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Scanner;

// A client sends messages to the server, the server spawns a thread to communicate with the client.
// Each communication with a client is added to an array list so any message sent gets sent to every other client
// by looping through it.

public class Client extends JPanel implements MouseListener
{
    static final int PORT = 14312;
    static final String HOST = "localhost";
    
    private Socket socket;
    private ObjectOutputStream objectWriter;
    private ObjectInputStream objectReader;
    private boolean[] keysPressed;
    private float[][] projectionMatrix;
    private Robot robot;
    private ArrayList<Triangle> trianglesToDraw;
    private int width, height, screenWidth, screenHeight;
    private int fps = 30;
    private float sensitivity = 0.1f;
    private float fov = 135.0f;
    private float zNear = 0.1f;
    private float zFar = 1000.0f;
    private boolean inputEnabled = true;
    private Game newGameState;
    private Player player;
    private BufferedImage crosshair;
    private Color[] colors = {Color.blue, Color.red, Color.green, Color.yellow};
    
    public Client(Socket socket, int width, int height)
    {
        try
        {
            this.socket = socket;
            this.objectWriter = new ObjectOutputStream(socket.getOutputStream());
            this.objectReader = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e)
        {
            closeEverything();
        }
        
        this.keysPressed = new boolean[6];
        try
        {
            this.robot = new Robot();
        }
        catch (AWTException e)
        {
            closeEverything();
        }
        
        this.width = width;
        this.height = height;
        this.screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        this.screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        this.trianglesToDraw = new ArrayList<>();
        this.player = new Player(-1, 1.8f);
        
        try
        {
            crosshair = ImageIO.read(new File("src/res/crosshair.png"));
        }
        catch (IOException e)
        {
            closeEverything();
        }
    
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "W_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "A_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "S_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "D_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "Space_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, false), "Shift_Pressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "Pause");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "W_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "A_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "S_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "D_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "Space_Released");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "Shift_Released");
    
        ActionMap actionMap = getActionMap();
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
        actionMap.put("Shift_Pressed", new AbstractAction()
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
        actionMap.put("Shift_Released", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                keysPressed[5] = false;
            }
        });
        actionMap.put("Pause", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                inputEnabled = !inputEnabled;
            }
        });
        
        this.projectionMatrix = MathUtils.makeProjectionMatrix(fov, (float) height / (float) width, zNear, zFar);
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        ArrayList<Triangle> finalList = new ArrayList<>(trianglesToDraw);
        finalList.removeIf(Objects::isNull);
        
        g.setColor(Color.black);
        g.fillRect(0, 0, width, height);
        
        for (Triangle t : finalList)
        {
            g.setColor(t.color);
            
            int[] xValues = {(int) t.points[0].x, (int) t.points[1].x, (int) t.points[2].x};
            int[] yValues = {(int) t.points[0].y, (int) t.points[1].y, (int) t.points[2].y};
            
            g.fillPolygon(new Polygon(xValues, yValues, 3));
            // g.setColor(Color.white);
            // g.drawPolygon(new Polygon(xValues, yValues, 3));
        }
        
        g.drawImage(crosshair, getWidth() / 2 - crosshair.getWidth() / 2, getHeight() / 2 - crosshair.getHeight() / 2, null);
    }
    
    void renderTriangle(Triangle triangle, float[][] projectionMatrix, float[][] cameraMatrix)
    {
        ArrayList<Triangle> trianglesToRasterize = new ArrayList<>();
        Triangle projectedTriangle, viewedTriangle = new Triangle();
        
        Vector line1 = MathUtils.subtract(triangle.points[1], triangle.points[0]);
        Vector line2 = MathUtils.subtract(triangle.points[2], triangle.points[0]);
        
        Vector normal = MathUtils.cross(line1, line2);
        normal.normalize();
        
        Vector cameraRay = MathUtils.subtract(triangle.points[0], player.camera.origin);
        
        if (MathUtils.dot(normal, cameraRay) < 0)
        {
            Vector lightDirection = new Vector(-1.0f, 0.5f, -1.0f);
            lightDirection.normalize();
            float luminance = MathUtils.dot(lightDirection, normal);
            
            if (luminance > 1.0f)
            {
                luminance = 1.0f;
            }
            else if (luminance < 0.4f)
            {
                luminance = 0.4f;
            }
            
            viewedTriangle.color = new Color(luminance * triangle.color.getRed() / 255.0f, luminance * triangle.color.getGreen() / 255.0f, luminance * triangle.color.getBlue() / 255.0f);
            
            for (int i = 0; i < 3; i++)
            {
                viewedTriangle.points[i] = MathUtils.multiply(cameraMatrix, triangle.points[i]);
                viewedTriangle.textureCoordinates[i] = triangle.textureCoordinates[i];
                viewedTriangle.textured = triangle.textured;
            }
            
            Triangle[] clippedTriangles = MathUtils.clipAgainstPlane(new Vector(0.0f, 0.0f, 0.1f), new Vector(0.0f, 0.0f, 1.0f), viewedTriangle);
            
            for (Triangle t : clippedTriangles)
            {
                projectedTriangle = new Triangle();
                
                for (int i = 0; i < 3; i++)
                {
                    projectedTriangle.points[i] = MathUtils.multiply(projectionMatrix, t.points[i]);
                    projectedTriangle.textureCoordinates[i] = t.textureCoordinates[i];
                    projectedTriangle.textureCoordinates[i].divide(projectedTriangle.points[i].w);
                    projectedTriangle.textureCoordinates[i].w = 1.0f / projectedTriangle.points[i].w;
                    projectedTriangle.points[i].divide(projectedTriangle.points[i].w);
                    projectedTriangle.points[i].x *= -1.0f;
                    projectedTriangle.points[i].y *= -1.0f;
                    projectedTriangle.points[i].add(new Vector(1.0f, 1.0f, 0.0f));
                    projectedTriangle.points[i].x *= (0.5f * width);
                    projectedTriangle.points[i].y *= (0.5f * height);
                }
                
                projectedTriangle.textured = t.textured;
                projectedTriangle.color = t.color;
                
                trianglesToRasterize.add(projectedTriangle);
            }
            
            // Clip against 4 other planes
            for (Triangle triangleToRasterize : trianglesToRasterize)
            {
                ArrayList<Triangle> triangleList = new ArrayList<>();
                triangleList.add(triangleToRasterize);
                int numNewTriangles = 1;
                
                for (int plane = 0; plane < 4; plane++)
                {
                    Triangle[] trianglesToAdd;
                    
                    while (numNewTriangles > 0)
                    {
                        Triangle test = triangleList.remove(0);
                        numNewTriangles--;
                        
                        switch (plane)
                        {
                            // Top Plane
                            case 0:
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector(0.0f, 0.0f, 0.0f), new Vector(0.0f, 1.0f, 0.0f), test);
                                break;
                            // Bottom Plane
                            case 1:
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector(0.0f, (float) height - 1, 0.0f), new Vector(0.0f, -1.0f, 0.0f), test);
                                break;
                            // Left Plane
                            case 2:
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector(0.0f, 0.0f, 0.0f), new Vector(1.0f, 0.0f, 0.0f), test);
                                break;
                            // Right Plane
                            case 3:
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector((float) width - 1, 0.0f, 0.0f), new Vector(-1.0f, 0.0f, 0.0f), test);
                                break;
                            // We are in the mirror dimension and the very fabric of reality is breaking, RUN.
                            default:
                                throw new IllegalStateException("Unexpected value: " + plane);
                        }
                        
                        Collections.addAll(triangleList, trianglesToAdd);
                    }
                    
                    numNewTriangles = triangleList.size();
                }
                
                trianglesToDraw.addAll(triangleList);
            }
        }
    }
    
    private float newFrame(float[][] projectionMatrix)
    {
        Game game;
        if (newGameState == null || player.id == -1)
        {
            return 0;
        }
        else
        {
            game = newGameState;
            player.height = game.characterHeight;
            player.health = game.startingHealth;
        }
    
        long startOfFrame = System.nanoTime();
        trianglesToDraw.clear();
    
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        float dx = mousePosition.x - centerX;
        float dy = mousePosition.y - centerY;
    
        if (inputEnabled)
        {
            robot.mouseMove(centerX, centerY);
        }
        else
        {
            dx = 0;
            dy = 0;
        }
    
        if (!Double.isNaN(dx))
        {
            player.camera.yaw -= sensitivity * dx;
        
            while (player.camera.yaw < 0.0f)
            {
                player.camera.yaw += 360.0f;
            }
            while (player.camera.yaw >= 360.0f)
            {
                player.camera.yaw -= 360.0f;
            }
        }
    
        if (!Double.isNaN(dy))
        {
            player.camera.pitch -= sensitivity * dy;
        
            if (player.camera.pitch > 179.0f)
            {
                player.camera.pitch = 179.0f;   // 180.0f does some funky stuff with camera
            }
            else if (player.camera.pitch < -179.0f)
            {
                player.camera.pitch = -179.0f;  // -180.0f does some funky stuff with camera
            }
        }
    
        float currentSpeed = game.speed / fps;
    
        if (keysPressed[5])
        {
            currentSpeed *= 3;
        }
    
        Vector up = new Vector(0.0f, 1.0f, 0.0f);
        Vector target = MathUtils.multiply(player.camera.lookDirection.normalized(), currentSpeed);
        target.y = 0.0f;
    
        Vector netForce = new Vector(0.0f);
    
        if (player.inAir) // Apply gravity
        {
            player.currentVerticalVelocity -= (game.gravity / fps);
        }
    
        if (inputEnabled)
        {
            if (keysPressed[0]) // W, move forward
            {
                netForce.add(target);
            }
            if (keysPressed[1]) // A, strafe left
            {
                Vector right = MathUtils.cross(target, up);
                netForce.subtract(right);
            }
            if (keysPressed[2]) // S, move backward
            {
                netForce.subtract(target);
            }
            if (keysPressed[3]) // D, strafe right
            {
                Vector right = MathUtils.cross(target, up);
                netForce.add(right);
            }
            if (keysPressed[4]) // SPACE, jump
            {
                if (!player.inAir)
                {
                    player.currentVerticalVelocity = (float) Math.sqrt(2 * game.gravity * game.jumpHeight);
                    player.inAir = true;
                }
            }
        }
    
        Vector verticalVelocityVector = MathUtils.multiply(up, player.currentVerticalVelocity / fps);
    
        netForce.add(verticalVelocityVector);
        Vector collisionPush = MathUtils.collisionDetection(player, netForce, game);
        collisionPush.multiply(currentSpeed);
        netForce.subtract(collisionPush);
        
        player.position.add(netForce);
        player.camera.origin = new Vector(player.position.x, player.position.y + game.characterHeight / 2, player.position.z);
    
        sendMessage("Player Update");
        sendObject(player);
    
        float[][] pitchCameraRotationMatrix = MathUtils.makePitchRotationMatrix(-player.camera.pitch);
        float[][] yawCameraRotationMatrix = MathUtils.makeYawRotationMatrix(player.camera.yaw);
        float[][] cameraRotationMatrix = MathUtils.multiply(pitchCameraRotationMatrix, yawCameraRotationMatrix);
    
        Vector forward = new Vector(0.0f, 0.0f, 1.0f);
        player.camera.lookDirection = MathUtils.multiply(cameraRotationMatrix, forward);
        target = MathUtils.add(player.camera.origin, player.camera.lookDirection);
    
        float[][] cameraMatrix = (MathUtils.inverseMatrix(player.camera.pointAt(target, up)));
    
        for (GameObject object : game.objects)
        {
            for (Triangle triangle : object.triangles)
            {
                renderTriangle(triangle, projectionMatrix, cameraMatrix);
            }
        }
        
        for (Player p : game.players)
        {
            float radius = 0.45f;
            player.model = new Sphere(10, 10);
            player.model.scale = new Vector(radius, p.height, radius);
            player.model.position = MathUtils.add(p.position, new Vector(0, p.height, 0));
            player.model.color = colors[p.id];
            player.model.update();
            
            if (p.id != player.id)
            {
                for (Triangle triangle : player.model.triangles)
                {
                    renderTriangle(triangle, projectionMatrix, cameraMatrix);
                }
            }
        }
        
        return (System.nanoTime() - startOfFrame) / 1e6f;    // Delta Time in millis
    }
    
    public void sendMessage(String messageToSend)
    {
        try
        {
            objectWriter.writeUTF(messageToSend);
            objectWriter.flush();
        }
        catch (IOException e)
        {
            closeEverything();
        }
    }
    
    public void sendObject(Object object)
    {
        try
        {
            objectWriter.reset();
            objectWriter.writeObject(object);
            objectWriter.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            closeEverything();
        }
    }
    
    public void listenForMessages()
    {
        new Thread(() ->
        {
            while (!socket.isClosed())
            {
                try
                {
                    String msgFromServer = objectReader.readUTF();
                    handleMessageFromServer(msgFromServer);
                }
                catch (IOException | ClassNotFoundException e)
                {
                    e.printStackTrace();
                    closeEverything();
                }
            }
        }).start();
    }
    
    public void handleMessageFromServer(String message) throws IOException, ClassNotFoundException
    {
        if (message.equals("Game"))
        {
            newGameState = (Game) objectReader.readObject();
        }
        else
        {
            String[] messageParts = message.split(" ");
            
            if (messageParts[0].equals("ID"))
            {
                player.id = Integer.parseInt(messageParts[1]);
            }
        }
    }
    
    public void closeEverything()
    {
        try
        {
            if (objectReader != null)
            {
                objectReader.close();
            }
            if (objectWriter != null)
            {
                objectWriter.close();
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
    
        boolean fullScreen = false;
    
        int width, height;
    
        if (!fullScreen)
        {
            System.out.println("Enter desired screen size (x, y) in pixels:");
            String size = scan.nextLine();
            String[] sizeInfo = size.replaceAll(",", "").split(" ");
    
            try
            {
                width = Integer.parseInt(sizeInfo[0]);
                height = Integer.parseInt(sizeInfo[1]);
            }
            catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
            {
                width = 1600;
                height = 900;
            }
        }
        else
        {
            width = Toolkit.getDefaultToolkit().getScreenSize().width;
            height = Toolkit.getDefaultToolkit().getScreenSize().height;
        }
        
        Client client = new Client(socket, width, height);
        
        JFrame frame = new JFrame("3D Game | " + username);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(width, height));
        frame.addMouseListener(client);
        frame.setResizable(false);
        if (fullScreen)
        {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
        }
        frame.setVisible(true);
        frame.add(client);
        
        BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        frame.getContentPane().setCursor(blankCursor);
    
        // Infinite loop to read messages.
        client.listenForMessages();
        
        // Initially send Username
        client.sendMessage(username);
        
        while (!socket.isClosed())
        {
            float deltaTime = client.newFrame(client.projectionMatrix);
            client.repaint();
            
            if (client.inputEnabled)
            {
                client.grabFocus();
                frame.toFront();
            }
            
            try
            {
                long delay = (long) Math.max(0, ((float) (1000 / client.fps) - deltaTime));
                Thread.sleep(delay);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                client.closeEverything();
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (player == null || newGameState == null)
        {
            return;
        }
        
        sendMessage("Shoot");
        
//        Player shot = MathUtils.shoot(player, newGameState);
//
//        if (shot != null)
//        {
//            sendMessage("Shot " + shot.id);
//        }
    }
    
    @Override
    public void mousePressed(MouseEvent e)
    {
    
    }
    
    @Override
    public void mouseReleased(MouseEvent e)
    {
    
    }
    
    @Override
    public void mouseEntered(MouseEvent e)
    {
    
    }
    
    @Override
    public void mouseExited(MouseEvent e)
    {
    
    }
}
