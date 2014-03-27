package Motor;

import org.ros.message.MessageListener;

import MotorControl.RobotVelocityController;
import org.ros.message.rss_msgs.MotionMsg;
import org.ros.message.rss_msgs.PositionTargetMsg;

public class MotorListenerForPositionControl implements MessageListener<PositionTargetMsg> {

    private RobotPositionController controller;

    public MotorListenerForPositionControl(RobotPositionController rvc) {
        controller = rvc;
    }

    @Override
    public void onNewMessage(PositionTargetMsg msg) {

        System.out.println("got pos command: " + msg.x + ", " + msg.y);
        System.out.println("listner received message");
        controller.setGoal(msg.x, msg.y, msg.theta);
    }

}
