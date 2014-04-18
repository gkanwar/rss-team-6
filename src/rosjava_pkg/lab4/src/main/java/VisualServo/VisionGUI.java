package VisualServo;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import org.ros.message.MessageListener;
import rss_msgs.MotionMsg;
import rss_msgs.OdometryMsg;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;


public class VisionGUI extends JPanel implements NodeMain {

        //Publisher<MotionMsg> publisher;

    /**
     * <p>
     * Node for ROS communication
     * </p>
     */
    public Node node;

    /**
     * <p>
     * The application name.
     * </p>
     **/
    public static final String APPNAME = "VisionGUI";

    /**
     * <p>
     * Needed as JPanel is serializable
     * </p>
     **/
    static final long serialVersionUID = 42;

    /**
     * <p>
     * Default width.
     * </p>
     **/
    public static final int DEFAULT_WIDTH = 800;

    /**
     * <p>
     * Default canvas height.
     * </p>
     **/
    public static final int DEFAULT_HEIGHT = DEFAULT_WIDTH;

    /**
     * <p>
     * Whether to render double-buffered.
     * </p>
     **/
    public static final boolean DOUBLE_BUFFERED = true;

    /**
     * <p>
     * Whether to use <code>RenderingHints.VALUE_ANTIALIAS_ON</code> and
     * <code>RenderingHints.VALUE_TEXT_ANTIALIAS_ON</code>.
     * </p>
     **/
    public static final boolean ANTIALIASING = true;

    /**
     * <p>
     * The rendering quality to use.
     * </p>
     **/
    public static final Object RENDERING_QUALITY = RenderingHints.VALUE_RENDER_QUALITY;

    /**
     * <p>
     * The interpolation to use.
     * </p>
     **/
    public static final Object INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BICUBIC;

    /**
     * <p>
     * Whether to use <code>RenderingHints.VALUE_FRACTIONALMETRICS_ON</code>.
     * </p>
     **/
    public static final boolean FRACTIONALMETRICS = true;

    /**
     * <p>
     * Whether to use <code>RenderingHints.VALUE_STROKE_NORMALIZE</code>.
     * </p>
     **/
    public static final boolean STROKE_NORMALIZATION = true;



    protected boolean firstUpdate = true;

    /**
     * <p>
     * Whether to sacrifice rendering quality for speed.
     * </p>
     **/
    protected boolean renderFastest = false;


    /**
     * <p>
     * Whether to paint the vision image.
     * </p>
     **/
    protected boolean visionImageEnabled = true;

    /**
     * <p>
     * The frame containing this GUI.
     * </p>
     **/
    protected JFrame frame;

    /**
     * <p>
     * Total time it took in milliseconds to render the last frame.
     * </p>
     **/
    protected double lastFrameTime = 0.0;

    /**
     * <p>
     * Frame time in ms above which to force fast rendering.
     * </p>
     **/
    public static final double FORCE_FAST_RENDER_THRESHOLD = 300.0;

    /**
     * <p>
     * Frame time in ms below which to un-force fast rendering.
     * </p>
     **/
    public static final double UN_FORCE_FAST_RENDER_THRESHOLD = 5.0;

    public Subscriber<OdometryMsg> odoSub;
    public Subscriber<sensor_msgs.Image> vidSub;

    /**
     * <p>
     * A paintable graphical object.
     * </p>
     **/
    protected abstract class Glyph {

        /**
         * <p>
         * Paint this glyph.
         * </p>
         * 
         * @param g2d
         *            the graphics context
         **/
        public abstract void paint(Graphics2D g2d);
    }


    /**
     * <p>
     * Displays images from the robot's camera.
     * </p>
     **/
    protected class VisionImage extends Glyph {

        /**
         * <p>
         * The pixel buffer for the displayed image.
         * </p>
         **/
        int packedImage[] = null;

        /**
         * <p>
         * Java image animation machinery.
         * </p>
         **/
        MemoryImageSource source = null;

        /**
         * <p>
         * The actual image we paint, null if none.
         * </p>
         **/
        java.awt.Image image = null;

        /**
         * <p>
         * Currently displayed image width or -1 if none.
         * </p>
         **/
        int width = -1;

        /**
         * <p>
         * Currently displayed image height or -1 if none.
         * </p>
         **/
        int height = -1;

        /**
         * <p>
         * Create a new vision image, initially not displaying anything.
         * </p>
         **/
        VisionImage() {
            unset();
        }

        /**
         * <p>
         * Set to display an image.
         * </p>
         * 
         * @param unpackedImage
         *            the unpacked RGB image (a copy is made)
         * @param width
         *            image width
         * @param height
         *            image height
         **/
        void set(byte[] unpackedImage, int width, int height) {
            if ((unpackedImage == null) || (width <= 0) || (height <= 0)) {
                unset();
                return;
            }

            boolean reConsedPacked = false;
            if ((packedImage == null) || (this.width != width)
                    || (this.height != height)) {
                packedImage = new int[width * height];
                reConsedPacked = true;
            }

            int srcIndex = 0;
            int destIndex = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int red = unpackedImage[srcIndex++] & 0xff;
                    int green = unpackedImage[srcIndex++] & 0xff;
                    int blue = unpackedImage[srcIndex++] & 0xff;
                    packedImage[destIndex++] = (0xff << 24) | (red << 16)
                            | (green << 8) | blue;
                }
            }

            if (reConsedPacked || (image == null) || (this.width != width)
                    || (this.height != height)) {

                source = new MemoryImageSource(width, height, packedImage, 0,
                        width);
                source.setAnimated(true);

                image = createImage(source);

            } else {
                source.newPixels();
            }

            this.width = width;
            this.height = height;
        }

        /**
         * <p>
         * Disable display.
         * </p>
         **/
        void unset() {
            image = null;
            width = -1;
            height = -1;
        }

        /**
         * <p>
         * Paints the image, if any.
         * </p>
         * 
         * <p>
         * Assumes g2d is in view coordinates.
         * </p>
         * 
         * @param g2d
         *            the graphics context
         **/
        @Override
        public void paint(Graphics2D g2d) {
            if (image == null)
                return;

            g2d.drawImage(image, 0, 0, VisionGUI.this);
        }
    }

    /**
     * <p>
     * The one {@link VisionGUI.VisionImage}.
     * </p>
     **/
    protected VisionImage visionImage = new VisionImage();


    /**
     * <p>
     * Consruct a new VisionGUI.
     * </p>
     * 
     * <p>
     * The new viewer will automatically appear in a new window.
     * </p>
     * 
     * @param poseSaveInterval
     *            the number of robot pose updates to skip between saving a
     *            persistent snapshot of the pose. Zero indicates that every
     *            pose should be saved. Negative indicates that <i>no</i> poses
     *            should be saved.
     * @param maxTV
     *            max translation velocity in m/s. Setting this less than or
     *            equal to 0.0 disables driving. See also {@link #setMaxTV}.
     * @param maxRV
     *            max translation velocity in m/s. Setting this less than or
     *            equal to 0.0 disables driving. See also {@link #setMaxRV}.
     **/
    public VisionGUI() {

        // do this here, not in invokeLater, so we can configure frame in
        // instanceMain()
        frame = new JFrame(getAppName());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                setBackground(Color.WHITE);
                setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
                setOpaque(true);
                setDoubleBuffered(true);

                Container contentPane = frame.getContentPane();
                contentPane.setBackground(Color.WHITE);
                contentPane.setLayout(new BorderLayout());
                contentPane.add(VisionGUI.this, "Center");

                frame.pack(); // do this before working on focus!

                setFocusable(true);
                requestFocusInWindow();

                frame.setLocationByPlatform(true);
                frame.setVisible(true);
            }
        });
    }

    /**
     * <p>
     * Get the frame containing this GUI.
     * </p>
     * 
     * @return the frame containing this GUI
     **/
    public JFrame getFrame() {
        return frame;
    }

    /**
     * <p>
     * Get the title for the GUI frame.
     * </p>
     * 
     * <p>
     * Default impl returns {@link #APPNAME}.
     * </p>
     * 
     * @return the title for the GUI frame
     **/
    public String getAppName() {
        return APPNAME;
    }

    /**
     * <p>
     * Set the vision image for display.
     * </p>
     * 
     * <p>
     * Note: if you are running the GUI in stand-alone mode, this will be
     * automatically called when Carmen vision image messages are recieved.
     * </p>
     * 
     * @param image
     *            is an unpacked <code>width</code> by <code>height</code> RGB
     *            image
     * @param width
     *            the image width
     * @param height
     *            the image height
     **/
    public void setVisionImage(byte[] image, int width, int height) {
        synchronized (visionImage) {
            visionImage.set(image, width, height);
        }
        repaint();
    }

    /**
     * <p>
     * Erase the vision image, if any.
     * </p>
     **/
    public void eraseVisionImage() {
        synchronized (visionImage) {
            visionImage.unset();
        }
    }

    /**
     * <p>
     * Calls {@link #paintComponent(Graphics2D)}.
     * </p>
     * 
     * @param g
     *            the paint context
     **/
    @Override
    public void paintComponent(Graphics g) {
        paintComponent((Graphics2D) g);
    }

    /**
     * <p>
     * Calls superclass impl, then {@link #paintContents}.
     * </p>
     **/
    protected void paintComponent(Graphics2D g2d) {
        super.paintComponent(g2d);
        paintContents(g2d);
    }

    /**
     * <p>
     * Paint all the graphics.
     * </p>
     * 
     * @param g2d
     *            the paint context
     **/
    protected void paintContents(Graphics2D g2d) {
        double startTime = System.currentTimeMillis();

        if (lastFrameTime > FORCE_FAST_RENDER_THRESHOLD)
            renderFastest = true;

        if (lastFrameTime < UN_FORCE_FAST_RENDER_THRESHOLD)
            renderFastest = false;

        if (renderFastest) {

            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

            g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                    RenderingHints.VALUE_STROKE_PURE);
        } else {

            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RENDERING_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    INTERPOLATION);

            if (ANTIALIASING) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }

            if (FRACTIONALMETRICS)
                g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                        RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            if (STROKE_NORMALIZATION)
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                        RenderingHints.VALUE_STROKE_NORMALIZE);

        }

        if (visionImageEnabled)
            paintVisionImage(g2d);

        lastFrameTime = System.currentTimeMillis() - startTime;

        // System.err.println("frame time = " + lastFrameTime);
    }

    /**
     * <p>
     * Paint {@link #visionImage}.
     * </p>
     * 
     * @param g2d
     *            the graphics context
     **/
    protected void paintVisionImage(Graphics2D g2d) {

        // avoid NPE on init
        if (visionImage == null)
            return;

        synchronized (visionImage) {
            visionImage.paint(g2d);
        }
    }



    /**
     * <p>
     * Extends default impl to set white background.
     * </p>
     **/
    @Override
    public JToolTip createToolTip() {
        JToolTip toolTip = super.createToolTip();
        toolTip.setBackground(Color.WHITE);
        return toolTip;
    }


    /**
     * <p>
     * See {@link #instanceMain}.
     * </p>
     **/
    @Override
    public void onStart(final ConnectedNode node) {
        this.node = node;

        final boolean reverseRGB = node.getParameterTree().getBoolean(
                "reverse_rgb", false);

        vidSub = node.newSubscriber("/rss/low_video", "sensor_msgs/Image");
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
                setVisionImage(rgbData, (int) message.getWidth(),
                               (int) message.getHeight());
            }
        });

        //publisher = node.newPublisher("command/Motors", "rss_msgs/MotionMsg");
    }

    /**
     * <p>
     * Draw some things to test the graphics capabilities.
     * </p>
     **/
    protected void testGraphics() {

        try {

            // for (int i = 0; i < 5000; i++)
            // setRobotPose(Math.random(), Math.random(),
            // (Math.random()-0.5)*2.0*Math.PI);

            byte[] testImage = new byte[256 * 256 * 3];

            int index = 0;
            for (int r = 0; r < 256; r++) {
                for (int c = 0; c < 256; c++) {
                    byte val = (byte) c;
                    testImage[index++] = val;
                    testImage[index++] = val;
                    testImage[index++] = val;
                }
            }

            setVisionImage(testImage, 256, 256);

            Thread.sleep(1000);

            setVisionImage(testImage, 100, 100);

            Thread.sleep(1000);

            eraseVisionImage();

            testGraphicsHook();

            for (;;)
                Thread.sleep(1000);

        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * <p>
     * Hook to append to the end of {@link #testGraphics}.
     * </p>
     **/
    protected void testGraphicsHook() throws InterruptedException {
    }

    @Override
    public void onShutdown(Node arg0) {
        if (node != null) {
            node.shutdown();
        }
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable error) {
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("rss/visiongui");
    }

}
