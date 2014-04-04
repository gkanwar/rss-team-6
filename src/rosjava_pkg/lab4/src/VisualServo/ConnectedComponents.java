package VisualServo;

import java.util.ArrayList;

/**
 * Edsinger: from
 * http://homepages.inf.ed.ac.uk/rbf/HIPR2/flatjavasrc/ImageLabel.java
 * ImageLabel is an algorithm that applies Connected Component Labeling
 * alogrithm to an input image. Only mono images are catered for.
 * 
 * @author Neil Brown, DAI
 * @author Judy Robertson, SELLIC OnLine
 * @see code.iface.imagelabel
 */
public class ConnectedComponents {
    // the width and height of the output image
    private int d_w;
    private int d_h;
    private int labels[];
    private int numberOfLabels;
    private boolean labelsValid = false;
    private int colorMax = 0;
    private int countMax = 0;

    /**
     * Constructs a new Image Operator
     */
    public ConnectedComponents() {

    }

    /**
     * Get the value of a pixel at some distance away from a specified pixel.
     * 
     * @param src1d
     *            the image
     * @param i
     *            the pixel whose neighbor is sought
     * @param ox
     *            the horizontal offset
     * @param oy
     *            the vertical offset
     * @return The pixel value of i's neighbor which is ox and oy away from i,
     *         or 0 if this is outside the image.
     */

	public ArrayList<Integer> getComponents(int[][] src, int height, int width) {
		int currentLabel = 1;
		ArrayList<Integer> blobSize = new ArrayList<Integer>();
		blobSize.add(0);

		//print out initial array
		/*for(int i=0; i<width; i++){
		    for(int j=0; j<height; j++)
			System.out.print(src[j][i] + " ");
		    System.out.println();
		    }*/
		
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (src[j][i] == -1) {
					int size = startNewLabel(src, height, width, i, j, currentLabel);
					currentLabel++;
					blobSize.add(size);
				}
			}
		}

		//print out final array
		/*for(int i=0; i<width; i++){
		    for(int j=0; j<height; j++)
			System.out.print(src[j][i] + " ");
		    System.out.println();
		    }*/
		
		return blobSize;
	}

    public ArrayList<Integer> getComponents(int[][] src, int height, int width, int threshold) {
		int currentLabel = 1;
		ArrayList<Integer> blobSize = new ArrayList<Integer>();
		blobSize.add(0);

       	
		for (int i=0; i<width; i++) {
			for (int j=0; j<height; j++) {
				if (src[j][i] == -1) {
				    int size = startNewLabel(src, height, width, i, j, currentLabel, threshold);
					currentLabel++;
					blobSize.add(size);
				}
			}
		}

		return blobSize;
	}
	
	public int startNewLabel(int[][] src, int height, int width, int i, int j, int labelNum) {
		ArrayList<Integer> x = new ArrayList<Integer>();
		x.add(i);
		ArrayList<Integer> y = new ArrayList<Integer>();
		y.add(j);
		int count = 0;
		
		while (!x.isEmpty()) {
			int xToLabel = x.get(0);
			int yToLabel = y.get(0);
			x.remove(0);
			y.remove(0);
			if (src[yToLabel][xToLabel] == -1) {
				src[yToLabel][xToLabel] = labelNum;
				count++;
			
				int left = Math.max(0,xToLabel-1);
				int right = Math.min(width-1,xToLabel+1);
				int top = Math.max(0,yToLabel-1);
				int bottom = Math.min(height-1,yToLabel+1);
				
				if (src[top][left] == -1) { x.add(left); y.add(top); }
				if (src[top][xToLabel] == -1) { x.add(xToLabel); y.add(top); }
				if (src[top][right] == -1) { x.add(right); y.add(top); }
				if (src[yToLabel][left] == -1) { x.add(left); y.add(yToLabel); }
				if (src[yToLabel][right] == -1) { x.add(right); y.add(yToLabel); }
				if (src[bottom][left] == -1) { x.add(left); y.add(bottom); }
				if (src[bottom][xToLabel] == -1) { x.add(xToLabel); y.add(bottom); }
				if (src[bottom][right] == -1) { x.add(right); y.add(bottom); }
			}
		}
		
		return count;
	}


    public int startNewLabel(int[][] src, int height, int width, int i, int j, int labelNum, int threshold) {
	ArrayList<Integer> x = new ArrayList<Integer>();
	x.add(i);
	ArrayList<Integer> y = new ArrayList<Integer>();
	y.add(j);
	int count = 0;
	
	while (!x.isEmpty()) {
	    int xToLabel = x.get(0);
	    int yToLabel = y.get(0);
	    x.remove(0);
	    y.remove(0);
	    if (src[yToLabel][xToLabel] == -1) {
	    	src[yToLabel][xToLabel] = labelNum;
	    	count++;
		
	    	for(int k=-1*threshold; k<threshold+1; k++){
	    		for(int l=-1*threshold; l<threshold+1; l++){
	    			int xpos = xToLabel + k;
	    			int ypos = yToLabel + l;
			
	    			if(xpos < 0) xpos = 0;
	    			if(ypos < 0) ypos = 0;
	    			if(xpos >= width) xpos = width - 1;
	    			if(ypos >= height) ypos = height - 1;
	    			
	    			if(src[ypos][xpos] == -1) {x.add(xpos); y.add(ypos);}
		    }
		} 
	    }
	}
	
	return count;
    }


	/**
	 * Get the value of a pixel at some distance away from a specified pixel.
	 * 
	 * @param src1d the image
	 * @param i the pixel whose neighbor is sought
	 * @param ox the horizontal offset
	 * @param oy the vertical offset
	 * @return The pixel value of i's neighbor which is ox and oy away from i,
	 * or 0 if this is outside the image.
	 */
	private int getNeighbour(int[] src1d, int i, int ox, int oy) {
		int x, y;
		// TODO: Fill me in
        return 0;
	}

    /**
     * Associate (equivalence) a with b. a should be less than b to give some
     * ordering (sorting) if b is already associated with some other value, then
     * propagate down the list.
     */
    private void associate(int a, int b) {
        if (a > b) {
            associate(b, a);
            return;
        }
        if ((a == b) || (labels[b] == a))
            return;
        if (labels[b] == b) {
            labels[b] = a;
        } else {
            associate(labels[b], a);
            labels[b] = a;
        }
    }

    /**
     * Reduces the number of labels.
     */
    private int reduce(int a) {
        if (labels[a] == a) {
            return a;
        } else {
            return reduce(labels[a]);
        }
    }

    /**
     * doLabel applies the Labeling algorithm plus offset and scaling The input
     * image is expected to be 8-bit mono 0=black everything else=white
     * 
     * @param src1_1d
     *            The input pixel array
     * @param width
     *            width of the destination image in pixels
     * @param height
     *            height of the destination image in pixels
     * @return A pixel array containing the labelled image
     */
    // NB For images 0,0 is the top left corner.
    public int[] doLabel(int[] src1_1d, int[] dest_1d, int width, int height) {

        int nextlabel = 1;
        int nbs[] = new int[4];
        int nbls[] = new int[4];

        // Get size of image and make 1d_arrays
        d_w = width;
        d_h = height;

        labels = new int[d_w * d_h / 3]; // the most there can be is 9/25 of the
                                         // point

        int src1rgb;
        int result = 0;
        int count, found;

        labelsValid = false; // only set to true once we've complete the task
        // initialise labels
        for (int i = 0; i < labels.length; i++)
            labels[i] = i;

        // now Label the image
        for (int i = 0; i < src1_1d.length; i++) {

            src1rgb = src1_1d[i] & 0x000000ff;

            if (src1rgb == 0) {
                result = 0; // nothing here
            } else {

                // The 4 visited neighbours
                nbs[0] = getNeighbour(src1_1d, i, -1, 0);
                nbs[1] = getNeighbour(src1_1d, i, 0, -1);
                nbs[2] = getNeighbour(src1_1d, i, -1, -1);
                nbs[3] = getNeighbour(src1_1d, i, 1, -1);

                // Their corresponding labels
                nbls[0] = getNeighbour(dest_1d, i, -1, 0);
                nbls[1] = getNeighbour(dest_1d, i, 0, -1);
                nbls[2] = getNeighbour(dest_1d, i, -1, -1);
                nbls[3] = getNeighbour(dest_1d, i, 1, -1);

                // label the point
                if ((nbs[0] == nbs[1]) && (nbs[1] == nbs[2])
                        && (nbs[2] == nbs[3]) && (nbs[0] == 0)) {
                    // all neighbours are 0 so gives this point a new label
                    result = nextlabel;
                    nextlabel++;
                } else { // one or more neighbours have already got labels
                    count = 0;
                    found = -1;
                    for (int j = 0; j < 4; j++) {
                        if (nbs[j] != 0) {
                            count += 1;
                            found = j;
                        }
                    }
                    if (count == 1) {
                        // only one neighbour has a label, so assign the same
                        // label to this.
                        result = nbls[found];
                    } else {
                        // more than 1 neighbour has a label
                        result = nbls[found];
                        // Equivalence the connected points
                        for (int j = 0; j < 4; j++) {
                            if ((nbls[j] != 0) && (nbls[j] != result)) {
                                associate(nbls[j], result);
                            }
                        }
                    }
                }
            }

            dest_1d[i] = result;
        }

        // reduce labels ie 76=23=22=3 -> 76=3
        // done in reverse order to preserve sorting
        for (int i = labels.length - 1; i > 0; i--) {
            labels[i] = reduce(i);
        }

        /*
         * now labels will look something like 1=1 2=2 3=2 4=2 5=5.. 76=5 77=5
         * this needs to be condensed down again, so that there is no wasted
         * space eg in the above, the labels 3 and 4 are not used instead it
         * jumps to 5.
         */
        int condensed[] = new int[nextlabel]; // cant be more than nextlabel
                                              // labels

        count = 0;
        for (int i = 0; i < nextlabel; i++) {
            if (i == labels[i])
                condensed[i] = count++;
        }
        // Record the number of labels
        numberOfLabels = count - 1;

        // now run back through our preliminary results, replacing the raw label
        // with the reduced and condensed one, and do the scaling and offsets
        // too

        // Now generate an array of colours which will be used to label the
        // image
        int[] labelColors = new int[numberOfLabels + 1];
        int[] labelCnt = new int[numberOfLabels + 1];

        // Variable used to check if the color generated is acceptable
        boolean acceptColor = false;

        for (int i = 0; i < labelColors.length; i++) {
            labelCnt[i] = 0;
            acceptColor = false;
            while (!acceptColor) {
                double tmp = Math.random();
                labelColors[i] = (int) (tmp * 16777215);
                if (((labelColors[i] & 0x000000ff) < 200)
                        && (((labelColors[i] & 0x0000ff00) >> 8) < 64)
                        && (((labelColors[i] & 0x00ff0000) >> 16) < 64)) {
                    // Color to be rejected so don't set acceptColor
                } else {
                    acceptColor = true;
                }
            }
            if (i == 0)
                labelColors[i] = 0;
        }

        countMax = 0;
        for (int i = 0; i < dest_1d.length; i++) {
            result = condensed[labels[dest_1d[i]]];
            labelCnt[result]++;
            if (countMax < labelCnt[result] && result != 0) {
                countMax = labelCnt[result];
                colorMax = labelColors[result] + 0xff000000;
            }

            // result = (int) ( scale * (float) result + oset );
            // truncate if necessary
            // if( result > 255 ) result = 255;
            // if( result < 0 ) result = 0;
            // produce grayscale
            // dest_1d[i] = 0xff000000 | (result + (result << 16) + (result <<
            // 8));
            dest_1d[i] = labelColors[result] + 0xff000000;
        }

        labelsValid = true; // only set to true now we've complete the task
        return dest_1d;
    }

    /**
     * @return the number of unique, nonzero colours, or -1 if invalid
     */
    public int getColours() {
        if (labelsValid) {
            return numberOfLabels;
        } else {
            return -1;
        }
    }

    /**
     * @return the number of labels.
     */
    public int getNumberOfLabels() {
        return numberOfLabels;
    }

    /**
     * @return The maximum color.
     */
    public int getColorMax() {
        return colorMax;
    }

    /**
     * @return The maximum count.
     */
    public int getCountMax() {
        return countMax;
    }
}
