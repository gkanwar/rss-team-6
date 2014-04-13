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
	private int width;
	private int height;
	private boolean serialize;
	private boolean useBlurred;
	private int hueThreshold;
	private int skipThreshold;
	private int sizeThreshold;

	public boolean targetDetected = false;
    PrintWriter out;
    StopWatch watch;
    FileOutputStream fileOut;
    ObjectOutputStream outStream;
    List<Image> capturedImages;
    Image currentImage;
    Image destinationImage;
    int[][] currentHues;

	public BlobTrackingChallenge(int width, int height, boolean serialize, boolean useBlurred, int hueThreshold, int skipThreshold, int sizeThreshold) {
		this.width = width;
		this.height = height;
		this.serialize = serialize;
		this.useBlurred = useBlurred;
		this.hueThreshold = hueThreshold;
		this.skipThreshold = skipThreshold;
		this.sizeThreshold = sizeThreshold;
		currentHues = new int[height][width];
		
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

	public void apply(Image src, Image dest) {
		currentImage = src;
		destinationImage = dest; 
		
		// Change current image to blurred image if boolean activated
		if (useBlurred) {
			byte[] blurredPixels = new byte[width * height * 3];
			GaussianBlur.apply(src.toArray(), blurredPixels, width, height);
			currentImage = new Image(blurredPixels, width, height);
		}
		
		// Compute the hues of the current image (unfiltered and filtered)
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				currentHues[y][x] = currentImage.getHue(x, y);
			}
		}

		if(serialize) storeImage();
		// computeUpperLeftAverage();
		
		// Interpret the image
		Set<Blob> discoveredObjects = processImage();
		
		//System.out.println("Found " + discoveredBlobs.size() + " blobs");
		int grayscale = 50;
		for (Blob blob : discoveredObjects) {
			Set<Point2D.Double> blobPoints = blob.getPoints();
			//System.out.println("\tBlob of size " + blobPoints.size());
			if (true || blobPoints.size() > 200 && blobPoints.size() < 3000) {
				//System.out.println("grayscale: " + grayscale);
				for (Point2D.Double point : blobPoints) {
					dest.setPixel((int) point.x, (int) point.y, (byte) grayscale,
							(byte) grayscale, (byte) grayscale);
				}
				grayscale += 50;
			}
		}
	}

	public void computeUpperLeftAverage() {
		int ht = height / 10;
		int wt = width / 10;
		int ht_start = 0;
		int wt_start = 0;

		double hueSum = 0;

		// Determine the average rgb/hsv pixel values in the upper left hand corner
		for (int x = wt_start; x < wt_start + wt; x++) {
			for (int y = ht_start; y < ht_start + ht; y++) {
				hueSum += currentHues[y][x];
			}
		}
		int hueApprox = (int) hueSum / (ht * wt);
		System.out.println("Upper left hue: " + hueApprox);
	}
	
	public void storeImage() {
		if (watch.getTime() > 1000*3) {
			watch.reset();
			watch.start();
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					out.print(currentHues[y][x] + " ");
			    }
			    out.println();
			}
			out.println(); out.println(); out.flush();
			
			capturedImages.add(currentImage);
			System.out.println("Size of captured images: " + capturedImages.size());
			if (capturedImages.size() == 20) {
				closeSerialization();
			}
		}
    }
	
	public Set<Blob> processImage() {
		Set<Blob> hueConstantRegions = findHueConstantRegions();
		Set<Blob> objectRegions = findObjectRegions(hueConstantRegions);
		//classifyObjectRegions(objectRegions);
		
		
		//return hueConstantRegions;
		return objectRegions;
	}
		
	public Set<Blob> findHueConstantRegions() {
		Set<Point2D.Double> examinedPoints = new HashSet<Point2D.Double>();
		Set<Blob> discoveredBlobs = new HashSet<Blob>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Point2D.Double startPoint = new Point2D.Double(x, y);
				if (!examinedPoints.contains(startPoint)) {
					Set<Point2D.Double> currentBlobPoints = findNewBlob(startPoint);
					examinedPoints.addAll(currentBlobPoints);
					discoveredBlobs.add(new Blob(currentBlobPoints));
				}
			}
		}

		return discoveredBlobs;
	}

	public Set<Point2D.Double> findNewBlob(Point2D.Double startPoint) {
		// Initialize a set representing the blob and a queue of points to add
		// to the blob
		Set<Point2D.Double> currentPoints = new HashSet<Point2D.Double>();
		Queue<Point2D.Double> pointsToTest = new LinkedList<Point2D.Double>();
		pointsToTest.add(startPoint);

		while (!pointsToTest.isEmpty()) {
			// Add the first point in the queue to the blob if it isn't already
			// present
			Point2D.Double point = pointsToTest.remove();
			if (!currentPoints.contains(point)) {
				currentPoints.add(point);
				
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
								&& (Image.hueWithinThreshold(currentHues[yPos][xPos], 
										currentHues[(int)point.y][(int)point.x], hueThreshold))) {
							pointsToTest.add(new Point2D.Double(xPos, yPos));
						}
					}
				}
			}
		}

		return currentPoints;
	}
	
	public Set<Blob> findObjectRegions(Set<Blob> hueConstantRegions) {
		Set<Blob> objectBlobs = new HashSet<Blob>();
		for (Blob blob : hueConstantRegions) {
			//System.out.println("size: " + blob.getSize() + " " + (blob.getSize() > sizeThreshold));
			//System.out.println("edge: " + (blob.pointsOnEdge(width, height)));
			//System.out.println("object: " + (blob.isObject(currentHues)));
			if (blob.getSize() > sizeThreshold && !blob.pointsOnEdge(width, height) && blob.isObject(currentHues)) {
				objectBlobs.add(blob);
			}
		}
		return objectBlobs;
	}
	
	public void classifyObjectRegions(Set<Blob> objectBlobs) {
		for (Blob blob : objectBlobs) {
			blob.classifyShape();
		}
	}
	
	public void closeSerialization() {
		try {
			outStream.writeObject(capturedImages);
			outStream.close();
		    fileOut.close();
		    serialize = false;
		    System.out.println("SERIALIZATION STREAM ENDED");
		}
		catch (IOException e) {
		}
	}
}

