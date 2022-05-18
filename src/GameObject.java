import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GameObject
{
    ArrayList<Vector> vertices;
    ArrayList<Vector2D> textureCoordinates;
    ArrayList<Triangle> triangles;
    Vector position, scale, rotation;
    BufferedImage texture;
    boolean textured;
    Hyperrectangle collisionBox;
    Color color;
    
    public GameObject()
    {
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
        this.textureCoordinates = new ArrayList<>();
        this.position = new Vector(0);
        this.scale = new Vector(1);
        this.rotation = new Vector(0);
        this.textured = false;
        this.collisionBox = null;
        this.color = Color.white;
    }
    
    public GameObject(boolean textured)
    {
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
        this.textureCoordinates = new ArrayList<>();
        this.position = new Vector(0);
        this.scale = new Vector(1);
        this.rotation = new Vector(0);
        this.textured = textured;
        this.collisionBox = null;
        this.color = Color.white;
    }
    
    void generateTriangles()
    {
    
    }
    
    void generateCollisionBox()
    {
    
    }
}
