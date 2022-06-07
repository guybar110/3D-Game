import java.io.Serializable;

public class Player implements Serializable
{
    Camera camera;
    float currentVerticalVelocity;
    boolean inAir;
    Vector position;
    int id;
    
    Player(int id)
    {
        this.position = new Vector(0);
        this.camera = new Camera();
        this.inAir = false;
        this.currentVerticalVelocity = 0.0f;
        this.id = id;
    }
}
