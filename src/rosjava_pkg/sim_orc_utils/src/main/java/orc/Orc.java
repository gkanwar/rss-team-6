package orc;

import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.geom.Point2D;
import map.PolygonMap;
import odometry.Odometry;
import org.ros.node.parameter.ParameterTree;

public class Orc
{
    private static final double MAX_MOTOR_ANGVEL = 8.0;

    private static Orc instance = null;

    private PolygonMap map;
    private double x, y, theta;
    private double motorLeft, motorRight;
    private int posLeft = 0, posRight = 0;
    private double velLeft = 0, velRight = 0;
    private long utime;
    
    public static Orc makeOrc() {
        if (instance == null) {
            instance = new Orc();
        }
        return instance;
    }
    
    private Orc() {
        //ParameterTree paramTree = node.getParameterTree();
        //final String mapFile = paramTree.getString(node.resolveName("/loc/mapFileName"));
        final String mapFile = "/home/gurtej/rss-team-6/src/rosjava_pkg/maps/global-nav-maze-2011-basic.map";
        try {
            map = new PolygonMap(mapFile);
        }
        catch (Exception e) {
            throw new RuntimeException("Map file failed to load.");
        }
        Point2D.Double robotStart = map.getRobotStart();
        x = robotStart.x;
        y = robotStart.y;
        theta = 0;
        utime = System.nanoTime()/1000;

        // Fire up physics updates
        new PhysicsThread().start();
    }

    private class PhysicsThread extends Thread {
        @Override
        public void run() {
            while (true) {
                long deltaUTime = System.nanoTime()/1000 - utime;
                double deltaTime = deltaUTime/1000000.0; // Convert to seconds
                //System.out.println("Physics update: " + deltaTime);
                System.out.println("Motors: " + motorLeft + "," + motorRight);
                // Update "board" time
                utime += deltaUTime;
                // Physics
                double leftAngVel = MAX_MOTOR_ANGVEL * motorLeft;
                double leftLinear = leftAngVel * Odometry.WHEEL_RADIUS_IN_M;
                double rightAngVel = MAX_MOTOR_ANGVEL * motorRight * -1; // Right motor is flipped
                double rightLinear = rightAngVel * Odometry.WHEEL_RADIUS_IN_M;
                double transVel = (leftLinear+rightLinear)/2.0;
                double rotVel = (leftLinear-rightLinear)/Odometry.WHEELBASE;
                System.out.println("Transvel: " + transVel + ", rotvel: " + rotVel);
                // Just do trans then rot
                double dist = transVel * deltaTime;
                x += dist * Math.cos(theta);
                y += dist * Math.sin(theta);
                theta += rotVel * deltaTime;
                // Normalize theta
                theta = theta % (Math.PI*2);
                if (theta < 0) theta += Math.PI*2;
                System.out.println("x:" + x + ",y:" + y + ",theta:" + theta);
                // Now update orc "board" encoder info
                velLeft = leftAngVel * Odometry.TICKS_PER_REVOLUTION / (Math.PI * 2);
                velRight = rightAngVel * Odometry.TICKS_PER_REVOLUTION / (Math.PI * 2);
                posLeft += (int)(deltaTime * velLeft);
                posRight += (int)(deltaTime * velRight);

                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {}
            }
        }
    }
    
    public OrcStatus getStatus() {
        return new OrcStatus(this, map, x, y, theta);
    }

    public void setMotorVel(int port, double v) {
        if (port == 0) {
            motorLeft = v;
        }
        else {
            motorRight = v;
        }
    }

    public double getMotorVel(int port) {
        if (port == 0) {
            return motorLeft;
        }
        else {
            return motorRight;
        }
    }

    public int getEncoderPos(int port) {
        if (port == 0) {
            return posLeft;
        }
        else {
            return posRight;
        }
    }

    public double getEncoderVel(int port) {
        if (port == 0) {
            return velLeft;
        }
        else {
            return velRight;
        }
    }

    public long getUTime() {
        return utime;
    }
}
