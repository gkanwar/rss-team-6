package Motor;

import org.ros.message.MessageListener;

import MotorControl.RobotVelocityController;
import org.ros.message.rss_msgs.MotionMsg;
import org.ros.message.rss_msgs.ReverseMsg;

public class MotorListenerForReverse implements MessageListener<ReverseMsg> {

    private RobotPositionController controller;

    public MotorListenerForReverse(RobotPositionController rvc) {
        controller = rvc;
    }

    @Override
    public void onNewMessage(ReverseMsg msg) {
        controller.setReverse(msg.reverse);
    }

}
