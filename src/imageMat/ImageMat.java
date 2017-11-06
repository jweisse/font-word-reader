package imageMat;

import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import utility.Const;
import utility.UtilityMethods;

public class ImageMat extends Mat{

	public ImageMat(ImageMat img, Rect bound) {
		super(img, bound);
	}

	public ImageMat(Mat mat) {
		mat.copyTo(this);
	}

	public ImageMat(String fileName) {
		Mat mat = Imgcodecs.imread( fileName );
		mat.copyTo(this);
	}

	public ImageMat(double[] values, int rows, int cols){
		Mat mat = new MatOfDouble(transformValues(values, rows, cols, 3));
		Mat newMat = new Mat(rows, cols, CvType.CV_8UC3 );
		mat.convertTo(newMat, CvType.CV_8UC3);
		Mat newMat2 = newMat.reshape(3, cols).t();
		newMat2.copyTo(this);
	}

	public ImageMat(BufferedImage image){
		Mat mat = new Mat(image.getHeight(), image.getWidth(), Const.IMAGE_MAT_TYPE);
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, pixels);
		mat.copyTo(this);
	}

	private double[] transformValues(double[] values,int rows,int cols, int channels){
		double[][][] og3dArray = new double[cols][rows][channels];
		int count = 0;
		for(int i = 0; i < og3dArray[0].length; i++){
			for(int j = 0; j < og3dArray.length; j++){
				for(int k = 0; k < og3dArray[0][0].length; k++){
					og3dArray[j][i][k] = values[count];
					count++;
				}
			}
		}
		count = 0;
		double[] result = new double[values.length];
		for(int i = 0; i < og3dArray.length; i++){
			for(int j = 0; j < og3dArray[i].length; j++){
				for(int k = 0; k < og3dArray[i][j].length; k++){
					result[count] = og3dArray[i][j][k];
					count++;

				}
			}
		}
		return result;
	}

	public double[] toDoubleArray(){
		Mat newMat = this.reshape(1, 1).t();
		Mat newMat2 = new Mat(this.rows(), this.cols(), CvType.CV_64FC1 );
		newMat.convertTo(newMat2, CvType.CV_64FC1);
		return new MatOfDouble(newMat2).toArray();
	}

	/**
	 * Sizes the Mat file to the requested size, then converts file to an array
	 * of doubles, that are normalized between -1 and 1
	 *
	 * @param width number of columns to size the image
	 * @param height number of rows to size the image
	 * @return
	 */
	public double[] getNormalizedPixelData(int width, int height) {
		return getPixelData(width, height, true);
	}

	/**
	 * Sizes the Mat file to the requested size, then converts file to an array
	 * of doubles, between 0 and 255.0
	 *
	 * @param width number of columns to size the image
	 * @param height number of rows to size the image
	 * @return
	 */
	public double[] getPixelData(int width, int height) {
		return getPixelData(width, height, false);
	}

	private double[] getPixelData(int width, int height, boolean normalize){
		Mat sizedMat = new Mat();
		Imgproc.resize( this, sizedMat, new Size( width, height ) );
		double[] pixelData = new double[height * width];
		int arrayIndex = 0;
		NormalizedField normalizer = new NormalizedField(NormalizationAction.Normalize,
				"pixelValue", 255, 0, 1, -1);

		for(int row = 0; row < sizedMat.rows(); row++){
			for(int col = 0; col < sizedMat.cols(); col++){
				double pixelValue = UtilityMethods.getAvg(sizedMat.get(row, col));
				if(normalize){
					pixelData[arrayIndex] = normalizer.normalize(pixelValue);
				}
				else {
					pixelData[arrayIndex] = pixelValue;
				}
				arrayIndex++;
			}
		}
		return pixelData;
	}

	public File writeResizedImageToFile(int width, int height, String filePathAndName) throws Exception{
		Mat sizedMat = new Mat();
		Imgproc.resize( this, sizedMat, new Size( width, height ) );
		ImageMat sizedImageMat = new ImageMat(sizedMat);
		return sizedImageMat.writeToFile(filePathAndName);

	}

	/**
	 * Writes the mat out to the specified file
	 * Precondition: valid filePathAndName ending in .jpg
	 * @param filePathAndName
	 * @throws Exception
	 */
	public File writeToFile(String filePathAndName) throws Exception {
		MatOfByte matOfByte = new MatOfByte();
		Imgcodecs.imencode(".jpg", this, matOfByte);
		BufferedImage bufImage = null;
		byte[] byteArray = matOfByte.toArray();
		File destFile = new File(filePathAndName);
		InputStream in = new ByteArrayInputStream(byteArray);
		bufImage = ImageIO.read(in);
		System.out.println("Writing ImageMat out to file: " + destFile.getAbsolutePath());
		ImageIO.write(bufImage, "JPG", destFile);
		return destFile;
	}

	public void print(String printingName){
		System.out.println("Printing " + printingName + " mat");
		System.out.println("Rows: " + this.rows() + ", Cols: " + this.cols() +
				", Channels: " + this.channels() + ", Depth: " + this.depth());
		for(int row = 0; row < this.rows(); row++){
			for(int col = 0; col < this.cols(); col++){
				System.out.print("(" + row + "," + col + "): ");
				double[] elementDoubles = this.get(row, col);
				for(int doubleIndex = 0; doubleIndex < elementDoubles.length; doubleIndex++){
					System.out.print(elementDoubles[doubleIndex] + " ");
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	public double[] getVerticalHistogram() {
		return getVerticalDarkestNPixelsAvg(this.rows());
	}

	/**
	 * Gets a vertical histogram of the pixels, but only for the average of the
	 * darkest n pixels
	 *
	 * @param n
	 * @return
	 */
	public double[] getVerticalDarkestNPixelsAvg(int n) {
		double[] result = new double[this.cols()];
		for(int col = 0; col < result.length; col++){
			double[] allValuesInCol = new double[this.rows()];
			for(int row = 0; row < this.rows(); row++){
				double pixelValue = UtilityMethods.getAvg(this.get(row, col));
				allValuesInCol[row] = pixelValue;
			}
			Arrays.sort(allValuesInCol);
			double[] topN = Arrays.copyOfRange(allValuesInCol, 0, n);
			result[col] = UtilityMethods.getAvg(topN);
		}
		return result;
	}

	public boolean rowContainsNonWhitePixels(int rowNum){
		return getNumNonWhitePixelsInRow(rowNum) >= 2;
	}

	public int[] getNumNonWhitePixelsInAllRows() {
		int[] result = new int[this.rows()];
		for(int i = 0; i < this.rows(); i++){
			result[i] = getNumNonWhitePixelsInRow(i);
		}
		return result;
	}

	public int getNumNonWhitePixelsInRow(int rowNum){
		int count = 0;
		for(int col = 0; col < this.cols(); col++){
			double pixelValue = UtilityMethods.getAvg(this.get(rowNum, col));
			if(pixelValue < Const.HISTOGRAM_BACKGROUND_PIXEL_THRESHOLD){
				count++;
			}
		}
		return count;
	}

	public double[] getHorizontalHistogram() {
		double[] result = new double[this.rows()];
		for(int row = 0; row < result.length; row++){
			double[] allValuesInRow = new double[this.cols()];
			for(int col = 0; col < this.cols(); col++){
				double pixelValue = UtilityMethods.getAvg(this.get(row, col));
				allValuesInRow[col] = pixelValue;
			}
			result[row] = UtilityMethods.getAvg(allValuesInRow);
		}
		return result;
	}

	/**
	 * Return all the contour points found in the image
	 *
	 * @return
	 */
	public List<MatOfPoint> getContourPoints() {
		//reverse image to white text on black
		Mat img_thresh = new Mat();
		Imgproc.threshold( this, img_thresh, Const.CONTOUR_BACKGROUND_PIXEL_THRESHOLD,
				255, Imgproc.THRESH_BINARY_INV );

		//find contours of white (character) regions
		Mat img_contours = new Mat();
		img_thresh.copyTo( img_contours );
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.cvtColor( img_contours, img_contours, Imgproc.COLOR_RGB2GRAY);

		Imgproc.findContours( img_contours, contours, new Mat(),
				Imgproc.RETR_EXTERNAL,
				Imgproc.CHAIN_APPROX_NONE );

		return contours;
	}

	private ImageMat getInterestingPixels(MatOfPoint pointMat) {
		ImageMat interestingPixels = new ImageMat(Mat.zeros(this.rows(), this.cols(), this.type()));
		Point[] contourPoints = pointMat.toArray();
		for(int i = 0; i < contourPoints.length; i++){
			interestingPixels.put((int)contourPoints[i].y, (int) contourPoints[i].x,
					new byte[]{(byte)255,(byte)255,(byte)255});
		}
		List<MatOfPoint> pointMats = new ArrayList<>();
		pointMats.add(pointMat);
		Imgproc.fillPoly(interestingPixels, pointMats, new Scalar(255,255,255));
		return interestingPixels;
	}

	public void addNoise(int maxNoise) {
		for(int i = 0; i < this.rows(); i++){
			for(int j = 0; j < this.cols(); j++){
				int noise = (int)(Math.random() * 2 * maxNoise) - maxNoise;
				double[] values = this.get(i, j);
				for(int valIndex = 0; valIndex < values.length; valIndex++){
					values[valIndex] = clamp(values[valIndex] + noise);
				}
				this.put(i, j, values);
			}
		}
		//System.out.println("adding noise took " + (System.currentTimeMillis() - start) + "ms");
	}
	private double clamp(double pixelVal){
		if(pixelVal > 255){
			pixelVal = 255;
		}
		else if(pixelVal < 0){
			pixelVal = 0;
		}
		return pixelVal;
	}

	public List<MatOfPoint> getOrderedContourPoints() {
		List<MatOfPoint> result = getContourPoints();
		Collections.sort(result, UtilityMethods.getMatOfPointHorizontalComparator());
		return result;
	}

	public static ImageMat getAllWhiteImage(int rows, int cols) {
		double[] allWhite = new double[]{255,255,255};
		Mat baseMat = new Mat(rows, cols,Const.IMAGE_MAT_TYPE, new Scalar(allWhite));
		return new ImageMat(baseMat);
	}


	public ImageMat whitenAllOtherPointsToNewImage(List<Point> allKeepPoints) {
		ImageMat newImage = getAllWhiteImage(this.rows(), this.cols());
		for(Point keepPoint : allKeepPoints){
			newImage.put((int) keepPoint.y, (int) keepPoint.x,
					this.get((int)keepPoint.y,(int)keepPoint.x));
		}
		return newImage;
	}

	public List<Point> getAllPointsWithinContour(MatOfPoint contour) {
		ImageMat interestingPixels = getInterestingPixels(contour);
		List<Point> result = new ArrayList<>();
		Rect bounds = Imgproc.boundingRect(contour);
		//only look at the area within the bounds
		for(int i = 0; i < this.rows(); i++){
			for(int j = bounds.x; j < bounds.x + bounds.width; j++){
				if(interestingPixels.get(i, j)[0] != 0){
					result.add(new Point(j, i));
				}
			}
		}
		return result;
	}
}
