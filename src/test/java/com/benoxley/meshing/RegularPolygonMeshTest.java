package com.benoxley.meshing;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.junit.Test;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

/**
 * Created by ben on 27/01/2016.
 */
public class RegularPolygonMeshTest {


    @Test
    public void testConstructor() {
        Polygon polygon = new Polygon(0, 0, 1, 1, 1, 0);
        RegularPolygonMesh mesh = new RegularPolygonMesh(polygon, 1);
        assertNotNull(mesh);
    }

    @Test
    public void testRender() {
        Polygon polygon = new Polygon(0, 0, 1, 0, 1, 1, 0, 1);
        RegularPolygonMesh mesh = new RegularPolygonMesh(polygon, 1);
        MeshView meshView = new MeshView(mesh);
    }

    @Test
    public void getSignTest(){
        Point2D pointA = new Point2D(0,0);
        Point2D pointB = new Point2D(100,0);
        Point2D pointC = new Point2D(0,10);
        Point2D pointD = new Point2D(0,-10);
        Point2D pointE = new Point2D(100,100);
        Point2D pointF = new Point2D(3000,-1000);
        Point2D pointG = new Point2D(3000,1000);
        assertFalse(RegularPolygonMesh.getSign(pointC,pointA,pointB)<0.0d);
        assertTrue(RegularPolygonMesh.getSign(pointD,pointA,pointB)<0.0d);
        assertFalse(RegularPolygonMesh.getSign(pointC,pointA,pointE)<0.0d);
        assertTrue(RegularPolygonMesh.getSign(pointD,pointA,pointE)<0.0d);
        assertFalse(RegularPolygonMesh.getSign(pointG,pointA,pointB)<0.0d);
        assertTrue(RegularPolygonMesh.getSign(pointF,pointA,pointB)<0.0d);
    }

    @Test
    public void pointInTriangleTest(){
        double[][] pointsInTriangle={{-20,255},{28,37},{187.5,147},{346,147}};
        double[][] pointsOutOfTriangle={{0,1},{0,0},{-1000,-1000},{28,35},{27,37},{29,36}};
        Point2D a = new Point2D(28,36);
        Point2D b = new Point2D(-127,300);
        Point2D c = new Point2D(347,147);
        for (int i = 0; i < pointsInTriangle.length; i++){
            Point2D d = new Point2D(pointsInTriangle[i][0],pointsInTriangle[i][1]);
            assertTrue("Point "+i+", "+d.toString()+" was outside the triangle",RegularPolygonMesh.pointInTriangle(d,a,b,c));
        }
        for (int i = 0; i < pointsOutOfTriangle.length; i++){
            Point2D d = new Point2D(pointsOutOfTriangle[i][0],pointsOutOfTriangle[i][1]);
            assertFalse("Point "+i+", "+d.toString()+" was inside the triangle",RegularPolygonMesh.pointInTriangle(d,a,b,c));
        }

    }

    @Test
    public void testShowRender() throws InterruptedException {
        JFXPanel panel = new JFXPanel();
        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        Platform.runLater(() -> {
            //Arrow
            Polygon polygon = new Polygon(0, 0, 100, 0, 100, 100, 50, 150, 0, 100);
            //Heart
            polygon = new Polygon(0, 0, -50,50,-100,50,-200,0,0,-200,200,0,100,50,50,50);
            //Complex polygon
            polygon = new Polygon(0,0,100,0,100,100,200,100,200,200,175,200,175,125,75,125,75,25,25,25,25,100,0,100);

            RegularPolygonMesh mesh = new RegularPolygonMesh(polygon, 100);
            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(new PhongMaterial(Color.RED));
            meshView.setCullFace(CullFace.BACK);
            meshView.setDepthTest(DepthTest.ENABLE);
            meshView.setDrawMode(DrawMode.LINE);

            Group group = new Group();
            SubScene subScene = new SubScene(group,500d,500d,true, SceneAntialiasing.BALANCED);
            group.setTranslateX(250);
            group.setTranslateY(250);
            //SubScene rootWindow = new SubScene(group,500,500);
            Scene scene = new Scene(group, 500, 500);
            Stage stage = new Stage();
            stage.setScene(scene);
            RotateTransition transition = new RotateTransition();
            transition.setNode(meshView);
            transition.setByAngle(360);
            transition.setAxis(new Point3D(1d, 1d, 0d));
            transition.setDuration(Duration.seconds(3));
            transition.setAutoReverse(false);
            transition.setCycleCount(Transition.INDEFINITE);
            group.setOnMouseClicked(e->{
                if (transition.getStatus().equals(Animation.Status.PAUSED)){
                    transition.play();
                } else {
                    transition.pause();
                }
            });
            group.getChildren().setAll(meshView);
            transition.play();
            stage.showAndWait();
            semaphore.release();
        });
        semaphore.acquire();
    }
}