package VisualServo;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import org.ros.message.MessageListener;
import rss_msgs.MotionMsg;
import rss_msgs.BallLocationMsg;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

/**
 * 
 * @author previous TA's, prentice, vona
 * 
 */
public class VisualServo extends AbstractNodeMain implements Runnable {

    private static final int width = 160;

    private static final int height = 120;

    /**
     * <p>
     * The blob tracker.
     * </p>
     **/
    private BlobTrackingChallenge blobTrack = null;

    private VisionGUI gui;
    private ArrayBlockingQueue<byte[]> visionImage = new ArrayBlockingQueue<byte[]>(
            1);

    protected boolean firstUpdate = true;

    public Subscriber<sensor_msgs.Image> vidSub;
    public Subscriber<rss_msgs.OdometryMsg> odoSub;

    private Publisher<rss_msgs.BallLocationMsg> ballLocationPub;

    /**
     * <p>
     * Create a new VisualServo object.
     * </p>
     */
    public VisualServo() {

        setInitialParams();

        gui = new VisionGUI();
    }

    protected void setInitialParams() {

    }

    /**
     * <p>
     * Handle a CameraMessage. Perform blob tracking and servo robot towards
     * target.
     * </p>
     * 
     * @param rawImage
     *            a received camera message
     */
    public void handle(byte[] rawImage) {

        visionImage.offer(rawImage);
    }

    @Override
    public void run() {
        while (true) {
            Image src = null;
            try {
		//System.out.println("size: " + visionImage.take().length);
                src = new Image(visionImage.take(), width, height);
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            Image dest = new Image(src);

	    //System.out.println("blobtrack apply");
            blobTrack.apply(src, dest);

            // update newly formed vision message
            gui.setVisionImage(dest.toArray(), width, height);

	    /*	
            // Begin Student Code
            double range=1;
            double bearing=0;

	    if (blobTrack.targetDetected) {
                blobTrack.blobFix();
   
             range = blobTrack.targetRange;
                bearing = blobTrack.targetBearing;
            } else {
                // range=-1 is going to be our special output value to convey
                // "blob not found" in the
                // message we are publishing
                range = -1;
                bearing = 0;
            }
	    
            // publish velocity messages to move the robot towards the target
            BallLocationMsg msg = ballLocationPub.newMessage();
            msg.setRange(range);
            msg.setBearing(bearing);
	    //System.out.println("Publishing ballLocationMsg");
            ballLocationPub.publish(msg);
            // End Student Code
	    */
        }
    }

    /**
     * <p>
     * Run the VisualServo process
     * </p>
     * 
     * @param node
     *            optional command-line argument containing hostname
     */
    @Override
    public void onStart(final ConnectedNode node) {
        blobTrack = new BlobTrackingChallenge(width, height);

        // Begin Student Code

        // set parameters on blobTrack as you desire

        // initialize the ROS publication to command/MotorsBallLocation
        ballLocationPub = node.newPublisher("/command/MotorsBallLocation",
                "rss_msgs/BallLocationMsg");

        // End Student Code

        final boolean reverseRGB = node.getParameterTree().getBoolean(
                "reverse_rgb", false);

        vidSub = node.newSubscriber("/rss/video", "sensor_msgs/Image");
	        vidSub.addMessageListener(new MessageListener<sensor_msgs.Image>() {
            @Override
            public void onNewMessage(sensor_msgs.Image message) {
                byte[] rgbData;
                if (reverseRGB) {
                    rgbData = Image.RGB2BGR(message.getData().array(), (int) message.getWidth(),
                                            (int) message.getHeight());
                } else {
                    rgbData = message.getData().array();
                }
                assert ((int) message.getWidth() == width);
                assert ((int) message.getHeight() == height);
		if ((int) message.getWidth() != width) {
		    throw new RuntimeException ("Widths don't match: " + message.getWidth() + "," + width);
		}
		if ((int) message.getHeight() != height) {
		    throw new RuntimeException ("Heights don't match: " + message.getHeight() + "," + height);
		}
		if (rgbData.length != 3 * width * height) {
		    /*
		    throw new RuntimeException ("Extra data received: " + rgbData.length + "," + 3*width*height);
		    */
		    // Strip the first n characters to make the length right (yay hacks! P.S. don't let tej see this code)
		    byte[] rgbDataNew = Arrays.copyOfRange(rgbData, rgbData.length - 3*width*height, rgbData.length);
		    rgbData = rgbDataNew;
		}
		//System.out.println("rgbData length: " + rgbData.length); 
                handle(rgbData);
            }
        });
	
        odoSub = node.newSubscriber("/rss/odometry", "rss_msgs/OdometryMsg");
        odoSub.addMessageListener(new MessageListener<rss_msgs.OdometryMsg>() {
            @Override
            public void onNewMessage(
                    rss_msgs.OdometryMsg message) {
                if (firstUpdate) {
                    firstUpdate = false;
                    gui.resetWorldToView(message.getX(), message.getY());
                }
                gui.setRobotPose(message.getX(), message.getY(), message.getTheta());
            }
        });
        Thread runningStuff = new Thread(this);
        runningStuff.start();
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rss/visualservo");
    }
}
