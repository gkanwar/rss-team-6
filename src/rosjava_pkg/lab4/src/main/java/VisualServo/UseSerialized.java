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
		UseSerialized tester = new UseSerialized("C:\\Katharine\\MIT\\Classes\\6.141\\images.ser");
	}
		
	public UseSerialized(String fileName) {
		try {
			fileIn = new FileInputStream(fileName);
			in = new ObjectInputStream(fileIn);
			try {
				imageSet = (List<Image>) in.readObject();
				interpretImages(imageSet);
				ImageGUI gui = new ImageGUI(processedImages);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			in.close();
			fileIn.close();			
		}
		catch(IOException e) {
		}
	}

	private void interpretImages(List<Image> imageSet) {
		processedImages = new ArrayList<Image>();
		BlobTrackingChallenge blobTracker = new BlobTrackingChallenge(50, 50, false);
		for (Image image : imageSet) {
			Image dest = new Image(image);
			blobTracker.process(image, dest, 50, 50);
			processedImages.add(dest);
		}
	}

}
