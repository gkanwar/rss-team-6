package VisualServo;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.lang.time.StopWatch;

/**
 * BlobTracking performs image processing and tracking for the VisualServo
 * module. BlobTracking filters raw pixels from an image and classifies blobs,
 * generating a higher-level vision image.
 * 
 * @author previous TA's, prentice
 */
public class BlobTrackingChallenge {
	protected int stepCounter = 0;
	protected double lastStepTime = 0.0;

	public int width;
	public int height;
	public boolean serialize;

	public boolean targetDetected = false;
    PrintWriter out;
    StopWatch watch;
    FileOutputStream fileOut;
    ObjectOutputStream outStream;
    List<Image> capturedImages;

	public BlobTrackingChallenge(int width, int height, boolean serialize) {
		this.width = width;
		this.height = height;
		this.serialize = serialize;
		if (serialize) {
			try {
			    out = new PrintWriter("/home/rss-student/rss-challenge/rss-team-6/src/rosjava_pkg/lab4/snapshots/imageObjects.txt");
			    fileOut = new FileOutputStream("/home/rss-student/rss-challenge/rss-team-6/src/rosjava_pkg/lab4/snapshots/imageObjects.ser");
			    outStream = new ObjectOutputStream(fileOut);
			}
			catch (IOException e) {
			}
			watch = new StopWatch();
			watch.start();
		}
		capturedImages = new ArrayList<Image>();
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

	public void apply(Image src, Image dest) {
		// stepTiming(); // monitors the frame rate

		int h = src.getHeight();
		int w = src.getWidth();
		int ht = h / 10;
		int wt = w / 10;
		int ht_start = 0;
		int wt_start = 0;

		double hueSum = 0;

		// Determine the average rgb/hsb pixel values in the upper left hand
		// corner
		for (int x = wt_start; x < wt_start + wt; x++) {
			for (int y = ht_start; y < ht_start + ht; y++) {
				hueSum += src.getHue(x, y);
			}
		}

		int hueApprox = (int) hueSum / (ht * wt);
		//System.out.println("Upper left hue: " + hueApprox);

		// Create a filtered image by using a Gaussian filter
		byte[] blurredPixels = new byte[w * h * 3];
		GaussianBlur.apply(src.toArray(), blurredPixels, w, h);
		Image blurredImage = new Image(blurredPixels, w, h);

		// Use either original or blurred image for processing
		// process(src, dest, w, h);
		process(blurredImage, dest, w, h);
	}

	public void process(Image src, Image dest, int width, int height) {

		int[][] hues = new int[height][width];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				hues[y][x] = src.getHue(x, y);
			}
		}

	    if (serialize && watch.getTime() > 1000*10) {
			watch.reset();
			watch.start();
			for (int x = 0; x < width; x++) {
			    for (int y = 0; y < height; y++) {
				out.print(hues[y][x] + " ");
			    }
			    out.println();
			}
			out.println(); out.println(); out.flush();
			capturedImages.add(src);
	    }

		int hueThreshold = 15;
		int skipThreshold = 1;
		int sizeThreshold = 200;

		Set<Blob> discoveredBlobs = findBlobs(hues, hueThreshold,
				skipThreshold, sizeThreshold);
		
		System.out.println("Found " + discoveredBlobs.size() + " blobs");
		int grayscale = 0;
		
		for (Blob blob : discoveredBlobs) {
			Set<Point2D.Double> blobPoints = blob.getPoints();
			System.out.println("\tBlob of size " + blobPoints.size());
			if (true || blobPoints.size() > 200 && blobPoints.size() < 3000) {
				System.out.println("grayscale: " + grayscale);
				for (Point2D.Double point : blobPoints) {
					dest.setPixel((int) point.x, (int) point.y, (byte) grayscale,
							(byte) grayscale, (byte) grayscale);
				}
				grayscale += 50;
			}
		}
	}

	public Set<Blob> findBlobs(int[][] hues, int hueThreshold,
			int skipThreshold, int sizeThreshold) {
		Set<Point2D.Double> examinedPoints = new HashSet<Point2D.Double>();
		Set<Blob> discoveredBlobs = new HashSet<Blob>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point2D.Double startPoint = new Point2D.Double(x, y);
				if (!examinedPoints.contains(startPoint)) {
					Set<Point2D.Double> currentBlobPoints = findNewBlob(hues,
							startPoint, hueThreshold, skipThreshold);
					examinedPoints.addAll(currentBlobPoints);
					Blob processedBlob = processBlob(hues, currentBlobPoints,
							sizeThreshold);

					if (processedBlob != null) {
						discoveredBlobs.add(processedBlob);
					}
				}
			}
		}

		return discoveredBlobs;
	}

	public Set<Point2D.Double> findNewBlob(int[][] hues,
			Point2D.Double startPoint, int hueThreshold, int skipThreshold) {
		// Initialize a set representing the blob and a queue of points to add
		// to the blob
		Set<Point2D.Double> currentBlob = new HashSet<Point2D.Double>();
		Queue<Point2D.Double> pointsToTest = new LinkedList<Point2D.Double>();
		pointsToTest.add(startPoint);

		while (!pointsToTest.isEmpty()) {
			// Add the first point in the queue to the blob if it isn't already
			// present
			Point2D.Double point = pointsToTest.remove();
			if (!currentBlob.contains(point)) {
				currentBlob.add(point);
				
				// System.out.println("Current blob just added : " + point.x + " " + point.y);

				// Add the surrounding points with similar hues to the queue of
				// points to examine
				for (int xSq = -1 * skipThreshold; xSq < skipThreshold + 1; xSq++) {
					for (int ySq = -1 * skipThreshold; ySq < skipThreshold + 1; ySq++) {
						int xPos = (int) point.x + xSq;
						int yPos = (int) point.y + ySq;

						// If the surrounding point is within the image and
						// satisfies the hue difference
						// criteria, add it to the queue
						if ((xPos >= 0 && xPos <= width - 1)
								&& (yPos >= 0 && yPos <= height - 1)
								&& (Math.abs(hues[yPos][xPos]
										- hues[(int)point.y][(int)point.x]) <= hueThreshold)) {
							pointsToTest.add(new Point2D.Double(xPos, yPos));
						}
					}
				}
			}
		}

		return currentBlob;
	}

	public Blob processBlob(int[][] hues, Set<Point2D.Double> blobPoints,
			int sizeThreshold) {
		Blob blob = null;
		if (blobPoints.size() > sizeThreshold) {
			blob = new Blob(blobPoints, hues);
		}
		return blob;
	}
	
	public void closeSerialization() {
		try {
			outStream.writeObject(capturedImages);
			outStream.close();
		    fileOut.close();
		}
		catch (IOException e) {
		}
	}
}

