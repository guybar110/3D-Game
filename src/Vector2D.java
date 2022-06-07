import java.io.Serializable;

public class Vector2D implements Serializable
{
    float u, v, w;
    
    Vector2D(float f)
    {
        this.u = f;
        this.v = f;
        this.w = 1.0f;
    }
    
    Vector2D(float u, float v)
    {
        this.u = u;
        this.v = v;
        this.w = 1.0f;
    }
    
    Vector2D(float u, float v, float w)
    {
        this.u = u;
        this.v = v;
        this.w = w;
    }
    
    Vector2D(Vector2D v)
    {
        this.u = v.u;
        this.v = v.v;
        this.w = v.w;
    }
    
    void divide(float f)
    {
        if (f != 0)
        {
            this.u /= f;
            this.v /= f;
        }
    }
    
    float length()
    {
        return (float) Math.sqrt(this.u * this.u + this.v * this.v);
    }
    
    @Override
    public String toString()
    {
        return "(" + u + ", " + v + ")";
    }
}
