package VisualServo;

import java.util.ArrayList;
import java.util.List;

public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		Image image = new Image(generateImage(), 50, 50);
		List<Image> list = new ArrayList<Image>();
		list.add(image);
		ImageGUI gui = new ImageGUI(list);
		
		/*byte[] src = generateImage();
		Image image = new Image(src, 50, 50);	
		Image dest = new Image(50, 50);
		BlobTrackingChallenge tracker = new BlobTrackingChallenge(50, 50);
		tracker.process(image,  dest, 50, 50);*/
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
