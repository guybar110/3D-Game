import java.awt.*;
import java.io.Serializable;

public class Triangle implements Serializable
{
    Vector[] points;
    Vector2D[] textureCoordinates;
    Color color;
    boolean textured;
    static final Color DEFAULT_COLOR = Color.white;
    
    public Triangle()
    {
        this(new Vector(0), new Vector(0), new Vector(0), false);
    }
    
    public Triangle(Triangle t)
    {
        this(t.points[0], t.points[1], t.points[2], t.textureCoordinates[0], t.textureCoordinates[1], t.textureCoordinates[2], t.textured);
        
        this.color = t.color;
    }
    
    public Triangle(Vector a, Vector b, Vector c, boolean textured)
    {
        this(a, b, c, new Vector2D(0), new Vector2D(0), new Vector2D(0), textured);
    }
    
    public Triangle(Vector a, Vector b, Vector c, Color color)
    {
        this(a, b, c, new Vector2D(0), new Vector2D(0), new Vector2D(0), false);
        this.color = color;
    }
    
    public Triangle(Vector[] points, Color color, boolean textured)
    {
        this(points[0], points[1], points[2], textured);
    }
    
    public Triangle(Vector a, Vector b, Vector c, Vector2D d, Vector2D e, Vector2D f)
    {
        this(a, b, c, d, e, f, false);
    }
    
    public Triangle(Vector a, Vector b, Vector c, Vector2D d, Vector2D e, Vector2D f, boolean textured)
    {
        this(a, b, c, d, e, f, new Vector(0), textured);
    }
    
    public Triangle(Vector a, Vector b, Vector c, Vector2D d, Vector2D e, Vector2D f, Vector normal, boolean textured)
    {
        this.points = new Vector[3];
        points[0] = a;
        points[1] = b;
        points[2] = c;
        this.textureCoordinates = new Vector2D[3];
        textureCoordinates[0] = d;
        textureCoordinates[1] = e;
        textureCoordinates[2] = f;
        this.color = DEFAULT_COLOR;
        this.textured = textured;
    }
    
    @Override
    public String toString()
    {
        return "[" + points[0] + "] [" + points[1] + "] [" + points[2] + "]";
    }
}
