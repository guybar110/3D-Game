import java.util.ArrayList;

public class MathUtils
{
    public static Vector multiply(float[][] matrix, Vector vector)
    {
        return multiply(vector, matrix);
    }
    
    public static Vector multiply(Vector vector, float[][] matrix)
    {
        Vector product = new Vector(0);
        
        product.x = vector.x * matrix[0][0] + vector.y * matrix[1][0] + vector.z * matrix[2][0] + vector.w * matrix[3][0];
        product.y = vector.x * matrix[0][1] + vector.y * matrix[1][1] + vector.z * matrix[2][1] + vector.w * matrix[3][1];
        product.z = vector.x * matrix[0][2] + vector.y * matrix[1][2] + vector.z * matrix[2][2] + vector.w * matrix[3][2];
        product.w = vector.x * matrix[0][3] + vector.y * matrix[1][3] + vector.z * matrix[2][3] + vector.w * matrix[3][3];
        
        return product;
    }
    
    public static float[][] multiply(float[][] m1, float[][] m2)
    {
        float[][] product = new float[4][4];
        
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                product[j][i] = m1[j][0] * m2[0][i] + m1[j][1] * m2[1][i] + m1[j][2] * m2[2][i] + m1[j][3] * m2[3][i];
            }
        }
        
        return product;
    }
    
    public static void printMatrix(float[][] matrix)
    {
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    public static float[][] inverseMatrix(float[][] matrix) // ONLY FOR ROTATION/TRANSLATION MATRICES
    {
        float[][] inverted = new float[4][4];
        inverted[0][0] = matrix[0][0];
        inverted[0][1] = matrix[1][0];
        inverted[0][2] = matrix[2][0];
        inverted[0][3] = 0.0f;
        inverted[1][0] = matrix[0][1];
        inverted[1][1] = matrix[1][1];
        inverted[1][2] = matrix[2][1];
        inverted[1][3] = 0.0f;
        inverted[2][0] = matrix[0][2];
        inverted[2][1] = matrix[1][2];
        inverted[2][2] = matrix[2][2];
        inverted[2][3] = 0.0f;
        inverted[3][0] = -(matrix[3][0] * inverted[0][0] + matrix[3][1] * inverted[1][0] + matrix[3][2] * inverted[2][0]);
        inverted[3][1] = -(matrix[3][0] * inverted[0][1] + matrix[3][1] * inverted[1][1] + matrix[3][2] * inverted[2][1]);
        inverted[3][2] = -(matrix[3][0] * inverted[0][2] + matrix[3][1] * inverted[1][2] + matrix[3][2] * inverted[2][2]);
        inverted[3][3] = 1.0f;
        return inverted;
    }
    
    public static float[][] makeIdentityMatrix(int dimensions)
    {
        float[][] identityMatrix = new float[dimensions][dimensions];
        
        for (int i = 0; i < dimensions; i++)
        {
            identityMatrix[i][i] = 1.0f;
        }
        
        return identityMatrix;
    }
    
    public static float[][] makePitchRotationMatrix(float pitch) // Around X axis
    {
        float[][] rotationMatrix = new float[4][4];
        float angleRad = (float) Math.toRadians(pitch);
        
        rotationMatrix[0][0] = 1.0f;
        rotationMatrix[1][1] = (float) Math.cos(angleRad);
        rotationMatrix[1][2] = (float) Math.sin(angleRad);
        rotationMatrix[2][1] = (float) -Math.sin(angleRad * 0.5f);
        rotationMatrix[2][2] = (float) Math.cos(angleRad * 0.5f);
        rotationMatrix[3][3] = 1.0f;
        
        return rotationMatrix;
    }
    
    public static float[][] makeYawRotationMatrix(float yaw) // Around Y axis
    {
        float[][] rotationMatrix = new float[4][4];
        float angleRad = (float) Math.toRadians(yaw);
        
        rotationMatrix[0][0] = (float) Math.cos(angleRad);
        rotationMatrix[0][2] = (float) -Math.sin(angleRad);
        rotationMatrix[1][1] = 1.0f;
        rotationMatrix[2][0] = (float) Math.sin(angleRad);
        rotationMatrix[2][2] = (float) Math.cos(angleRad);
        rotationMatrix[3][3] = 1.0f;
        return rotationMatrix;
    }
    
    public static float[][] makeRollRotationMatrix(float roll) // Around Z axis
    {
        float[][] rotationMatrix = new float[4][4];
        float angleRad = (float) Math.toRadians(roll);
        
        rotationMatrix[0][0] = (float) Math.cos(angleRad);
        rotationMatrix[0][1] = (float) Math.sin(angleRad);
        rotationMatrix[1][0] = (float) -Math.sin(angleRad);
        rotationMatrix[1][1] = (float) Math.cos(angleRad);
        rotationMatrix[2][2] = 1.0f;
        rotationMatrix[3][3] = 1.0f;
        
        return rotationMatrix;
    }
    
    public static float[][] makeTranslationMatrix(float x, float y, float z)
    {
        float[][] translationMatrix = new float[4][4];
        translationMatrix[0][0] = 1.0f;
        translationMatrix[1][1] = 1.0f;
        translationMatrix[2][2] = 1.0f;
        translationMatrix[3][3] = 1.0f;
        translationMatrix[3][0] = x;
        translationMatrix[3][1] = y;
        translationMatrix[3][2] = z;
        return translationMatrix;
    }
    
    public static float[][] makeProjectionMatrix(float fov, float aspectRatio, float zNear, float zFar)
    {
        // float fovRad = (float) (1.0f / Math.tan(fov * 0.5f / 180.0f * Math.PI));
        float fovRad = (float) Math.toRadians(fov * 0.5f);
        float arctan = (float) Math.atan(fovRad);
        
        float[][] projectionMatrix = new float[4][4];
        projectionMatrix[0][0] = aspectRatio * arctan;
        projectionMatrix[1][1] = arctan;
        projectionMatrix[2][2] = zFar / (zFar - zNear);
        projectionMatrix[3][2] = (-zFar * zNear) / (zFar - zNear);
        projectionMatrix[2][3] = 1.0f;
        projectionMatrix[3][3] = 0.0f;
        return projectionMatrix;
    }
    
    public static Vector2D projectInto2D(Vector planeX, Vector planeY, Vector point)
    {
        return new Vector2D(point.dot(planeX), point.dot(planeY));
    }
    
    public static boolean isInside2DTriangle(Vector2D point, Vector2D[] trianglePoints)
    {
        for (int i = 0; i < trianglePoints.length; i++)
        {
            int next = i + 1;
            if (next == trianglePoints.length)
            {
                next = 0;
            }
            
            if (triangle2DArea(trianglePoints[i], trianglePoints[next], point) <= 0.0f)
            {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean sphereSphereCollisionDetection(Vector collisionSphereOrigin, float collisionSphereRadius, Sphere sphere)
    {
        return subtract(collisionSphereOrigin, sphere.position).getLength() < collisionSphereRadius + sphere.radius;
    }
    
    public static Vector collisionDetection(Vector collisionSphereOrigin, float collisionSphereRadius, ArrayList<GameObject> objects)
    {
        int collisionCount = 0;
        Vector shiftDelta = new Vector(0);
        
        for (GameObject object : objects)
        {
            if (object instanceof Sphere)
            {
                boolean collision = sphereSphereCollisionDetection(collisionSphereOrigin, collisionSphereRadius, (Sphere) object);
                
                if (collision)
                {
                    collisionCount++;
                }
                continue;
            }
            
            if (object.collisionBox.floorLevel == collisionSphereOrigin.y - collisionSphereRadius)
            {
                continue;
            }
            
            for (Triangle t : object.triangles)
            {
                float collisionSphereRadius2 = collisionSphereRadius * collisionSphereRadius;
                
                boolean fullyInsideTriangle = false;
                boolean outsideAllVertices = true;
                boolean outsideAllEdges = true;
                
                Vector line1 = MathUtils.subtract(t.points[1], t.points[0]);
                Vector line2 = MathUtils.subtract(t.points[2], t.points[0]);
                
                Vector normal = cross(line1, line2);
                
                if (cross(line1, line2).getLength() == 0)
                {
                    continue;
                }
                
                normal.normalize();
                
                float d = -dot(normal, t.points[0]);
                float distanceFromPlane = dot(normal, collisionSphereOrigin) + d;
                
                if (Math.abs(distanceFromPlane) > collisionSphereRadius)
                {
                    continue;
                }
                
                Vector a = subtract(t.points[1], t.points[0]);
                Vector b = subtract(t.points[2], t.points[1]);
                Vector c = subtract(t.points[0], t.points[2]);
                
                Vector planeX = a.normalized();
                Vector planeY = normal.cross(a).normalized();
                
                Vector2D planePos2D = projectInto2D(planeX, planeY, collisionSphereOrigin);
                Vector2D[] trianglePoints2D = new Vector2D[]{projectInto2D(planeX, planeY, t.points[0]), projectInto2D(planeX, planeY, t.points[1]), projectInto2D(planeX, planeY, t.points[2])};
                
                if (isInside2DTriangle(planePos2D, trianglePoints2D))
                {
                    fullyInsideTriangle = true;
                }
                
                for (int i = 0; i < 3; i++)
                {
                    if (subtract(t.points[i], collisionSphereOrigin).getLength2() <= collisionSphereRadius2)
                    {
                        outsideAllVertices = false;
                    }
                }
                
                if
                (
                    intersectRaySegmentSphere(t.points[0], a, collisionSphereOrigin, collisionSphereRadius2).intersection ||
                        intersectRaySegmentSphere(t.points[1], b, collisionSphereOrigin, collisionSphereRadius2).intersection ||
                        intersectRaySegmentSphere(t.points[2], c, collisionSphereOrigin, collisionSphereRadius2).intersection
                )
                {
                    outsideAllEdges = false;
                }
                
                if (outsideAllEdges && outsideAllVertices && !fullyInsideTriangle)
                {
                    continue;
                }
                
                shiftDelta.add(multiply(normal, collisionSphereRadius - distanceFromPlane));
                collisionCount++;
            }
        }
        
        if (collisionCount != 0)
        {
            shiftDelta.divide(collisionCount);
            return shiftDelta.normalized();
        }
        
        return null;
    }
    
    public static Intersection intersectRaySegmentSphere(Vector rayOrigin, Vector rayDirection, Vector sphereOrigin, float collisionSphereRadius2)
    {
        Vector d = rayDirection.normalized();
        Vector m = subtract(rayOrigin, sphereOrigin);
        float b = dot(m, d);
        float c = m.getLength2() - collisionSphereRadius2;
        
        if (c > 0.0f && b > 0.0f)
        {
            return new Intersection(false, 0, null);
        }
        
        float discriminant = b * b - c;
        
        if (discriminant < 0.0f)
        {
            return new Intersection(false, 0, null);
        }
        
        float t = (float) (-b - Math.sqrt(discriminant));
        
        if (t < 0.0f)
        {
            t = 0.0f;
        }
        
        Vector intersectionPoint = add(rayOrigin, multiply(rayDirection, t));
        
        return new Intersection(true, t, intersectionPoint);
    }
    
    public static Intersection intersectPlane(Vector pointOnPlane, Vector planeNormal, Vector lineStart, Vector lineEnd)
    {
        planeNormal.normalize();
        Vector lineDirection = subtract(lineEnd, lineStart).normalized();
        
        if (dot(planeNormal, lineDirection) == 0)
        {
            return new Intersection(false, 0, null);
        }
        
        float t = (dot(planeNormal, pointOnPlane) - dot(planeNormal, lineStart)) / dot(planeNormal, lineDirection);
        return new Intersection(true, t, add(lineStart, multiply(lineDirection, t)));
    }
    
    public static Triangle[] clipAgainstPlane(Vector pointOnPlane, Vector planeNormal, Triangle triangle)
    {
        planeNormal.normalize();
        
        Vector[] insidePoints = new Vector[3], outsidePoints = new Vector[3];
        Vector2D[] insideTextures = new Vector2D[3], outsideTextures = new Vector2D[3];
        int insidePointCount = 0, outsidePointCount = 0;
        
        for (int i = 0; i < 3; i++)
        {
            float distance = dot(planeNormal, triangle.points[i]) - dot(planeNormal, pointOnPlane);
            
            if (distance >= 0)
            {
                insidePoints[insidePointCount] = triangle.points[i];
                insideTextures[insidePointCount++] = triangle.textureCoordinates[i];
            }
            else
            {
                outsidePoints[outsidePointCount] = triangle.points[i];
                outsideTextures[outsidePointCount++] = triangle.textureCoordinates[i];
            }
        }
        if (insidePointCount == 0) // Clip whole triangle, it's not valid
        {
            return new Triangle[]{};
        }
        if (insidePointCount == 1 && outsidePointCount == 2)
        {
            Intersection intersection1 = intersectPlane(pointOnPlane, planeNormal, insidePoints[0], outsidePoints[0]);
            Intersection intersection2 = intersectPlane(pointOnPlane, planeNormal, insidePoints[0], outsidePoints[1]);
            Triangle smallerTriangle = new Triangle(triangle);
            
            smallerTriangle.points[0] = insidePoints[0];
            smallerTriangle.textureCoordinates[0] = insideTextures[0];
            
            smallerTriangle.points[1] = intersection1.p;
            smallerTriangle.textureCoordinates[1].u = intersection1.t * (outsideTextures[0].u - insideTextures[0].u) + insideTextures[0].u;
            smallerTriangle.textureCoordinates[1].v = intersection1.t * (outsideTextures[0].v - insideTextures[0].v) + insideTextures[0].v;
            smallerTriangle.textureCoordinates[1].w = intersection1.t * (outsideTextures[0].w - insideTextures[0].w) + insideTextures[0].w;
            
            smallerTriangle.points[2] = intersection2.p;
            smallerTriangle.textureCoordinates[2].u = intersection2.t * (outsideTextures[1].u - insideTextures[0].u) + insideTextures[0].u;
            smallerTriangle.textureCoordinates[2].v = intersection2.t * (outsideTextures[1].v - insideTextures[0].v) + insideTextures[0].v;
            smallerTriangle.textureCoordinates[2].w = intersection2.t * (outsideTextures[1].w - insideTextures[0].w) + insideTextures[0].w;
            
            return new Triangle[]{smallerTriangle};
        }
        if (insidePointCount == 2 && outsidePointCount == 1)
        {
            Intersection intersection1 = intersectPlane(pointOnPlane, planeNormal, insidePoints[0], outsidePoints[0]);
            Intersection intersection2 = intersectPlane(pointOnPlane, planeNormal, insidePoints[1], outsidePoints[0]);
            Triangle[] smallerTriangles = new Triangle[2];
            smallerTriangles[0] = new Triangle(triangle);
            smallerTriangles[1] = new Triangle(triangle);
            
            smallerTriangles[0].points[0] = insidePoints[0];
            smallerTriangles[0].textureCoordinates[0] = insideTextures[0];
            
            smallerTriangles[0].points[1] = insidePoints[1];
            smallerTriangles[0].textureCoordinates[1] = insideTextures[1];
            
            smallerTriangles[0].points[2] = intersection1.p;
            smallerTriangles[0].textureCoordinates[2].u = intersection1.t * (outsideTextures[0].u - insideTextures[0].u) + insideTextures[0].u;
            smallerTriangles[0].textureCoordinates[2].v = intersection1.t * (outsideTextures[0].v - insideTextures[0].v) + insideTextures[0].v;
            smallerTriangles[0].textureCoordinates[2].w = intersection1.t * (outsideTextures[0].w - insideTextures[0].w) + insideTextures[0].w;
            
            smallerTriangles[1].points[0] = insidePoints[1];
            smallerTriangles[1].textureCoordinates[0] = insideTextures[1];
            
            smallerTriangles[1].points[1] = smallerTriangles[0].points[2];
            smallerTriangles[1].textureCoordinates[1] = smallerTriangles[0].textureCoordinates[2];
            
            smallerTriangles[1].points[2] = intersection2.p;
            smallerTriangles[1].textureCoordinates[2].u = intersection2.t * (outsideTextures[0].u - insideTextures[1].u) + insideTextures[1].u;
            smallerTriangles[1].textureCoordinates[2].v = intersection2.t * (outsideTextures[0].v - insideTextures[1].v) + insideTextures[1].v;
            smallerTriangles[1].textureCoordinates[2].w = intersection2.t * (outsideTextures[0].w - insideTextures[1].w) + insideTextures[1].w;

//            smallerTriangles[0].color = Color.blue;
//            smallerTriangles[0].textured = false;
//
//            smallerTriangles[1].color = Color.red;
//            smallerTriangles[1].textured = false;
            
            return smallerTriangles;
        }
        else // Don't clip triangle, it's valid
        {
            return new Triangle[]{new Triangle(triangle)};
        }
    }
    
    public static boolean isInsideHyperrectangle(Vector point, Hyperrectangle rect)
    {
        Vector a = rect.vertices.get(0);
        Vector b = rect.vertices.get(3);
        Vector c = rect.vertices.get(1);
        Vector d = rect.vertices.get(4);
        
        Vector ap = subtract(a, point);
        Vector ab = subtract(a, b);
        Vector ac = subtract(a, c);
        Vector ad = subtract(a, d);
        
        return (0 < dot(ap, ab) && dot(ap, ab) < dot(ab, ab)) && (0 < dot(ap, ac) && dot(ap, ac) < dot(ac, ac)) && (0 < dot(ap, ad) && dot(ap, ad) < dot(ad, ad));
    }
    
    public static Vector add(Vector v1, Vector v2)
    {
        return new Vector(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }
    
    public static Vector subtract(Vector v1, Vector v2)
    {
        return new Vector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }
    
    public static Vector multiply(Vector v, float f)
    {
        return new Vector(v.x * f, v.y * f, v.z * f);
    }
    
    public static float dot(Vector v1, Vector v2)
    {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }
    
    public static float angleBetweenVectors(Vector v1, Vector v2)
    {
        return (float) (Math.acos(dot(v1, v2) / v1.getLength() / v2.getLength()) * 180.0f / Math.PI);
    }
    
    public static Vector cross(Vector v1, Vector v2)
    {
        return new Vector(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, v1.x * v2.y - v1.y * v2.x);
    }
    
    public static class Intersection
    {
        public final boolean intersection;
        public final float t;
        public final Vector p;
        
        public Intersection(boolean intersection, float t, Vector p)
        {
            this.intersection = intersection;
            this.t = t;
            this.p = p;
        }
    }
    
    public static float triangle2DArea(Vector2D p1, Vector2D p2, Vector2D p3)
    {
        return (p1.u * (p2.v - p3.v) + p2.u * (p3.v - p1.v) + p3.u * (p1.v - p2.v)) * 0.5f;
    }
}