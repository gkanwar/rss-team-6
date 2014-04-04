package VisualServo;

import java.awt.Color;
import java.util.ArrayList;

/**
 * BlobTracking performs image processing and tracking for the VisualServo
 * module. BlobTracking filters raw pixels from an image and classifies blobs,
 * generating a higher-level vision image.
 * 
 * @author previous TA's, prentice
 */
public class BlobTracking {
    protected int stepCounter = 0;
    protected double lastStepTime = 0.0;

    public int width;
    public int height;

    // Variables used for velocity controller that are available to calling
    // process. Visual results are valid only if targetDetected==true; motor
    // velocities should do something sane in this case.
    public boolean targetDetected = false; // set in blobPresent()
    public double centroidX = 0.0; // set in blobPresent()
    public double centroidY = 0.0; // set in blobPresent()
    public double targetArea = 0.0; // set in blobPresent()
    public double targetRange = 0.0; // set in blobFix()
    public double targetBearing = 0.0; // set in blobFix()
	public int ballWidth = 0;
	public int ballHeight = 0;
	
	/**
	 * RADIUS_TO_RANGE divided by estimated ball radius on-screen
	 * gives the estimated ball range from camera.
	 * 
	 * Data: 80cm gave us 10px (this constant is part of an inversely
	 * proportional equation; see below)
	 */
	protected static final double RADIUS_TO_RANGE = 10.0 * .80;

    protected static final double RADIUS_TO_RANGE_LAB6 = 8.2857;
	
	/**
	 * SCREEN_X_TO_RADIANS multiplied by an x offset in screen pixels
	 * gives the angular position of the offset.
	 * 
	 * Data: An 11" (~= 28cm) sheet of paper filled the FOV (160px across) at 29cm.
	 * 
	 * Negated, since left on the screen (negative) is CCW, which is positive bearing.
	 */
	protected static final double SCREEN_X_TO_RADIANS = - Math.atan2(14.0, 29.0) / (160.0/2.0);

    /**
     * <p>
     * Create a BlobTracking object
     * </p>
     * 
     * @param width
     *            image width
     * @param height
     *            image height
     */
    public BlobTracking(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * <p>
     * Computes frame rate of vision processing
     * </p>
     */
    private void stepTiming() {
        double currTime = System.currentTimeMillis();
        stepCounter++;
        // if it's been a second, compute frames-per-second
        if (currTime - lastStepTime > 1000.0) {
            // double fps = (double) stepCounter * 1000.0
            // / (currTime - lastStepTime);
            // System.err.println("FPS: " + fps);
            stepCounter = 0;
            lastStepTime = currTime;
        }
    }

    /**
     * <p>
     * Segment out a blob from the src image (if a good candidate exists).
     * </p>
     * 
     * <p>
     * <code>dest</code> is a packed RGB image for a java image drawing routine.
     * If it's not null, the blob is highlighted.
     * </p>
     * 
     * @param src
     *            the source RGB image, not packed
     * @param dest
     *            the destination RGB image, packed, may be null
     */
    public void apply(Image src, Image dest) {

        stepTiming(); // monitors the frame rate

        // Begin Student Code
        boolean hsb = true;

        int h = src.getHeight();
        int w = src.getWidth();

        int ht = h / 10;
        int wt = w / 10;

        //int ht_start = h / 2 - ht / 2;
        //int wt_start = w / 2 - wt / 2;

		int ht_start = 0;
		int wt_start = 0;
		
		int[] colors = new int[3];
		
		// Determine the average rgb/hsb pixel values in the upper left hand corner
		for (int x=wt_start; x<wt_start + wt; x++) {
			for (int y=ht_start; y<ht_start + ht; y++) {
				int red = src.getPixelRed(x,y) & 0xff;
		 		int green = src.getPixelGreen(x,y) & 0xff;
		 		int blue = src.getPixelBlue(x,y) & 0xff;
		 		if (!hsb) {
		 			colors[0] += red;
		 			colors[1] += green;
		 			colors[2] += blue;
		 		}
		 		else {
		 			float[] hsbvals = new float[3];
    		 	    Color.RGBtoHSB(red, green, blue, hsbvals);
		 			colors[0] += (int) (hsbvals[0]*255);
		 			colors[1] += (int) (hsbvals[1]*255);
		 			colors[2] += (int) (hsbvals[2]*255);
		 		}
			}
		}

		colors[0] = colors[0] / (ht * wt);
		colors[1] = colors[1] / (ht * wt);
		colors[2] = colors[2] / (ht * wt);

		if (!hsb) {
		 	System.out.println("Upper left in RGB: " + colors[0] + ", " + colors[1] + ", " + colors[2]);
		}
		else {
			System.out.println("Upper left in HSB: " + colors[0] + ", " + colors[1] + ", " + colors[2]);
		}

		blobPresent(src, dest);
		// Comment out the next two lines to display the histogram overlayed over the src image
		//dest.setPixelArray(src.toArray());
		//Histogram.getHistogram(src, dest, hsb);
		//System.out.println("In apply!");
		// End Student Code
	}
	
	/**
	 * TODO
	 * @return
	 */
    public boolean blobPresent(Image src, Image dest) {
        int h = src.getHeight();
        int w = src.getWidth();

        // Create an image that contains red pixels substituted for pixels that may be part
        // of the object and averaged gray pixels for all other pixels
        classify(src, dest);

        // Create a 2D array that represents potential object pixels with -1 and all other
        // pixels with 0
	    int[][] basic_img = new int[h][w];	    
	    for(int x=0; x<w; x++) {
	    	for (int y=0; y<h; y++) {
	    		if((dest.getPixelRed(x,y) & 0xff) == 255){
	    			basic_img[y][x] = -1;
	    		}
	    		else {
	    			basic_img[y][x] = 0;
	    		}
	    	}
	    }
	    
/*	    for(int i=0; i<h; i++){
			for(int j=0; j<w; j++){
			    if(Image.byteToUnsigned(src.getPixelRed(j,i)) == 255){
			    	basic_img[i][j] = -1;
			    }
			    else{
			    	basic_img[i][j] = 0;
			    }
			}
	    }*/
	    
	    //System.out.println(basic_img[0] + " " + basic_img[1] + " " + basic_img[2]);
	    //System.out.println(basic_img[0] & 0x000000ff);

	    // Determine the connected components composed of pixels that may be part of the object
	    ConnectedComponents labeller = new ConnectedComponents();
	    ArrayList<Integer> blobSize = new ArrayList<Integer>();
	    blobSize = labeller.getComponents(basic_img, h, w);
	    
	    // Determine the size and label of the largest component (most likely to be the object)
	    int blobSize_max_size = 0;
	    int blobSize_max_label = 0;
	    for (int iter = 0; iter < blobSize.size(); iter++) {
	    	if (blobSize.get(iter) > blobSize_max_size) {
	    		blobSize_max_size = blobSize.get(iter);
	    		blobSize_max_label = iter;
	    	}
	    }
	    
	    // Use a threshold of the number of pixels (recall the entire screen is 160*120=19200 pixels)
	    int threshold = 50;
	    targetArea = blobSize_max_size;
		if (targetArea > threshold) {
			targetDetected = true;
		}
		else {
			targetDetected = false;
		}
	    // System.out.println("num label: " + blobSize.size() + " maxSize: " + blobSize_max_size + " maxLabel: " + blobSize_max_label);

		// Determine the x and y bounds and the centroid of the largest obstacle
		int minX = w;
		int maxX = 0;
		int minY = h;
		int maxY = 0;
		int sumX = 0;
		int sumY = 0;
		for (int x=0;x<w;x++) {
			for (int y=0;y<h;y++) {
				if (basic_img[y][x] == blobSize_max_label) {
					// If the obstacle is larger than the threshold, color its pixels green
					if(targetDetected) {
						dest.setPixel(x, y, (byte)0, (byte)255, (byte)0);
					}
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
					sumX += x;
					sumY += y;
				}
			}
		}
		ballWidth = maxX - minX;
		ballHeight = maxY - minY;
		double centroidXCornerRelative = (double)sumX/blobSize_max_size;
        double centroidYCornerRelative = (double)sumY/blobSize_max_size;
		centroidX = centroidXCornerRelative - w/2.0;
		centroidY = centroidYCornerRelative - h/2.0;
		
		if (targetDetected) {
			// Draw 20 pixel wide cross at the centroid of the object
			for(int i=Math.max(0,(int)centroidXCornerRelative-10); i<Math.min((int)centroidXCornerRelative+10, w-1); i++){
			    int pos = (int) centroidYCornerRelative;
			    if(pos < 0) pos = 0;
			    if(pos > h-1) pos = h-1;
				dest.setPixel(i, pos, (byte)0, (byte)0, (byte)255);
			}
			for(int i=Math.max(0,(int)centroidYCornerRelative-10); i<Math.min((int)centroidYCornerRelative+10, h-1); i++){
				int pos = (int) centroidXCornerRelative;
			    if(pos < 0) pos = 0;
			    if(pos > w - 1) pos = w-1;
				dest.setPixel(pos, i, (byte)0, (byte)0, (byte)255);
			}
	
			// Draw a box outlining the object
			for(int i=minX; i<maxX; i++){
			    dest.setPixel(i, minY, (byte)0, (byte)0, (byte)255);
			    dest.setPixel(i, maxY, (byte)0, (byte)0, (byte)255);
			}
			for(int i=minY; i<maxY; i++){
			    dest.setPixel(minX, i, (byte)0, (byte)0, (byte)255);
			    dest.setPixel(maxX, i, (byte)0, (byte)0, (byte)255);
			}
		}
		
	    return true;
	}

    public void classify(Image src, Image dest) {
        byte[] blurredPixels = new byte[width*height*3];
        int h = src.getHeight();
        int w = src.getWidth();
        
        // Create a filtered image by using a Gaussian filter
        GaussianBlur.apply(src.toArray(), blurredPixels, w, h);       
        Image blurredImage = new Image(blurredPixels, w, h); 
        
        // Use the filtered image to determine which pixels are part of the object
        for (int x = 0; x < w; x++) {
        	for(int y = 0; y < h; y++) {
            	int red = blurredImage.getPixelRed(x,y) & 0xff;
            	int green = blurredImage.getPixelGreen(x,y) & 0xff;
            	int blue = blurredImage.getPixelBlue(x,y) & 0xff;
            	
                //int red = Image.byteToUnsigned(src.getPixelRed(j,i));
                //int green = Image.byteToUnsigned(src.getPixelGreen(j,i));
                //int blue = Image.byteToUnsigned(src.getPixelBlue(j,i));

		    	if (isValidRedPixel(red, green, blue)) {
		    		dest.setPixel(x,y,(byte)255,(byte)0,(byte)0);
		    	}
		    	else {
		    		int avg = (red + green + blue)/3;
		    		dest.setPixel(x,y,(byte)avg,(byte)avg,(byte)avg);		    		
		    	}
		    }
		}
    }

    public boolean isValidRedPixel(int red, int green, int blue) {
		float[] hsbvals = new float[3];
	    hsbvals = Color.RGBtoHSB(red,green,blue,hsbvals);
	    int hue = (int) (255 * hsbvals[0]);
	    int sat = (int) (255 * hsbvals[1]);
	    int val = (int) (255 * hsbvals[2]);

	    // Green!
	    if (hue > 60 && hue < 100 && sat >= 200 && val >= 100 ) return true;

	    // GREEN?
	    //if (hue > 85 && hue < 110) return true;

	    //if (hue < 15 || hue > 240) return true;
	    
	    //if((hue < 10 || hue > 240) && sat >= 150 && sat <=180 && val >= 95 && val <=120)
	    //	return true;
	    //if(hue >= 140 && hue <= 160 && sat >= 190 && sat <=205 && val >=160 && val <=170)
	    //	return true; 
	    
	    //if (hue < 15 && sat >= 185 && sat <= 205 && val >= 165 && val <= 190) return true;
	    //if (hue > 120 && hue <= 140 && sat >= 140 && sat <= 160 && val >= 215 && val <= 235) return true;
	    
	    // if ((hue <= 15 || hue > 240) && sat >= 100 && val >= 10) return true;

	    return false;
    }
	
	/**
	 * Sets targetRange and targetBearing using outputs from blobPresent
	 */
	public void blobFix() {
	    //double estimatedRadius = Math.sqrt(targetArea / Math.PI);
	    //targetRange = RADIUS_TO_RANGE / (ballWidth/2.0);
	    targetRange = RADIUS_TO_RANGE_LAB6 / ballWidth;
	    //System.out.println("Got range of : " + targetRange);

	    targetBearing = SCREEN_X_TO_RADIANS * centroidX;
	}
}
