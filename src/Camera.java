import java.io.Serializable;

public class Camera implements Serializable
{
    Vector origin, lookDirection;
    float pitch, yaw;
    
    public Camera()
    {
        this(new Vector(0));
    }
    
    public Camera(Vector origin)
    {
        this.origin = origin;
        this.lookDirection = new Vector(0.0f, 0.0f, 1.0f);
        yaw = 0.0f;
        pitch = 0.0f;
    }
    
    public float[][] pointAt(Vector forward, Vector up)
    {
        Vector newForward = MathUtils.subtract(forward, origin);
        newForward.normalize();
        
        Vector a = MathUtils.multiply(newForward, MathUtils.dot(up, newForward));
        Vector newUp = MathUtils.subtract(up, a);
        newUp.normalize();
        
        Vector newRight = MathUtils.cross(newUp, newForward);
        
        float[][] pointAtMatrix = new float[4][4];
        pointAtMatrix[0][0] = newRight.x;   pointAtMatrix[0][1] = newRight.y;   pointAtMatrix[0][2] = newRight.z;   pointAtMatrix[0][3] = 0.0f;
        pointAtMatrix[1][0] = newUp.x;   pointAtMatrix[1][1] = newUp.y;   pointAtMatrix[1][2] = newUp.z;   pointAtMatrix[1][3] = 0.0f;
        pointAtMatrix[2][0] = newForward.x;   pointAtMatrix[2][1] = newForward.y;   pointAtMatrix[2][2] = newForward.z;   pointAtMatrix[2][3] = 0.0f;
        pointAtMatrix[3][0] = origin.x;   pointAtMatrix[3][1] = origin.y;   pointAtMatrix[3][2] = origin.z;   pointAtMatrix[3][3] = 1.0f;
        return pointAtMatrix;
    }
}
