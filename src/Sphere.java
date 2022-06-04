import java.util.ArrayList;

public class Sphere extends GameObject
{
    int latitudeLines, longitudeLines;
    float radius;
    private ArrayList<Vector> normals = new ArrayList<>();
    
    Sphere(int latitudeLines, int longitudeLines)
    {
        this(latitudeLines, longitudeLines, false);
        
        this.radius = 1.0f;
        this.position = new Vector(0);
    }
    
    Sphere(Vector center, float radius, int latitudeLines, int longitudeLines)
    {
        this(latitudeLines, longitudeLines, false);
        
        this.position = center;
        this.scale = new Vector(radius);
        this.radius = radius;
        
        generateTriangles();
    }
    
    Sphere(int latitudeLines, int longitudeLines, boolean textured)
    {
        super(textured);
        
        this.latitudeLines = Math.max(latitudeLines, 2);
        this.longitudeLines = Math.max(longitudeLines, 3);
        this.collisionBox = new Hyperrectangle(new Vector(-1, 0, -1), new Vector(1, 0, 1));
        
        double deltaLatitude = Math.PI / latitudeLines;
        double deltaLongitude = 2 * Math.PI / longitudeLines;
        double phi, theta;
    
        for (int i = 0; i <= latitudeLines; i++)
        {
            phi = Math.PI / 2 - i * deltaLatitude;
    
            for (int j = 0; j <= longitudeLines; j++)
            {
                theta = j * deltaLongitude;
                
                Vector vertex = new Vector((float) (Math.cos(phi) * Math.cos(theta)), (float) (Math.sin(phi)) - 1, (float) (Math.cos(phi) * Math.sin(theta)));
                vertices.add(vertex);
                Vector normal = new Vector(vertex);
                normal.divide(radius);
                normals.add(normal);
                textureCoordinates.add(new Vector2D((float) j / longitudeLines, (float) i / latitudeLines));
            }
        }
        
        generateTriangles();
    }
    
    @Override
    void generateTriangles()
    {
        triangles.clear();
    
        int k1, k2;
    
        for (int i = 0; i < latitudeLines; i++)
        {
            k1 = i * (longitudeLines + 1);
            k2 = k1 + longitudeLines + 1;
    
            for (int j = 0; j < longitudeLines; j++, k1++, k2++)
            {
                if (i != 0)
                {
                    triangles.add(new Triangle(vertices.get(k2), vertices.get(k1), vertices.get(k1 + 1), textureCoordinates.get(k2), textureCoordinates.get(k1), textureCoordinates.get(k1 + 1), textured));
                }
                
                if (i != latitudeLines - 1)
                {
                    triangles.add(new Triangle(vertices.get(k2), vertices.get(k1 + 1), vertices.get(k2 + 1), textureCoordinates.get(k2), textureCoordinates.get(k1 + 1), textureCoordinates.get(k2 + 1), textured));
                }
            }
        }
        
        setColor(color);
        
        generateCollisionBox();
    }
    
    @Override
    void generateCollisionBox()
    {
        this.collisionBox = new Hyperrectangle(MathUtils.add(new Vector(-scale.x, 0, -scale.z), position), MathUtils.add(new Vector(scale.x, scale.y, scale.z), position));
    }
}
