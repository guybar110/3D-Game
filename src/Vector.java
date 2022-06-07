import java.io.Serializable;

public class Vector implements Serializable
{
    float x, y, z, w;
    
    public Vector(float f)
    {
        this.x = f;
        this.y = f;
        this.z = f;
        this.w = 1.0f;
    }
    
    public Vector(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = 1.0f;
    }
    
    public Vector(Vector v)
    {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
    }
    
    float getLength()
    {
        return (float) Math.sqrt(this.dot(this));
    }
    
    float getLength2()
    {
        return this.dot(this);
    }
    
    void normalize()
    {
        float l = getLength();
        this.divide(l);
    }
    
    Vector normalized()
    {
        float l = getLength();
        if (l != 0)
        {
            float normalizedX = x / l;
            float normalizedY = y / l;
            float normalizedZ = z / l;
            return new Vector(normalizedX, normalizedY, normalizedZ);
        }
        else return this;
    }
    
    float dot(Vector v)
    {
        return x * v.x + y * v.y + z * v.z;
    }
    
    Vector cross(Vector v)
    {
        return new Vector(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }
    
    void add(Vector v)
    {
        x += v.x;
        y += v.y;
        z += v.z;
    }
    
    void subtract(Vector v)
    {
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }
    
    void multiply(float f)
    {
        x *= f;
        y *= f;
        z *= f;
    }
    
    void divide(float f)
    {
        if (f != 0)
        {
            x /= f;
            y /= f;
            z /= f;
        }
    }
    
    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
