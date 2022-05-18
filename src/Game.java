import java.awt.*;
import java.util.ArrayList;

public class Game
{
    float gravity, jumpHeight, speed, sensitivity, characterHeight, collisionSphereRadius, roomSize, roomHeight;
    int fps;
    
    ArrayList<GameObject> objects;
    
    Game()
    {
        this.fps = 5;
        this.gravity = 9.80665f;
        this.jumpHeight = 0.5f;
        this.speed = 2.0f;
        this.sensitivity = 0.3f;
        this.characterHeight = 1.8f;
        this.collisionSphereRadius = 0.9f;
        this.roomSize = 10.0f;
        
        this.objects = new ArrayList<>();
        
        setUpObjects();
    }
    
    private void setUpObjects()
    {
        Hyperrectangle ground = new Hyperrectangle(new Vector(-roomSize / 2, 0, -roomSize / 2), new Vector(roomSize / 2, 0.0f, roomSize / 2));
        Hyperrectangle northWall = new Hyperrectangle(new Vector(roomSize / 2, 0.0f, roomSize / 2), new Vector(-roomSize / 2, roomHeight, roomSize / 2));
        Hyperrectangle westWall = new Hyperrectangle(new Vector(roomSize / 2, 0.0f, roomSize / 2), new Vector(roomSize / 2, roomHeight, -roomSize / 2));
        Hyperrectangle southWall = new Hyperrectangle(new Vector(roomSize / 2, 0.0f, -roomSize / 2), new Vector(-roomSize / 2, roomHeight, -roomSize / 2));
        Hyperrectangle eastWall = new Hyperrectangle(new Vector(-roomSize / 2, 0.0f, roomSize / 2), new Vector(-roomSize / 2, roomHeight, -roomSize / 2));
        Hyperrectangle ceiling = new Hyperrectangle(new Vector(-roomSize / 2, roomHeight, -roomSize / 2), new Vector(roomSize / 2, roomHeight, roomSize / 2));
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
    }
}
