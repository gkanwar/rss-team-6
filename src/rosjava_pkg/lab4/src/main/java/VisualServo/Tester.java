package VisualServo;

import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class Tester {

	FileOutputStream fileOut;
    ObjectOutputStream outStream;
    
	public static void main(String[] args) {		
		/*Image image = new Image(generateImage(), 50, 50);
		List<Image> list = new ArrayList<Image>();
		list.add(image);
		ImageGUI gui = new ImageGUI(list);*/
		
		Tester tester = new Tester();
		testImage();
		
		/*byte[] src = generateImage();
		Image image = new Image(src, 50, 50);	
		Image dest = new Image(50, 50);
		BlobTrackingChallenge tracker = new BlobTrackingChallenge(50, 50);
		tracker.process(image,  dest, 50, 50);*/
	}
	
	public Tester() {
		try {
		    fileOut = new FileOutputStream("C:\\Katharine\\MIT\\Classes\\6.141\\images.ser");
		    outStream = new ObjectOutputStream(fileOut);
		    Image image = new Image(generateImage(), 50, 50);
		    List<Image> capturedImages = new ArrayList<Image>();
		    capturedImages.add(image);
		    outStream.writeObject(capturedImages);
			outStream.close();
		    fileOut.close();
		}
		catch (IOException e) {
		}
	}
	
	public static void testImage() {
		System.out.println("Running testImage");
		if (!Image.hueWithinRange(20, 10, 30)) System.out.println("Failed 1");
		if (!Image.hueWithinRange(80,50,2)) System.out.println("Failed 2");
		if (!Image.hueWithinRange(10,80,20)) System.out.println("Failed 3");
		if (!Image.hueWithinThreshold(0, 255, 1)) System.out.println("FAiled 4");
		if (!Image.hueWithinThreshold(34, 35, 1)) System.out.println("FAiled 5");
		if (Image.hueWithinThreshold(1, 255, 1)) System.out.println("Failed 6");
	}
	
	public static byte[] generateImage() {
		// create new 50x50 image
		byte[] src = new byte[7500];
		int width = 50;
		int height = 50;
		for (int x=10; x<25; x++) {
			for (int y=10; y<25; y++) {
				changeImagePixel(src, x, y, width, height, 0, 0, 255);
			}
		}
		for (int x = 30; x<40; x++) {
			for (int y=30; y<40; y++) {
				changeImagePixel(src, x, y, width, height, 0, 255, 0);
			}
		}
		return src;
	}
	
	public static void changeImagePixel(byte[] src, int x, int y, int width, int height, int red, int green, int blue) {
		src[(y*width + x)*3] = (byte) red;
		src[(y*width + x)*3 + 1] = (byte) green;
		src[(y*width + x)*3 + 2] = (byte) blue;
	}

}
