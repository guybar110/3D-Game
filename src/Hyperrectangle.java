public class Hyperrectangle extends GameObject
{
    float floorLevel;
    
    Hyperrectangle(Vector point1, Vector point2)
    {
        this(point1, point2, false);
    }
    
    Hyperrectangle(Vector point1, Vector point2, boolean textured)
    {
        this.textured = textured;
        this.floorLevel = point2.y;
    
        Vector pointA = new Vector(point1.x, point1.y, point1.z);
        Vector pointB = new Vector(point1.x, point2.y, point1.z);
        Vector pointC = new Vector(point2.x, point2.y, point1.z);
        Vector pointD = new Vector(point2.x, point1.y, point1.z);
        Vector pointE = new Vector(point1.x, point1.y, point2.z);
        Vector pointF = new Vector(point1.x, point2.y, point2.z);
        Vector pointG = new Vector(point2.x, point2.y, point2.z);
        Vector pointH = new Vector(point2.x, point1.y, point2.z);
    
        this.vertices.add(pointA);
        this.vertices.add(pointB);
        this.vertices.add(pointC);
        this.vertices.add(pointD);
        this.vertices.add(pointE);
        this.vertices.add(pointF);
        this.vertices.add(pointG);
        this.vertices.add(pointH);
    
        generateTriangles();
    }
    
    @Override
    void generateTriangles()
    {
        this.triangles.add(new Triangle(vertices.get(0), vertices.get(1), vertices.get(2), new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector(0.0f, 0.0f, -1.0f), textured));
        this.triangles.add(new Triangle(vertices.get(0), vertices.get(2), vertices.get(3), new Vector2D(0.0f, 1.0f), new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 1.0f), new Vector(0.0f, 0.0f, -1.0f), textured));
        this.triangles.add(new Triangle(vertices.get(3), vertices.get(2), vertices.get(6), new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector(1.0f, 0.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(3), vertices.get(6), vertices.get(7), new Vector2D(0.0f, 1.0f), new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 1.0f), new Vector(1.0f, 0.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(7), vertices.get(6), vertices.get(5), new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector(0.0f, 0.0f, 1.0f), textured));
        this.triangles.add(new Triangle(vertices.get(7), vertices.get(5), vertices.get(4), new Vector2D(0.0f, 1.0f), new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 1.0f), new Vector(0.0f, 0.0f, 1.0f), textured));
        this.triangles.add(new Triangle(vertices.get(4), vertices.get(5), vertices.get(1), new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector(-1.0f, 0.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(4), vertices.get(1), vertices.get(0), new Vector2D(0.0f, 1.0f), new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 1.0f), new Vector(-1.0f, 0.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(1), vertices.get(5), vertices.get(6), new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector(0.0f, 1.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(1), vertices.get(6), vertices.get(2), new Vector2D(0.0f, 1.0f), new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 1.0f), new Vector(0.0f, 1.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(7), vertices.get(4), vertices.get(0), new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector(0.0f, -1.0f, 0.0f), textured));
        this.triangles.add(new Triangle(vertices.get(7), vertices.get(0), vertices.get(3), new Vector2D(0.0f, 1.0f), new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 1.0f), new Vector(0.0f, -1.0f, 0.0f), textured));
        
        generateCollisionBox();
    }
    
    @Override
    void generateCollisionBox()
    {
        collisionBox = this;
    }
}
