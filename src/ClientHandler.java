import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

public class ClientHandler implements Runnable
{
    static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    
    private static final float fov = 90.0f, zFar = 0.1f, zNear = 1000.0f;
    
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clientUsername;
    private Game game;
    private int width, height;
    private ArrayList<Triangle> trianglesToDraw;
    private boolean[] keysPressed;
    private float[][] depthBuffer;
    private float mouseDX, mouseDY;
    private Vector netForce;
    private float currentVerticalVelocity;
    private boolean inAir;
    private Camera camera;
    
    public ClientHandler(Game game, Socket socket)
    {
        this.game = game;
        try
        {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = bufferedReader.readLine();    // When a client connects their username is sent
            String[] sizeInfo = bufferedReader.readLine().split(" ");
            this.width = Integer.parseInt(sizeInfo[1]);
            this.height = Integer.parseInt(sizeInfo[2]);
            clientHandlers.add(this);
            System.out.println("SERVER: " + clientUsername + " has joined the game.");
        }
        catch (IOException e)
        {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    
        this.trianglesToDraw = new ArrayList<>();
        this.keysPressed = new boolean[6];
        this.netForce = new Vector(0);
        this.currentVerticalVelocity = 0;
        this.inAir = false;
        this.depthBuffer = new float[width][height];
        this.camera = new Camera();
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
    
    public void listenForMessage()
    {
        new Thread(() ->
        {
            String messageFromClient;
            
            while (!socket.isClosed())
            {
                try
                {
                    messageFromClient = bufferedReader.readLine();
                    
                    handleClientMessage(messageFromClient);
                }
                catch (IOException e)
                {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }).start();
    }
    
    public void handleClientMessage(String message)
    {
        if (message.startsWith("keys: "))
        {
            String[] keyInfo = message.replaceAll(",", "").split(" ");
    
            for (int i = 0; i < keysPressed.length; i++)
            {
                keysPressed[i] = Boolean.parseBoolean(keyInfo[i + 1]);
            }
        }
        else if (message.startsWith("mouse_update: "))
        {
            String[] mouseInfo = message.split(" ");
            
            mouseDX = Float.parseFloat(mouseInfo[1]);
            mouseDY = Float.parseFloat(mouseInfo[2]);
        }
    }
    
    // If the client disconnects for any reason remove them from the list so a message isn't sent down a broken connection.
    public void remove()
    {
        clientHandlers.remove(this);
    
        System.out.println("SERVER: " + clientUsername + " has disconnected.");
    }
    
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter)
    {
        remove();
        
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
    
    private void newFrame(float[][] projectionMatrix)
    {
        long startOfFrame = System.nanoTime();
    
        sendMessage("Update Mouse");
        
        trianglesToDraw.clear();
        
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                depthBuffer[i][j] = 0.0f;
            }
        }
        
        float currentSpeed = game.speed;
        
        if (keysPressed[5])
        {
            currentSpeed = 3 * game.speed;
        }
        
        Vector up = new Vector(0.0f, 1.0f, 0.0f);
        Vector target = MathUtils.multiply(camera.lookDirection.normalized(), currentSpeed / game.fps);
        target.y = 0.0f;
        
        
        if (!Double.isNaN(mouseDX))
        {
            camera.yaw += game.sensitivity * mouseDX;
        }
        if (!Double.isNaN(mouseDY))
        {
            float newPitch = camera.pitch + game.sensitivity * mouseDY;
            
            if (newPitch < 180.0f && newPitch > -180.0f)
            {
                camera.pitch += game.sensitivity * mouseDY;
            }
        }
        
        netForce = new Vector(0.0f);
        
        if (inAir) // Apply gravity
        {
            currentVerticalVelocity -= (game.gravity / game.fps);
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
                currentVerticalVelocity = (float) Math.sqrt(2 * game.gravity * game.jumpHeight);
                inAir = true;
            }
        }
        
        Vector verticalVelocityVector = MathUtils.multiply(up, currentVerticalVelocity / game.fps);
        
        netForce.add(verticalVelocityVector);
        
        int collisionCount = 0;
        
        for (GameObject object : game.objects)
        {
            boolean collision = false;
            if (object instanceof Sphere)
            {
                float radius = ((Sphere) object).radius;
                
                if (object.scale.x == radius && object.scale.y == radius && object.scale.z == radius)
                {
                    collision = MathUtils.sphereSphereCollisionDetection(MathUtils.add(MathUtils.add(camera.origin, netForce), new Vector(0, game.characterHeight * 0.5f, 0)), game.collisionSphereRadius, (Sphere) object);
                }
                else
                {
                    collision = MathUtils.sphereBoxCollisionDetection(MathUtils.add(MathUtils.add(camera.origin, netForce), new Vector(0, game.characterHeight * 0.5f, 0)), game.collisionSphereRadius, object.collisionBox);
                }
            }
            else if (object instanceof Hyperrectangle)
            {
                collision = MathUtils.collisionDetection(MathUtils.add(MathUtils.add(camera.origin, netForce), new Vector(0, game.characterHeight * 0.5f, 0)), game.collisionSphereRadius, object);
            }
            
            if (collision)
            {
                collisionCount++;
                
                if (camera.origin.y - game.characterHeight / 2 >= object.collisionBox.floorLevel)
                {
                    inAir = false;
                    currentVerticalVelocity = 0.0f;
                    // netForce.y = 0.0f;
                    camera.origin.y = object.collisionBox.floorLevel + game.characterHeight;
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
        
        camera.origin.add(netForce);
        
        float[][] pitchCameraRotationMatrix = MathUtils.makePitchRotationMatrix(camera.pitch);
        float[][] yawCameraRotationMatrix = MathUtils.makeYawRotationMatrix(camera.yaw);
        
        float[][] cameraRotationMatrix = MathUtils.multiply(pitchCameraRotationMatrix, yawCameraRotationMatrix);
        
        Vector forward = new Vector(0.0f, 0.0f, 1.0f);
        camera.lookDirection = MathUtils.multiply(cameraRotationMatrix, forward);
        target = MathUtils.add(camera.origin, camera.lookDirection);
        
        float[][] cameraMatrix = (MathUtils.inverseMatrix(camera.pointAt(target, up)));
        
        for (GameObject object : game.objects)
        {
            for (Triangle triangle : object.triangles)
            {
                calculateTriangles(triangle, projectionMatrix, cameraMatrix);
            }
        }
        
        sendMessage("clear triangles");
        for (Triangle t : trianglesToDraw)
        {
            String triangleString = t.toString().replaceAll("[\\[\\](),]", "");
            String triangleColor = " " + t.color.getRed() + " " + t.color.getGreen() + " " + t.color.getBlue();
            sendMessage("triangle: " + triangleString + triangleColor);
        }
    
        try
        {
            Thread.sleep(1000 / game.fps - (System.nanoTime() - startOfFrame) / 1000000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
    
    void calculateTriangles(Triangle triangle, float[][] projectionMatrix, float[][] cameraMatrix)
    {
        ArrayList<Triangle> trianglesToRasterize = new ArrayList<>();
        Triangle projectedTriangle, viewedTriangle = new Triangle();
        
        Vector line1 = MathUtils.subtract(triangle.points[1], triangle.points[0]);
        Vector line2 = MathUtils.subtract(triangle.points[2], triangle.points[0]);
        
        Vector normal = MathUtils.cross(line1, line2);
        normal.normalize();
        
        Vector cameraRay = MathUtils.subtract(triangle.points[0], camera.origin);
        
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
                
                viewedTriangle.color = new Color(luminance * triangle.color.getRed() / 255.0f, luminance * triangle.color.getGreen() / 255.0f, luminance * triangle.color.getBlue() / 255.0f);
            }
            
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
    
    @Override
    public void run()
    {
        listenForMessage();
        
        float[][] projectionMatrix = MathUtils.makeProjectionMatrix(fov, (float) height / (float) width, zNear, zFar);
        
        // Server main loop
        while (!socket.isClosed())
        {
            newFrame(projectionMatrix);
            System.out.println(camera.origin);
            System.out.println(camera.lookDirection);
        }
    }
}
