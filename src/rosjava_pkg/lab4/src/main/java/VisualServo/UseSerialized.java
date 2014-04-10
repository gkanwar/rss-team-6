package VisualServo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class UseSerialized extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FileInputStream fileIn;
	ObjectInputStream in;
	Set<Image> imageSet;
	protected JFrame frame;
	
	public static void main(String[] args) {
		System.out.println("hello!");
		UseSerialized tester = new UseSerialized("yo.ser");
	}
		
	public UseSerialized(String fileName) {
		try {
			fileIn = new FileInputStream(fileName);
			in = new ObjectInputStream(fileIn);
			try {
				imageSet = (Set<Image>) in.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			in.close();
			fileIn.close();
			
		}
		catch(IOException e) {
		}
	}

	private ArrayList<Image> interpretImages(Set<Image> imageSet) {
		ArrayList<Image> processedImages = new ArrayList<Image>();
		return processedImages;
	}

}
