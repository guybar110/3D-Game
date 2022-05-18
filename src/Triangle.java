import java.awt.*;
import java.awt.image.BufferedImage;

public class Triangle
{
    Vector[] points;
    Vector normal;
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
        this.normal = normal;
    }
    
    void draw(CanvasPanel viewport)
    {
        draw(viewport, null);
    }
    
    void draw(CanvasPanel viewport, BufferedImage texture)
    {
        int x1 = (int) this.points[0].x, x2 = (int) this.points[1].x, x3 = (int) this.points[2].x;
        int y1 = (int) this.points[0].y, y2 = (int) this.points[1].y, y3 = (int) this.points[2].y;
        float u1 = this.textureCoordinates[0].u, u2 = this.textureCoordinates[1].u, u3 = this.textureCoordinates[2].u;
        float v1 = this.textureCoordinates[0].v, v2 = this.textureCoordinates[1].v, v3 = this.textureCoordinates[2].v;
        float w1 = this.textureCoordinates[0].w, w2 = this.textureCoordinates[1].w, w3 = this.textureCoordinates[2].w;
        int temp1;
        float temp2;
        
        if (y2 < y1)
        {
            temp1 = y1;
            y1 = y2;
            y2 = temp1;
            
            temp1 = x1;
            x1 = x2;
            x2 = temp1;
            
            temp2 = u1;
            u1 = u2;
            u2 = temp2;
            
            temp2 = v1;
            v1 = v2;
            v2 = temp2;
            
            temp2 = w1;
            w1 = w2;
            w2 = temp2;
        }
        
        if (y3 < y1)
        {
            temp1 = y1;
            y1 = y3;
            y3 = temp1;
            
            temp1 = x1;
            x1 = x3;
            x3 = temp1;
            
            temp2 = u1;
            u1 = u3;
            u3 = temp2;
            
            temp2 = v1;
            v1 = v3;
            v3 = temp2;
            
            temp2 = w1;
            w1 = w3;
            w3 = temp2;
        }
        
        if (y3 < y2)
        {
            temp1 = y2;
            y2 = y3;
            y3 = temp1;
            
            temp1 = x2;
            x2 = x3;
            x3 = temp1;
            
            temp2 = u2;
            u2 = u3;
            u3 = temp2;
            
            temp2 = v2;
            v2 = v3;
            v3 = temp2;
            
            temp2 = w2;
            w2 = w3;
            w3 = temp2;
        }
        
        int dy1 = y2 - y1;
        int dx1 = x2 - x1;
        float du1 = u2 - u1;
        float dv1 = v2 - v1;
        float dw1 = w2 - w1;
        
        int dy2 = y3 - y1;
        int dx2 = x3 - x1;
        float dv2 = v3 - v1;
        float du2 = u3 - u1;
        float dw2 = w3 - w1;
        
        float uTexture, vTexture, wTexture;
        float dx1Step = 0, dx2Step = 0, du1Step = 0, du2Step = 0, dv1Step = 0, dv2Step = 0, dw1Step = 0, dw2Step = 0;

        if (dy1 != 0)
        {
            dx1Step = dx1 / (float) Math.abs(dy1);
        }
        if (dy2 != 0)
        {
            dx2Step = dx2 / (float) Math.abs(dy2);
        }
        if (dy1 != 0)
        {
            du1Step = du1 / (float) Math.abs(dy1);
        }
        if (dy2 != 0)
        {
            du2Step = du2 / (float) Math.abs(dy2);
        }
        if (dy1 != 0)
        {
            dv1Step = dv1 / (float) Math.abs(dy1);
        }
        if (dy2 != 0)
        {
            dv2Step = dv2 / (float) Math.abs(dy2);
        }
        if (dy1 != 0)
        {
            dw1Step = dw1 / (float) Math.abs(dy1);
        }
        if (dy2 != 0)
        {
            dw2Step = dw2 / (float) Math.abs(dy2);
        }
    
        if (dy1 != 0)
        {
            for (int y = y1; y <= y2; y++)
            {
                int xStart = (int) (x1 + (y - y1) * dx1Step);
                int xEnd = (int) (x1 + (y - y1) * dx2Step);
                
                float uStart = u1 + (y - y1) * du1Step;
                float uEnd = u1 + (y - y1) * du2Step;
                
                float vStart = v1 + (y - y1) * dv1Step;
                float vEnd = v1 + (y - y1) * dv2Step;
                
                float wStart = w1 + (y - y1) * dw1Step;
                float wEnd = w1 + (y - y1) * dw2Step;
                
                if (xStart > xEnd)
                {
                    temp1 = xStart;
                    xStart = xEnd;
                    xEnd = temp1;
                    
                    temp2 = uStart;
                    uStart = uEnd;
                    uEnd = temp2;
                    
                    temp2 = vStart;
                    vStart = vEnd;
                    vEnd = temp2;
                    
                    temp2 = wStart;
                    wStart = wEnd;
                    wEnd = temp2;
                }
                
                float tStep = 1.0f / (xEnd - xStart);
                float t = 0.0f;
    
                for (int x = xStart; x < xEnd; x++)
                {
                    uTexture = (1.0f - t) * uStart + t * uEnd;
                    vTexture = (1.0f - t) * vStart + t * vEnd;
                    wTexture = (1.0f - t) * wStart + t * wEnd;
    
                    if (uTexture / wTexture >= 1)
                    {
                        // uTexture -= ((int) uTexture);
                        uTexture = 0;
                    }
                    if (vTexture / wTexture >= 1)
                    {
                        // vTexture -= ((int) vTexture);
                        vTexture = 0;
                    }

                    int pixelColor;

                    if (textured)
                    {
                        int uTextureCoordinates = (int) ((uTexture / wTexture) * texture.getWidth());
                        int vTextureCoordinates = (int) ((vTexture / wTexture) * texture.getHeight());
                        try
                        {
                            // pixelColor = texture.getRGB(uTextureCoordinates, vTextureCoordinates);
                            pixelColor = viewport.textureBuffer[uTextureCoordinates][vTextureCoordinates];
                        }
                        catch (ArrayIndexOutOfBoundsException e)
                        {
                            pixelColor = Color.black.getRGB();
                        }
                    }
                    else
                    {
                        pixelColor = color.getRGB();
                    }

                    if (wTexture > viewport.depthBuffer[x][y])
                    {
                        viewport.screenBuffer.setRGB(x, y, pixelColor);
                        viewport.depthBuffer[x][y] = wTexture;
                    }
                    
                    t += tStep;
                }
            }
        }
        
        dy1 = y3 - y2;
        dx1 = x3 - x2;
        du1 = u3 - u2;
        dv1 = v3 - v2;
        dw1 = w3 - w2;
        
        if (dy1 != 0)
        {
            dx1Step = dx1 / (float) Math.abs(dy1);
        }
        if (dy2 != 0)
        {
            dx2Step = dx2 / (float) Math.abs(dy2);
        }
        if (dy1 != 0)
        {
            du1Step = du1 / (float) Math.abs(dy1);
        }
        if (dy1 != 0)
        {
            dv1Step = dv1 / (float) Math.abs(dy1);
        }
        if (dy1 != 0)
        {
            dw1Step = dw1 / (float) Math.abs(dy1);
        }
        
        if (dy1 != 0)
        {
            for (int y = y2; y <= y3; y++)
            {
                int xStart = (int) (x2 + (y - y2) * dx1Step);
                int xEnd = (int) (x1 + (y - y1) * dx2Step);
                
                float uStart = u2 + (y - y2) * du1Step;
                float uEnd = u1 + (y - y1) * du2Step;
                
                float vStart = v2 + (y - y2) * dv1Step;
                float vEnd = v1 + (y - y1) * dv2Step;
                
                float wStart = w2 + (y - y2) * dw1Step;
                float wEnd = w1 + (y - y1) * dw2Step;
                
                if (xStart > xEnd)
                {
                    temp1 = xStart;
                    xStart = xEnd;
                    xEnd = temp1;
                    
                    temp2 = uStart;
                    uStart = uEnd;
                    uEnd = temp2;
                    
                    temp2 = vStart;
                    vStart = vEnd;
                    vEnd = temp2;
                    
                    temp2 = wStart;
                    wStart = wEnd;
                    wEnd = temp2;
                }
                
                float tStep = 1.0f / (xEnd - xStart);
                float t = 0.0f;
                
                for (int x = xStart; x < xEnd; x++)
                {
                    uTexture = (1.0f - t) * uStart + t * uEnd;
                    vTexture = (1.0f - t) * vStart + t * vEnd;
                    wTexture = (1.0f - t) * wStart + t * wEnd;
    
                    if (uTexture / wTexture >= 1)
                    {
                        // uTexture -= ((int) uTexture);
                        uTexture = 0;
                    }
                    if (vTexture / wTexture >= 1)
                    {
                        // vTexture -= ((int) vTexture);
                        vTexture = 0;
                    }
                    
                    int pixelColor;
                    
                    if (textured)
                    {
                        int uTextureCoordinates = (int) ((uTexture / wTexture) * texture.getWidth());
                        int vTextureCoordinates = (int) ((vTexture / wTexture) * texture.getHeight());
                        try
                        {
                            // pixelColor = texture.getRGB(uTextureCoordinates, vTextureCoordinates);
                            pixelColor = viewport.textureBuffer[uTextureCoordinates][vTextureCoordinates];
                        }
                        catch (ArrayIndexOutOfBoundsException e)
                        {
                            pixelColor = Color.black.getRGB();
                        }
                    }
                    else
                    {
                        pixelColor = color.getRGB();
                    }
                    
                    if (wTexture > viewport.depthBuffer[x][y])
                    {
                        viewport.screenBuffer.setRGB(x, y, pixelColor);
                        viewport.depthBuffer[x][y] = wTexture;
                    }
                    
                    t += tStep;
                }
            }
        }
    }
    
    @Override
    public String toString()
    {
        return "[" + points[0] + "] [" + points[1] + "] [" + points[2] + "]";
    }
}
