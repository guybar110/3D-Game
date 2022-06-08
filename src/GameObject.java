import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

public abstract class GameObject implements Serializable
{
    ArrayList<Vector> vertices;
    ArrayList<Vector2D> textureCoordinates;
    ArrayList<Triangle> triangles;
    Vector position, scale, rotation;
    BufferedImage texture;
    boolean textured;
    Hyperrectangle collisionBox;
    Color color;
    boolean isPlayerModel;
    
    public GameObject()
    {
        this(false, false);
    }
    
    public GameObject(boolean isPlayerModel)
    {
        this(isPlayerModel, false);
    }
    
    public GameObject(boolean isPlayerModel, boolean textured)
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
        this.isPlayerModel = isPlayerModel;
    }
    
    void generateTriangles()
    {
    
    }
    
    void generateCollisionBox()
    {
    
    }
    
    void update()
    {
        for (Vector p : vertices)
        {
            p.x *= scale.x;
            p.y *= scale.y;
            p.z *= scale.z;
        
            float[][] xObjectRotationMatrix = MathUtils.makePitchRotationMatrix(rotation.x);
            float[][] yObjectRotationMatrix = MathUtils.makeYawRotationMatrix(rotation.y);
            float[][] zObjectRotationMatrix = MathUtils.makeRollRotationMatrix(rotation.z);
            float[][] xyzObjectRotationMatrix = MathUtils.multiply(MathUtils.multiply(zObjectRotationMatrix, xObjectRotationMatrix), yObjectRotationMatrix);
            Vector rotatedPoint = MathUtils.multiply(p, xyzObjectRotationMatrix);
        
            p.x = rotatedPoint.x;
            p.y = rotatedPoint.y;
            p.z = rotatedPoint.z;
        
            p.x += position.x;
            p.y += position.y;
            p.z += position.z;
        }
        
        for (Triangle t : triangles)
        {
            t.color = color;
        }
    }
    
    void setColor(Color c)
    {
        color = c;
        
        for (Triangle t : triangles)
        {
            t.color = c;
        }
    }
}
