package utility;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import imageMat.ImageMat;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;


public class UtilityMethods {

	public static boolean charArrayContainsChar(char[] charArray, char inputChar){
		for(int i = 0; i < charArray.length; i++){
			if(charArray[i] == inputChar){
				return true;
			}
		}
		return false;
	}

	public static double getAvg(double[] values) {
		double total = 0;
		for(double val : values){
			total += val;
		}
		return total/values.length;
	}

	public static char[] combineArrays(char[] array1, char[] array2) {
		if(array1 == null){
			return array2;
		}
		if(array2 == null){
			return array1;
		}
		char[] result = new char[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	public static double getMaxArrayValue(Double[] array){
		double max = Double.MIN_VALUE;
		for(int i = 0; i < array.length; i++){
			if(array[i] > max){
				max = array[i];
			}
		}
		return max;
	}


	public static double getMinArrayValue(Double[] array){
		double min = Double.MAX_VALUE;
		for(int i = 0; i < array.length; i++){
			if(array[i] < min){
				min = array[i];
			}
		}
		return min;
	}

	public static ImageMat[] combineArrays(ImageMat[] array1, ImageMat[] array2) {
		if(array1 == null){
			return array2;
		}
		if(array2 == null){
			return array1;
		}
		ImageMat[] result = new ImageMat[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	public static ArrayList<Integer> getRandomSequence(int length) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < length; i++){
			list.add(i);
		}
		Collections.shuffle(list);
		return list;
	}

	public static int getSmallest(int i, int j) {
		if(i > j)
			return j;
		return i;
	}

	public static int getMaxLoc(double[] vector) {
		double biggest = Double.MIN_VALUE;
		int maxLoc = -1;
		for(int i = 0; i < vector.length; i++){
			if(vector[i] > biggest){
				maxLoc = i;
				biggest = vector[i];
			}
		}
		return maxLoc;
	}

	public static char getRandomChar(char[] ocrChars) {
		int randomIndex = (int) (Math.random() * ocrChars.length);
		return ocrChars[randomIndex];
	}

	public static int[] convertIntegerListToIntArray(
			ArrayList<Integer> list) {

		int[] result = new int[list.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	public static void createAllDirs(String outputDirName) {
		String[] subDirs = outputDirName.split("/");
		for(int i = 0; i < subDirs.length; i++){
			String subDirString = "";
			for(int j = 0; j <= i; j++){
				if(j != 0){
					subDirString += "/";
				}
				subDirString += subDirs[j];
			}
			File newDir = new File(subDirString);
			if(!newDir.exists()){
				newDir.mkdir();
			}
		}
	}

	public static String[] parseTextFileToStringArray(File textFile) throws Exception {
		BufferedReader read = new BufferedReader(new FileReader(textFile));
		ArrayList<String> result = new ArrayList<>();

		String line = read.readLine();
		while(line != null){
			result.add(line);
			line = read.readLine();
		}
		read.close();
		return result.toArray(new String[result.size()]);
	}

	public static boolean pointArrayContainsPoint(Point[] points, double row, double col) {
		for(int i = 0; i < points.length; i++){
			if(points[i].x == row && points[i].y == col)
				return true;
		}
		return false;
	}

	public static Comparator<MatOfPoint> getMatOfPointVerticalComparator() {
		return new MatOfPointVerticalComparator();
	}

	public static Comparator<MatOfPoint> getMatOfPointHorizontalComparator() {
		return new MatOfPointHorizontalComparator();
	}

	private static class MatOfPointVerticalComparator implements Comparator<MatOfPoint> {
		@Override
		public int compare(MatOfPoint o1, MatOfPoint o2) {
			Rect o1Bounds = Imgproc.boundingRect(o1);
			Rect o2Bounds = Imgproc.boundingRect(o2);
			int yDiff = Math.abs(o1Bounds.y - o2Bounds.y);
			//int xDiff = Math.abs(o1Bounds.x = o2Bounds.x);

			//if(yDiff > xDiff){
			if(yDiff > Const.CHAR_CONTOUR_VERTICAL_PIXEL_DISTANCE_THRESHOLD){
				//one with the smaller y (above) is first
				return new Integer(o1Bounds.y).compareTo(new Integer(o2Bounds.y));
			}
			else {
				//one with the smaller x (to the left) is first
				return new Integer(o1Bounds.x).compareTo(new Integer(o2Bounds.x));
			}
		}

	}

	private static class MatOfPointHorizontalComparator implements Comparator<MatOfPoint> {
		@Override
		public int compare(MatOfPoint o1, MatOfPoint o2) {
			Rect o1Bounds = Imgproc.boundingRect(o1);
			Rect o2Bounds = Imgproc.boundingRect(o2);
			int xDiff = Math.abs(o1Bounds.x - o2Bounds.x);
			if(xDiff == 0){
				return new Integer(o1Bounds.width).compareTo(new Integer(o2Bounds.width));
			}
			else {
				return new Integer(o1Bounds.x).compareTo(new Integer(o2Bounds.x));
			}
		}

	}

	public static int getModeNumContours(List<ImageMat> images) {
		int maxValue = -1, maxCount = 0;
		for(int i = 0; i < images.size(); i++){
			int count = 0;
			for(int j = 0; j < images.size(); j++){
				if(images.get(j).getContourPoints().size() ==
						images.get(i).getContourPoints().size())
					count++;
			}
			if(count > maxCount){
				maxCount = count;
				maxValue = images.get(i).getContourPoints().size();
			}
		}
		return maxValue;
	}

	public static Rect getAvgRect(List<Rect> list) {
		int totalX=0, totalY=0, totalW=0, totalH=0;
		for(Rect rect : list){
			totalX += rect.x;
			totalY += rect.y;
			totalW += rect.width;
			totalH += rect.height;
		}
		int x = (int) Math.round((0.0 + totalX) / list.size());
		int y = (int) Math.round((0.0 + totalY) / list.size());
		int w = (int) Math.round((0.0 + totalW) / list.size());
		int h = (int) Math.round((0.0 + totalH) / list.size());
		return new Rect(x, y, w, h);
	}

	public static List<Rect> convertMatOfPointsToBounds(
			List<MatOfPoint> contours) {
		List<Rect> result = new ArrayList<>();
		for(MatOfPoint contour : contours){
			result.add(Imgproc.boundingRect(contour));
		}
		return result;
	}

	/**
	 * Gets an array of widths from a list of bounds
	 * @param filteredContours
	 * @return
	 */
	public static int[] getWidthsArrayFromBoundsList(
			List<Rect> boundsList) {
		int[] widths = new int[boundsList.size()];
		for(int i = 0; i < widths.length; i++){
			widths[i] = boundsList.get(i).width;
		}
		return widths;
	}

	/**
	 * Converts a list of Rects into one big Rect with an x and y of (0,0)
	 * @param allBounds
	 * @return
	 */
	public static Rect combineRects(List<Rect> allBounds) {
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		for(Rect bounds : allBounds){
			int startX = bounds.x;
			int endX = startX + bounds.width;
			int startY = bounds.y;
			int endY = startY + bounds.height;
			if(startX < minX)
				minX = startX;
			if(endX > maxX)
				maxX = endX;
			if(startY < minY)
				minY = startY;
			if(endY > maxY)
				maxY = endY;
		}
		return new Rect(0, 0, maxX - minX, maxY - minY);
	}

	public static Point getCenter(Rect boundingRect) {
		int x = boundingRect.x + boundingRect.width/2;
		int y = boundingRect.y + boundingRect.height/2;
		return new Point(x,y);
	}

	public static double getDistance(Point p1, Point p2) {
		return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
	}

	public static MatOfPoint addMatOfPoints(MatOfPoint pointMat1,
			MatOfPoint pointMat2) {
		ArrayList<Point> points = new ArrayList<Point>(pointMat1.toList());
		points.addAll(pointMat2.toList());
		return new MatOfPoint(points.toArray(new Point[0]));
	}

	public static void printBoundsFromMatOfPointList(List<MatOfPoint> matOfPointList,
			String text) {

		System.out.println("all contour center points in image for word: " + text);
		for(MatOfPoint pointMat : matOfPointList){
			Point center = getCenter(Imgproc.boundingRect(pointMat));
			System.out.print(center + " ");
		}
	}

	public static double getMaxXOfPoints(ArrayList<Point> allPoints) {
		double max = Integer.MIN_VALUE;
		for(Point p : allPoints){
			if(p.x > max){
				max = p.x;
			}
		}
		return max;
	}

	public static double getMaxYOfPoints(ArrayList<Point> allPoints) {
		double max = Integer.MIN_VALUE;
		for(Point p : allPoints){
			if(p.y > max){
				max = p.y;
			}
		}
		return max;
	}

	public static void printOutElapsedTime(long ms) {
		double seconds = ms / 1000.0;

		int minutes = (int) (seconds / 60.0);
		double remainingSeconds = seconds - (60.0 * minutes);
		if(minutes > 0){
			System.out.print(minutes + " minutes ");
		}
		System.out.println(remainingSeconds + " seconds");
	}

	public static String getFileExtension(File file) {
		String path = file.getAbsolutePath();
		int index = path.lastIndexOf(".");
		return path.substring(index + 1);
	}

	public static Image convertBufImageToFXImage(BufferedImage bufImage){
		return SwingFXUtils.toFXImage(bufImage, null);
	}

	public static void writeLineToFileAndStandardOut(String output, BufferedWriter write) throws IOException {
		write.write(output);
		write.newLine();
		System.out.println(output);

	}

	public static Rect getBoundingRectFromMulitpleContours(List<MatOfPoint> contours) {
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		for(MatOfPoint contour: contours){
			Rect contourBounds = Imgproc.boundingRect(contour);
			int x = contourBounds.x;
			int y = contourBounds.y;
			int xEnd = x + contourBounds.width;
			int yEnd = y + contourBounds.height;
			if(x < minX)
				minX = x;
			if(y < minY)
				minY = y;
			if(xEnd > maxX)
				maxX = xEnd;
			if(yEnd > maxY)
				maxY = yEnd;
		}
		return new Rect(minX, minY, maxX - minX, maxY - minY);
	}

}
