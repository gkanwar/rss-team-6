package VisualServo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UseSerialized {
	private static final long serialVersionUID = 1L;
	FileInputStream fileIn;
	ObjectInputStream in;
	List<Image> imageSet;
	List<Image> processedImages;
	
	public static void main(String[] args) {
		UseSerialized tester = new UseSerialized("C:\\Users\\Katharine\\rss-team-6\\src\\rosjava_pkg\\lab4\\snapshots\\imageObjects.ser");
	}
		
	public UseSerialized(String fileName) {
		try {
			fileIn = new FileInputStream(fileName);
			in = new ObjectInputStream(fileIn);
			try {
				System.out.println("here1");
				imageSet = (List<Image>) in.readObject();
				System.out.println("here3");
				interpretImages(imageSet);
				ImageGUI gui = new ImageGUI(processedImages);
			} catch (ClassNotFoundException e) {
				System.out.println("here2");
				e.printStackTrace();
			}
			in.close();
			fileIn.close();			
		}
		catch(IOException e) {
		}
	}

	private void interpretImages(List<Image> imageSet) {
		System.out.println("here!");
		processedImages = new ArrayList<Image>();
		BlobTrackingChallenge blobTracker = new BlobTrackingChallenge(160, 120, false);
		for (Image image : imageSet) {
			Image dest = new Image(image);
			blobTracker.process(image, image, dest, 160, 120);
			processedImages.add(image);
			processedImages.add(dest);
		}
	}

}
