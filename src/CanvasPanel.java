import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CanvasPanel extends JPanel
{
    public static final float GRAVITY = 9.80665f;
    JFrame frame;
    private ArrayList<Triangle> trianglesToDraw;
    float[][] projectionMatrix;
    boolean[] keysPressed;
    Camera camera = new Camera(new Vector(0.0f));
    String objectPath = "src/res/spyro_level.obj";
    String texturePath = "src/res/brick.png";
    BufferedImage crosshair;
    ArrayList<GameObject> objects;
    float[][] depthBuffer;
    int[][] textureBuffer;
    Robot robot;
    BufferedImage screenBuffer;
    Vector netForce = new Vector(0.0f);
    float fps = 1000.0f;
    float jumpHeight = 0.5f;
    float speed = 2.0f;
    float sensitivity = 0.3f;
    float characterHeight = 1.8f;
    float currentVerticalVelocity = 0.0f;
    float collisionSphereRadius = 2.0f;
    boolean inAir = false;
    long lastFrame;
    
    public CanvasPanel(JFrame frame, int width, int height)
    {
        this.frame = frame;
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);
        trianglesToDraw = new ArrayList<>();
        keysPressed = new boolean[6];
        addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
            
            }
            
            @Override
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();
                
                if (key == KeyEvent.VK_W)
                {
                    keysPressed[0] = true;
                }
                else if (key == KeyEvent.VK_A)
                {
                    keysPressed[1] = true;
                }
                else if (key == KeyEvent.VK_S)
                {
                    keysPressed[2] = true;
                }
                else if (key == KeyEvent.VK_D)
                {
                    keysPressed[3] = true;
                }
                else if (key == KeyEvent.VK_SPACE)
                {
                    keysPressed[4] = true;
                }
                else if (key == KeyEvent.VK_CONTROL)
                {
                    keysPressed[5] = true;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                int key = e.getKeyCode();
                
                if (key == KeyEvent.VK_W)
                {
                    keysPressed[0] = false;
                }
                else if (key == KeyEvent.VK_A)
                {
                    keysPressed[1] = false;
                }
                else if (key == KeyEvent.VK_S)
                {
                    keysPressed[2] = false;
                }
                else if (key == KeyEvent.VK_D)
                {
                    keysPressed[3] = false;
                }
                else if (key == KeyEvent.VK_SPACE)
                {
                    keysPressed[4] = false;
                }
                else if (key == KeyEvent.VK_CONTROL)
                {
                    keysPressed[5] = false;
                }
            }
        });
        
        BufferedImage brickTexture = null;
        
        try
        {
            brickTexture = ImageIO.read(new File(texturePath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        assert brickTexture != null;
        textureBuffer = new int[brickTexture.getWidth()][brickTexture.getHeight()];
        for (int i = 0; i < brickTexture.getWidth(); i++)
        {
            for (int j = 0; j < brickTexture.getHeight(); j++)
            {
                textureBuffer[i][j] = brickTexture.getRGB(i, j);
            }
        }
        
        float roomSize = 10.0f;
        float roomHeight = 10.0f;
        float wallThickness = 0.0f;
        
        objects = new ArrayList<>();
        Hyperrectangle ground = new Hyperrectangle(new Vector(-roomSize / 2, -wallThickness, -roomSize / 2), new Vector(roomSize / 2, 0.0f, roomSize / 2));
        Hyperrectangle northWall = new Hyperrectangle(new Vector(roomSize / 2 + wallThickness, 0.0f, roomSize / 2 + wallThickness), new Vector(-roomSize / 2 - wallThickness, roomHeight, roomSize / 2));
        Hyperrectangle westWall = new Hyperrectangle(new Vector(roomSize / 2 + wallThickness, 0.0f, roomSize / 2 + wallThickness), new Vector(roomSize / 2, roomHeight, -roomSize / 2 - wallThickness));
        Hyperrectangle southWall = new Hyperrectangle(new Vector(roomSize / 2 + wallThickness, 0.0f, -roomSize / 2), new Vector(-roomSize / 2 - wallThickness, roomHeight, -roomSize / 2 - wallThickness));
        Hyperrectangle eastWall = new Hyperrectangle(new Vector(-roomSize / 2, 0.0f, roomSize / 2 + wallThickness), new Vector(-roomSize / 2 - wallThickness, roomHeight, -roomSize / 2 - wallThickness));
        Hyperrectangle ceiling = new Hyperrectangle(new Vector(-roomSize / 2, roomHeight, -roomSize / 2), new Vector(roomSize / 2, roomHeight + wallThickness, roomSize / 2));
        Sphere sphere = new Sphere(new Vector(2, 1, 4), 1, 16, 16);
        sphere.color = Color.blue;
        sphere.scale = new Vector(1.0f, 1.0f, 1.0f);        // x y z
        sphere.rotation = new Vector(0.0f, 0.0f, 0.0f);    // pitch yaw roll
        
        objects.add(ground);
        objects.add(northWall);
        objects.add(westWall);
        objects.add(southWall);
        objects.add(eastWall);
        objects.add(ceiling);
        objects.add(sphere);
        
        try
        {
            crosshair = ImageIO.read(new File("src/res/crosshair.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        float aspectRatio = (float) getHeight() / (float) getWidth();
        float zNear = 0.1f;
        float zFar = 1000.0f;
        float fov = 90.0f;
        projectionMatrix = MathUtils.makeProjectionMatrix(fov, aspectRatio, zNear, zFar);
        depthBuffer = new float[getWidth()][getHeight()];
        
        screenBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        
        try
        {
            robot = new Robot();
        }
        catch (AWTException e)
        {
            e.printStackTrace();
        }
        
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        frame.getContentPane().setCursor(blankCursor);
        
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        robot.mouseMove(centerX, centerY);
        
        camera.origin.y += characterHeight;
        
        for (int i = 0; i < getWidth(); i++)
        {
            for (int j = 0; j < getHeight(); j++)
            {
                depthBuffer[i][j] = 0.0f;
            }
        }
        
        for (GameObject object : objects)
        {
            for (Vector p : object.vertices)
            {
                p.x *= object.scale.x;
                p.y *= object.scale.y;
                p.z *= object.scale.z;
                
                float[][] xObjectRotationMatrix = MathUtils.makePitchRotationMatrix(object.rotation.x);
                float[][] yObjectRotationMatrix = MathUtils.makeYawRotationMatrix(object.rotation.y);
                float[][] zObjectRotationMatrix = MathUtils.makeRollRotationMatrix(object.rotation.z);
                float[][] xyzObjectRotationMatrix = MathUtils.multiply(MathUtils.multiply(zObjectRotationMatrix, xObjectRotationMatrix), yObjectRotationMatrix);
                Vector rotatedPoint = MathUtils.multiply(p, xyzObjectRotationMatrix);
                
                p.x = rotatedPoint.x;
                p.y = rotatedPoint.y;
                p.z = rotatedPoint.z;
                
                p.x += object.position.x;
                p.y += object.position.y;
                p.z += object.position.z;
            }
            
            object.generateTriangles();
        }
        
        
        lastFrame = System.nanoTime();
        
        int frequency = (int) (1000 / fps);
        Timer timer = new Timer(frequency, e -> newFrame(projectionMatrix));
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(screenBuffer, 0, 0, null);
        
        for (Triangle t : trianglesToDraw)
        {
            if (!t.textured)
            {
                g.setColor(t.color);
                
                int[] xValues = {(int) t.points[0].x, (int) t.points[1].x, (int) t.points[2].x};
                int[] yValues = {(int) t.points[0].y, (int) t.points[1].y, (int) t.points[2].y};
                
                g.fillPolygon(new Polygon(xValues, yValues, 3));
                g.setColor(Color.white);
                // g.drawPolygon(new Polygon(xValues, yValues, 3));
            }
        }
        
        g.drawImage(crosshair, getWidth() / 2 - crosshair.getWidth() / 2, getHeight() / 2 - crosshair.getHeight() / 2, null);
    }
    
    private void newFrame(float[][] projectionMatrix)
    {
        trianglesToDraw.clear();
        
        for (int i = 0; i < getWidth(); i++)
        {
            for (int j = 0; j < getHeight(); j++)
            {
                depthBuffer[i][j] = 0.0f;
            }
        }
        
        Graphics2D sbg2d = screenBuffer.createGraphics();
        sbg2d.setColor(Color.black);
        sbg2d.fillRect(0, 0, getWidth(), getHeight());
        
        float deltaTime = (System.nanoTime() - lastFrame) / 1e9f;
        lastFrame = System.nanoTime();
        
        float currentSpeed = speed;
        
        if (keysPressed[5])
        {
            currentSpeed = 3 * speed;
        }
        
        Vector up = new Vector(0.0f, 1.0f, 0.0f);
        Vector target = MathUtils.multiply(camera.lookDirection.normalized(), currentSpeed * deltaTime);
        target.y = 0.0f;
        
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int) screenSize.getWidth() / 2;
        int centerY = (int) screenSize.getHeight() / 2;
        robot.mouseMove(centerX, centerY);
        float dx = mousePosition.x - centerX;
        float dy = mousePosition.y - centerY;
        
        if (!Double.isNaN(dx))
        {
            camera.yaw += sensitivity * dx;
        }
        if (!Double.isNaN(dy))
        {
            float newPitch = camera.pitch + sensitivity * dy;
            
            if (newPitch < 180.0f && newPitch > -180.0f)
            {
                camera.pitch += sensitivity * dy;
            }
        }
        
        netForce = new Vector(0.0f);
        
        if (inAir) // Apply gravity
        {
            currentVerticalVelocity -= (GRAVITY * deltaTime);
        }
        
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
            if (!inAir)
            {
                currentVerticalVelocity = (float) Math.sqrt(2 * GRAVITY * jumpHeight);
                inAir = true;
            }
        }
        
        Vector verticalVelocityVector = MathUtils.multiply(up, currentVerticalVelocity * deltaTime);
        
//        float closestFloor = 2e16f;
//
//        for (GameObject object : objects)
//        {
//            float distance = Math.abs(object.collisionBox.floorLevel - (camera.origin.y - characterHeight));
//
//            if (distance < closestFloor)
//            {
//                closestFloor = distance;
//            }
//        }
//
//
//        if (camera.origin.y - characterHeight + currentVerticalVelocity < closestFloor)
//        {
//            currentVerticalVelocity = closestFloor - (camera.origin.y - characterHeight);
//        }
    
        netForce.add(verticalVelocityVector);
        
        int collisionCount = 0;
        
        for (GameObject object : objects)
        {
            boolean collision = false;
            if (object instanceof Sphere)
            {
                float radius = ((Sphere) object).radius;
                
                if (object.scale.x == radius && object.scale.y == radius && object.scale.z == radius)
                {
                    collision = MathUtils.sphereSphereCollisionDetection(MathUtils.add(MathUtils.add(camera.origin, netForce), new Vector(0, characterHeight * 0.5f, 0)), collisionSphereRadius, (Sphere) object);
                }
                else
                {
                    collision = MathUtils.sphereBoxCollisionDetection(MathUtils.add(MathUtils.add(camera.origin, netForce), new Vector(0, characterHeight * 0.5f, 0)), collisionSphereRadius, object.collisionBox);
                }
            }
            else if (object instanceof Hyperrectangle)
            {
                collision = MathUtils.collisionDetection(MathUtils.add(MathUtils.add(camera.origin, netForce), new Vector(0, characterHeight * 0.5f, 0)), collisionSphereRadius, object);
            }
            
            if (collision)
            {
                collisionCount++;
                
                if (camera.origin.y - characterHeight / 2 >= object.collisionBox.floorLevel)
                {
                    inAir = false;
                    currentVerticalVelocity = 0.0f;
                    // netForce.y = 0.0f;
                    camera.origin.y = object.collisionBox.floorLevel + characterHeight;
                }
                else
                {
                    netForce = new Vector(0.0f);
                }
                
                for (Triangle t : object.triangles)
                {
                    t.color = Color.red;
                }
            }
            else
            {
                for (Triangle t : object.triangles)
                {
                    t.color = object.color;
                }
            }
        }
        
        System.out.println(camera.origin.y);
        
        camera.origin.add(netForce);
        
        float[][] pitchCameraRotationMatrix = MathUtils.makePitchRotationMatrix(camera.pitch);
        float[][] yawCameraRotationMatrix = MathUtils.makeYawRotationMatrix(camera.yaw);
        
        float[][] cameraRotationMatrix = MathUtils.multiply(pitchCameraRotationMatrix, yawCameraRotationMatrix);
        
        Vector forward = new Vector(0.0f, 0.0f, 1.0f);
        camera.lookDirection = MathUtils.multiply(cameraRotationMatrix, forward);
        target = MathUtils.add(camera.origin, camera.lookDirection);
        
        float[][] cameraMatrix = (MathUtils.inverseMatrix(camera.pointAt(target, up)));
        
        for (GameObject object : objects)
        {
            for (Triangle triangle : object.triangles)
            {
                calculateTriangles(triangle, projectionMatrix, cameraMatrix);
            }
        }

//        for (int i = 0; i < trianglesToDraw.size() - 1; i++)
//        {
//            Triangle a = trianglesToDraw.get(i);
//            for (int j = i + 1; j < trianglesToDraw.size(); j++)
//            {
//                Triangle b = trianglesToDraw.get(j);
//                if ((a.points[0].z + a.points[1].z + a.points[2].z / 3.0f) < (b.points[0].z + b.points[1].z + b.points[2].z / 3.0f))
//                {
//                    Collections.swap(trianglesToDraw, i, j);
//                }
//            }
//        }
        
        for (Triangle t : trianglesToDraw)
        {
            if (t.textured)
            {
                t.draw(this, objects.get(0).texture);
            }
        }
        
        repaint();
    }
    
    void calculateTriangles(Triangle triangle, float[][] projectionMatrix, float[][] cameraMatrix)
    {
        ArrayList<Triangle> trianglesToRasterize = new ArrayList<>();
        Triangle transformedTriangle = new Triangle(), projectedTriangle, viewedTriangle = new Triangle();
        
        for (int i = 0; i < 3; i++)
        {
            transformedTriangle.points[i] = triangle.points[i];
            transformedTriangle.textureCoordinates[i] = triangle.textureCoordinates[i];
            transformedTriangle.textured = triangle.textured;
            transformedTriangle.color = triangle.color;
        }
        
        Vector line1 = MathUtils.subtract(transformedTriangle.points[1], transformedTriangle.points[0]);
        Vector line2 = MathUtils.subtract(transformedTriangle.points[2], transformedTriangle.points[0]);
        
        Vector normal = MathUtils.cross(line1, line2);
        normal.normalize();
        
        Vector cameraRay = MathUtils.subtract(transformedTriangle.points[0], camera.origin);
        
        if (MathUtils.dot(normal, cameraRay) < 0)
        {
            if (!triangle.textured)
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
                
                viewedTriangle.color = new Color(luminance * transformedTriangle.color.getRed() / 255.0f, luminance * transformedTriangle.color.getGreen() / 255.0f, luminance * transformedTriangle.color.getBlue() / 255.0f);
            }
            
            for (int i = 0; i < viewedTriangle.points.length; i++)
            {
                viewedTriangle.points[i] = MathUtils.multiply(cameraMatrix, transformedTriangle.points[i]);
                viewedTriangle.textureCoordinates[i] = transformedTriangle.textureCoordinates[i];
                viewedTriangle.textured = transformedTriangle.textured;
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
                    projectedTriangle.points[i].x *= (0.5f * getWidth());
                    projectedTriangle.points[i].y *= (0.5f * getHeight());
                }
                
                projectedTriangle.textured = t.textured;
                projectedTriangle.color = viewedTriangle.color;
                
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
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector(0.0f, (float) getHeight() - 1, 0.0f), new Vector(0.0f, -1.0f, 0.0f), test);
                                break;
                            // Left Plane
                            case 2:
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector(0.0f, 0.0f, 0.0f), new Vector(1.0f, 0.0f, 0.0f), test);
                                break;
                            // Right Plane
                            case 3:
                                trianglesToAdd = MathUtils.clipAgainstPlane(new Vector((float) getWidth() - 1, 0.0f, 0.0f), new Vector(-1.0f, 0.0f, 0.0f), test);
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
    
    GameObject parseObjectFile(String path, boolean hasTexture)
    {
        GameObject object = new GameObject();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path))))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                line = line.replaceAll("/", " ");
                line = line.replaceAll(" {2}", " ");
                System.out.println(line);
                
                String[] lineParts = line.split(" ");
                
                if (line.startsWith("v"))
                {
                    if (line.startsWith("vt"))
                    {
                        object.textureCoordinates.add(new Vector2D(Float.parseFloat(lineParts[1]), Float.parseFloat(lineParts[2])));
                    }
                    else if (line.startsWith("v "))
                    {
                        object.vertices.add(new Vector(Float.parseFloat(lineParts[1]), Float.parseFloat(lineParts[2]), Float.parseFloat(lineParts[3])));
                    }
                }
                if (!hasTexture)
                {
                    if (line.startsWith("f"))
                    {
                        if (lineParts.length == 4)
                        {
                            object.triangles.add(new Triangle(object.vertices.get(Integer.parseInt(lineParts[1]) - 1), object.vertices.get(Integer.parseInt(lineParts[2]) - 1), object.vertices.get(Integer.parseInt(lineParts[3]) - 1), false));
                        }
                        else
                        {
                            object.triangles.add(new Triangle(object.vertices.get(Integer.parseInt(lineParts[1]) - 1), object.vertices.get(Integer.parseInt(lineParts[3]) - 1), object.vertices.get(Integer.parseInt(lineParts[5]) - 1), false));
                            if (lineParts.length == 9)
                            {
                                object.triangles.add(new Triangle(object.vertices.get(Integer.parseInt(lineParts[1]) - 1), object.vertices.get(Integer.parseInt(lineParts[5]) - 1), object.vertices.get(Integer.parseInt(lineParts[7]) - 1), false));
                            }
                        }
                    }
                }
                else
                {
                    if (line.startsWith("f"))
                    {
                        object.triangles.add(new Triangle(object.vertices.get(Integer.parseInt(lineParts[1]) - 1), object.vertices.get(Integer.parseInt(lineParts[3]) - 1), object.vertices.get(Integer.parseInt(lineParts[5]) - 1), object.textureCoordinates.get(Integer.parseInt(lineParts[2]) - 1), object.textureCoordinates.get(Integer.parseInt(lineParts[4]) - 1), object.textureCoordinates.get(Integer.parseInt(lineParts[6]) - 1), true));
                        if (lineParts.length == 9)
                        {
                            object.triangles.add(new Triangle(object.vertices.get(Integer.parseInt(lineParts[1]) - 1), object.vertices.get(Integer.parseInt(lineParts[5]) - 1), object.vertices.get(Integer.parseInt(lineParts[7]) - 1), object.textureCoordinates.get(Integer.parseInt(lineParts[2]) - 1), object.textureCoordinates.get(Integer.parseInt(lineParts[6]) - 1), object.textureCoordinates.get(Integer.parseInt(lineParts[8]) - 1), true));
                        }
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        object.textured = hasTexture;
        
        return object;
    }
}
