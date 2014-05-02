package orc;

import java.util.*;
import java.net.*;
import java.io.*;
import java.text.ParseException;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;
import org.ros.node.parameter.ParameterTree;
import rss_msgs.MotorVelMsg;
import rss_msgs.SimulatorMsg;
import map.PolygonMap;

public class Orc
{
    private static Orc instance = null;

    private ConnectedNode node;
    private Subscriber<SimulatorMsg> simSub;
    private Publisher<MotorVelMsg> motPub;

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

    public void update(SimulatorMsg msg) {
        this.x = msg.getX();
        this.y = msg.getY();
        this.theta = msg.getTheta();
        this.posLeft = msg.getPosLeft();
        this.posRight = msg.getPosRight();
        this.velLeft = msg.getVelLeft();
        this.velRight = msg.getVelRight();
        this.motorLeft = msg.getMotorLeft();
        this.motorRight = msg.getMotorRight();
        this.utime = msg.getUtime();
    }
    
    private Orc() {}
    
    public OrcStatus getStatus() {
        return new OrcStatus(this, map, x, y, theta);
    }

    public void setMotorVel(int port, double v) {
        MotorVelMsg msg = motPub.newMessage();
        msg.setPort(port);
        msg.setVel(v);
        motPub.publish(msg);
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

    public boolean isSim() {
        return true;
    }

    public void setNode(ConnectedNode node) {
        this.node = node;
        ParameterTree paramTree = node.getParameterTree();
        final String mapFile = paramTree.getString(node.resolveName("/loc/mapFileName"));
        try {
            map = new PolygonMap(mapFile);
        }
        catch (IOException e) {
            throw new RuntimeException("Orc sim couldn't load map file");
        }
        catch (ParseException e) {
            throw new RuntimeException("Orc sim couldn't load map file");
        }

        // Subscribe to simulator updates
        simSub = node.newSubscriber("/sim/Simulator", "rss_msgs/SimulatorMsg");
        simSub.addMessageListener(
            new MessageListener<SimulatorMsg>() {
                @Override public void onNewMessage(SimulatorMsg msg) {
                    Orc.this.update(msg);
                }
            });

        motPub = node.newPublisher("/sim/MotorVel", "rss_msgs/MotorVelMsg");
    }
}
