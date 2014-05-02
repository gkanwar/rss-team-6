package orc;

import java.awt.geom.Point2D;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.parameter.ParameterTree;
import org.ros.node.topic.Subscriber;
import org.ros.node.topic.Publisher;

import rss_msgs.MotorVelMsg;
import rss_msgs.SimulatorMsg;

import map.PolygonMap;
import odometry.Odometry;

public class OrcNode extends AbstractNodeMain {

    private static final double MAX_MOTOR_ANGVEL = 5.0;

    private Subscriber<MotorVelMsg> motorVelSub;
    private Publisher<SimulatorMsg> simPub;

    private PolygonMap map;
    private double x, y, theta;
    private double motorLeft, motorRight;
    private int posLeft = 0, posRight = 0;
    private double velLeft = 0, velRight = 0;
    private long utime;

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
                double rightAngVel = MAX_MOTOR_ANGVEL * motorRight;
                double rightLinear = rightAngVel * Odometry.WHEEL_RADIUS_IN_M * -1; // Right motor is flipped
                double transVel = (rightLinear+leftLinear)/2.0;
                double rotVel = (rightLinear-leftLinear)/Odometry.WHEELBASE;
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

                // Publish all info
                SimulatorMsg msg = simPub.newMessage();
                msg.setX(x);
                msg.setY(y);
                msg.setTheta(theta);
                msg.setPosLeft(posLeft);
                msg.setPosRight(posRight);
                msg.setVelLeft(velLeft);
                msg.setVelRight(velRight);
                msg.setMotorLeft(motorLeft);
                msg.setMotorRight(motorRight);
                msg.setUtime(utime);
                simPub.publish(msg);

                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {}
            }
        }
    }

    @Override
    public void onStart(final ConnectedNode node) {
        ParameterTree paramTree = node.getParameterTree();
        final String mapFile = paramTree.getString(node.resolveName("/loc/mapFileName"));
        //final String mapFile = "/home/gurtej/rss-team-6/src/rosjava_pkg/maps/global-nav-maze-2011-basic.map";
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

        // Subscribers
        motorVelSub = node.newSubscriber("/sim/MotorVel", "rss_msgs/MotorVelMsg");
        motorVelSub.addMessageListener(
            new MessageListener<MotorVelMsg>() {
                @Override public void onNewMessage(MotorVelMsg msg) {
                    int port = msg.getPort();
                    if (port == 0) {
                        motorLeft = msg.getVel();
                    }
                    else {
                        motorRight = msg.getVel();
                    }
                }
            });

        // Publishers
        simPub = node.newPublisher("/sim/Simulator", "rss_msgs/SimulatorMsg");

        // Fire up physics updates
        new PhysicsThread().start();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rss/sim_orc");
    }
}
