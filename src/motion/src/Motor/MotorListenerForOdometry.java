package Motor;

import org.ros.message.MessageListener;

import MotorControl.RobotVelocityController;
import org.ros.message.rss_msgs.MotionMsg;
import org.ros.message.rss_msgs.OdometryMsg;

public class MotorListenerForOdometry implements MessageListener<OdometryMsg> {

    private RobotPositionController controller;

    public MotorListenerForOdometry(RobotPositionController rvc) {
        controller = rvc;
    }

    @Override
    public void onNewMessage(OdometryMsg msg) {

        // System.out.println("got velocity command: " + msg.translationalVelocity + ", " + msg.rotationalVelocity);
        //System.out.println("odometry listner received message");
        controller.setPose(msg.x, msg.y, msg.theta);
    }

}
