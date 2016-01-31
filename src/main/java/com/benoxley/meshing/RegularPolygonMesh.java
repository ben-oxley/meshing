package com.benoxley.meshing;


import javafx.geometry.Point2D;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.TriangleMesh;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a suitable mesh for the
 * JavaFX {@link javafx.scene.shape.MeshView} class from
 * {@link Polygon} objects.
 *
 * @author Ben Oxley
 * @version 1.0
 */
public class RegularPolygonMesh extends TriangleMesh{
    /**
     * Constants
     */
    private static final int ELEMENTS_PER_POINT = 3;
    private static final int ELEMENTS_PER_FACE = 6;
    private static final int NUMBER_OF_LAYERS = 2;
    private static final int INTERNAL_POINTS_PER_LAYER = 1;
    private static final int SIDES_PER_POINT = 2;

    /**
     * Constructor, takes a simple Polygon (no holes or intersections)
     * and a depth to extrude the shape.
     * @param polygon The polygon to render in 3D.
     * @param depth The depth in pixels to extrude the shape.
     */
    public RegularPolygonMesh(Polygon polygon, final double depth){
        this.getFaces().setAll(getListOfFaces(polygon));
        this.getPoints().setAll(getListOfPoints(polygon,depth));
        this.getTexCoords().setAll(0,0);
    }

    /**
     * This method renders all of the faces for the polygon.
     * @param polygon The polygon to render in 3D.
     * @return Returns an integer array of the faces in the format
     * required for the {@link TriangleMesh}.
     */
    private static int[] getListOfFaces(final Polygon polygon){
        final List<Double> points = polygon.getPoints();
        final int numNodes = points.size()/2;
        final int[] facesArray = new int[(NUMBER_OF_LAYERS+SIDES_PER_POINT)*numNodes*ELEMENTS_PER_FACE];
        int offset = 0;
        offset = pruneAndSearch(polygon,facesArray,offset);
        for (int i = 0; i< numNodes; i++){
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE] = i;
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+1] = 0; //texcoord
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+2] = i+numNodes; //point below
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+3] = 0; //texcoord
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+4] = ((i+1)%numNodes)+numNodes; //point to the right and below
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+5] = 0; //texcoord
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+6] = i; //point
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+7] = 0; //texcoord
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+8] = ((i+1)%numNodes)+numNodes; //point to the right and below
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+9] = 0; //texcoord
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+10] = (i+1)%numNodes; //next point
            facesArray[offset+i*SIDES_PER_POINT*ELEMENTS_PER_FACE+11] = 0; //texcoord
        }
        return facesArray;
    }

    /**
     * Method to get a list of points for the shape from the original polygon.
     * @param polygon The polygon to render in 3D.
     * @param depth The depth in pixels to extrude the shape.
     * @return Returns an array of points, defining the 3D point locations.
     */
    private static float[] getListOfPoints(final Polygon polygon, final double depth){
        final List<Double> points = polygon.getPoints();
        final int numNodes = points.size()/2;
        final float[] pointsArray = new float[(numNodes+INTERNAL_POINTS_PER_LAYER)*NUMBER_OF_LAYERS*ELEMENTS_PER_POINT];
        for (int i = 0; i < numNodes; i++){
            //Base layer points
            pointsArray[i*3] = points.get(i*2).floatValue();
            pointsArray[i*3+1] = points.get(i*2+1).floatValue();
            pointsArray[i*3+2] = 0;
        }
        for (int i = 0; i < numNodes; i++){
            //Top layer points
            pointsArray[(i+numNodes)*ELEMENTS_PER_POINT] = points.get(i*2).floatValue();
            pointsArray[(i+numNodes)*ELEMENTS_PER_POINT+1] = points.get(i*2+1).floatValue();
            pointsArray[(i+numNodes)*ELEMENTS_PER_POINT+2] = (float)depth;
        }
        final Point2D internalPoints = getAveragePosition(polygon);
        pointsArray[NUMBER_OF_LAYERS*numNodes*ELEMENTS_PER_POINT+0] = (float)internalPoints.getX();
        pointsArray[NUMBER_OF_LAYERS*numNodes*ELEMENTS_PER_POINT+1] = (float)internalPoints.getY();
        pointsArray[NUMBER_OF_LAYERS*numNodes*ELEMENTS_PER_POINT+2] = 0f;
        pointsArray[NUMBER_OF_LAYERS*numNodes*ELEMENTS_PER_POINT+3] = (float)internalPoints.getX();
        pointsArray[NUMBER_OF_LAYERS*numNodes*ELEMENTS_PER_POINT+4] = (float)internalPoints.getY();
        pointsArray[NUMBER_OF_LAYERS*numNodes*ELEMENTS_PER_POINT+5] = (float)depth;
        return pointsArray;
    }

    /**
     * Method to get the average position of all points, used for the simple mesher.
     * @param polygon The polygon to render in 3D.
     * @return Returns a point location, representing the average location of all points
     * in the polygon.
     */
    private static Point2D getAveragePosition(final Polygon polygon){
        final List<Double> points = polygon.getPoints();
        final int numberOfNodes = points.size()/2;
        double avgX = 0d;
        double avgY = 0d;
        for (int i = 0; i < numberOfNodes; i++){
            avgX += points.get(i*2);
            avgY += points.get(i*2+1);
        }
        return new Point2D(avgX/(double)numberOfNodes,avgY/(double)numberOfNodes);
    }

    /**
     * Method to create faces based on adding a central node to the polygon and drawing
     * faces from pairs of edge nodes to this center. Works for simple, regular polygons
     * but not much else
     * @param polygon The polygon to render in 3D.
     * @param facesArray The array of faces to add to.
     * @param offset The initial offset of the array to add to this method will not add face
     *               information before this point in the facesArray.
     * @return Returns the new offset from the start of the facesArray that points have been added up to.
     */
    @Deprecated
    private static int createFacesFromCentralNode(Polygon polygon, int[] facesArray, int offset){
        final List<Double> points = polygon.getPoints();
        final int numNodes = points.size()/2;
        //construct baselayer faces
        for (int i = 0; i< numNodes; i++){
            facesArray[offset+i*ELEMENTS_PER_FACE] = i; //point
            facesArray[offset+i*ELEMENTS_PER_FACE+1] = 0; //texcoord
            facesArray[offset+i*ELEMENTS_PER_FACE+2] = (i+1)%numNodes; //next point
            facesArray[offset+i*ELEMENTS_PER_FACE+3] = 0; //texcoord
            facesArray[offset+i*ELEMENTS_PER_FACE+4] = numNodes*2; //middle point, bottom layer
            facesArray[offset+i*ELEMENTS_PER_FACE+5] = 0; //texcoord
        }
        //construct top layer faces
        offset = offset + numNodes*ELEMENTS_PER_FACE;
        for (int i = 0; i< numNodes; i++){
            facesArray[offset+i*ELEMENTS_PER_FACE] = i+numNodes; //point
            facesArray[offset+i*ELEMENTS_PER_FACE+1] = 0; //texcoord
            facesArray[offset+i*ELEMENTS_PER_FACE+2] = numNodes*2+1; //middle point, top layer
            facesArray[offset+i*ELEMENTS_PER_FACE+3] = 0; //texcoord
            facesArray[offset+i*ELEMENTS_PER_FACE+4] = ((i+1)%numNodes)+numNodes; //next point
            facesArray[offset+i*ELEMENTS_PER_FACE+5] = 0; //texcoord
        }
        offset = numNodes*ELEMENTS_PER_FACE*2;
        return offset;
    }

    /**
     * Implementation of a prune and search method of finding and pruing ears from a simple polygon
     * in order to triangulate the polygon surface.
     * @param polygon The polygon to render in 3D.
     * @param facesArray The arrays to add face data to.
     * @param offset The initial offset of the array to add to this method will not add face
     *               information before this point in the facesArray.
     * @return Returns the new offset from the start of the facesArray that points have been added up to.
     */
    private static int pruneAndSearch(Polygon polygon, int[] facesArray, int offset){
        List<Double> originalPoints = new ArrayList<>(polygon.getPoints());
        List<Integer> remainingPoints = new ArrayList<>();
        final int numNodes = originalPoints.size()/2;
        for (int i = 0; i < originalPoints.size()/2; i++){
            remainingPoints.add(i);
        }
        while (remainingPoints.size()>2){

            //find an ear
            for (int i = 0; i < remainingPoints.size(); i++){
                final int numRemainingNodes = remainingPoints.size();
                Point2D point1 = new Point2D(originalPoints.get(2*remainingPoints.get(i)),originalPoints.get(2*remainingPoints.get(i)+1));
                Point2D point2 = new Point2D(originalPoints.get(2*remainingPoints.get((i+1)%numRemainingNodes)),originalPoints.get(2*remainingPoints.get((i+1)%numRemainingNodes)+1));
                Point2D point3 = new Point2D(originalPoints.get(2*remainingPoints.get((i+2)%numRemainingNodes)),originalPoints.get(2*remainingPoints.get((i+2)%numRemainingNodes)+1));
                //TODO - Currently this only works for anti-clockwise points, we can do a check at the start to invert
                // this statement if the points are clockwise.
                if (getSign(point2,point1,point3)<0.0d) {
                    boolean containsPoint = false;
                    for (int j = 0; j < remainingPoints.size(); j++) {
                        if (j != i && j != (i+1)%numRemainingNodes && j != (i+2)%numRemainingNodes) {
                            Point2D pointCheck = new Point2D(originalPoints.get(2 * remainingPoints.get(j)), originalPoints.get(2 * remainingPoints.get(j) + 1));
                            if (pointInTriangle(pointCheck, point1, point2, point3)) {
                                containsPoint = true;
                                break;
                            }
                        }
                    }
                    if (containsPoint == false) {
                        //We've found an ear
                        facesArray[offset] = remainingPoints.get(i);
                        facesArray[offset + 1] = 0; //texcoord
                        facesArray[offset + 2] = remainingPoints.get((i + 1) % numRemainingNodes);
                        facesArray[offset + 3] = 0; //texcoord
                        facesArray[offset + 4] = remainingPoints.get((i + 2) % numRemainingNodes);
                        facesArray[offset + 5] = 0; //texcoord
                        facesArray[offset + 6] = remainingPoints.get(i) + numNodes;
                        facesArray[offset + 7] = 0; //texcoord
                        facesArray[offset + 8] = remainingPoints.get((i + 2) % numRemainingNodes) + numNodes;
                        facesArray[offset + 9] = 0; //texcoord
                        facesArray[offset + 10] = remainingPoints.get((i + 1) % numRemainingNodes) + numNodes;
                        facesArray[offset + 11] = 0; //texcoord
                        //remove the central point
                        remainingPoints.remove((i + 1) % numRemainingNodes);
                        offset += 12;
                    }
                }
            }
        }
        return offset;
    }

    /**
     * Get the sign of the three points. If p1 is to the left of a line between p2 and p3
     * this will return negative.
     * @param p1 Point 1
     * @param p2 Point 2
     * @param p3 Point 3
     * @return Returns a negative number if p1 is to the left of a line intersecting p2 and
     * p3, positive if it is to the right of the line.
     */
    public static double getSign(Point2D p1, Point2D p2, Point2D p3)
    {
        return (p1.getX() - p3.getX()) * (p2.getY() - p3.getY()) - (p2.getX() - p3.getX()) * (p1.getY() - p3.getY());
    }

    /**
     * Checks if a point is within a triangle defined by three points.
     * @param pt The point to check
     * @param v1 Point 1 of the triangle
     * @param v2 Point 2 of the triangle
     * @param v3 Point 3 of the triangle
     * @return Returns true if the point pt is in the triangle, false if not or if the point
     * is on the edge of the triangle.
     */
    public static boolean pointInTriangle(Point2D pt, Point2D v1, Point2D v2, Point2D v3)
    {
        boolean b1, b2, b3;

        b1 = getSign(pt, v1, v2) < 0.0d;
        b2 = getSign(pt, v2, v3) < 0.0d;
        b3 = getSign(pt, v3, v1) < 0.0d;

        return ((b1 == b2) && (b2 == b3));
    }
}