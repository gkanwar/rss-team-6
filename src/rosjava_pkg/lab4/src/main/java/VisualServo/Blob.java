package VisualServo;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Blob {
	private Set<Point2D.Double> points;
	private Set<Point2D.Double> hullPoints;
	Map<Integer, Integer> minYforGivenX;
	Map<Integer, Integer> maxYforGivenX;
	Map<Integer, Integer> minXforGivenY;
	Map<Integer, Integer> maxXforGivenY;
	
	private int hueAvg;
	private String color;
	private boolean isObject;
	
	
	private double centroidX;
	private double centroidY;
	private double width;
	private double height;
	private double hue;
	private boolean isSphere;
	private double range;
	private double bearing;
	

	public static enum ObjectMatch {
		RED(246,13), 
		ORANGE(14,25),
		YELLOW(26,30),
		GREEN(31,110),
		BLUE(111,168),
		PURPLE(169,245);
		
		public final int minHue;
		public final int maxHue;
		public final int minSat;
		public final int maxSat;
	
		private ObjectMatch(int minHue, int maxHue) {
			this.minHue = minHue;
			this.maxHue = maxHue;
			this.minSat = 100;
			this.maxSat = 255;
		}
	}

	public Blob(Set<Point2D.Double> points) {
		this.points = points;
		minYforGivenX = new HashMap<Integer,Integer>();
		maxYforGivenX = new HashMap<Integer,Integer>();
		minXforGivenY = new HashMap<Integer,Integer>();
		maxXforGivenY = new HashMap<Integer,Integer>();
	}
	
	public int getSize() {
		return points.size();
	}
	
	public boolean pointsOnEdge(int width, int height) {
		for (Point2D.Double point : points) {
			if ((int)point.x == 0 || (int)point.x == width-1 || (int)point.y == 0 || (int)point.y == height-1)
				return true;
		}
		return false;
	}
	
	public boolean isObject(int[][] hues) {
		int hueSum = 0;
		for (Point2D.Double point : points) {
			hueSum += hues[(int)point.y][(int)point.x];
		}
		hueAvg = (int) hueSum / points.size();
		
		for (ObjectMatch potential : ObjectMatch.values()) {
			if (Image.hueWithinRange(hueAvg, potential.minHue, potential.maxHue)) {
				color = potential.name();
				isObject = true;
				return true;
			}
		}
		color = "NA";
		isObject = false;
		return false;
	}
	
	public void classifyShape() {
		findConvexHull();
		// need to compute centroid based on convex hull maps next and then compute standard deviation to classify
	}
	
	private void findConvexHull() {
		int x;
		int y;
		for (Point2D.Double point : points) {
			x = (int)point.x;
			y = (int)point.y;
			minYforGivenX.put(x, !minYforGivenX.containsKey(x) ? y : Math.min(y, minYforGivenX.get(x)));
			maxYforGivenX.put(x, !maxYforGivenX.containsKey(x) ? y : Math.max(y, maxYforGivenX.get(x)));
			minXforGivenY.put(y, !minXforGivenY.containsKey(y) ? x : Math.min(x, minXforGivenY.get(y)));
			maxXforGivenY.put(y, !maxXforGivenY.containsKey(y) ? x : Math.max(x, maxXforGivenY.get(y)));
		}
		
		for (Map.Entry<Integer, Integer> point : minYforGivenX.entrySet()) {
			hullPoints.add(new Point2D.Double(point.getKey(),point.getValue()));
		}
		for (Map.Entry<Integer, Integer> point : maxYforGivenX.entrySet()) {
			hullPoints.add(new Point2D.Double(point.getKey(),point.getValue()));
		}
		for (Map.Entry<Integer, Integer> point : minXforGivenY.entrySet()) {
			hullPoints.add(new Point2D.Double(point.getValue(),point.getKey()));
		}
		for (Map.Entry<Integer, Integer> point : maxXforGivenY.entrySet()) {
			hullPoints.add(new Point2D.Double(point.getValue(),point.getKey()));
		}
	}

	public void calculateBasics() {
		double sumX = 0;
		double minX = width;
		double maxX = 0;
		double sumY = 0;
		double minY = height;
		double maxY = 0;
		double sumHue = 0;

		for (Point2D.Double point : points) {
			sumX += point.x;
			sumY += point.y;
			minX = Math.min(minX, point.x);
			maxX = Math.max(maxX, point.x);
			minY = Math.min(minY, point.y);
			maxY = Math.max(maxY, point.y);
		}

		centroidX = sumX / points.size();
		centroidY = sumY / points.size();
		width = maxX - minX;
		height = maxY - minY;
		hue = sumHue / points.size();

		calculateRange();
		calculateBearing();
		calculateShape();
	}

	private void calculateRange() {

	}

	private void calculateBearing() {

	}

	private void calculateShape() {

	}

	public Set<Point2D.Double> getPoints() {
		return points;
	}

	public double getRange() {
		return range;
	}

	public double getBearing() {
		return bearing;
	}

	public String getColor() {
		return color;
	}

	public boolean getIsSphere() {
		return isSphere;
	}
}
