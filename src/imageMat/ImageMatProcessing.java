package imageMat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import utility.Const;
import utility.UtilityMethods;


/**
 * Class used to Break up image files into rows and segment into
 * individual character ImageMats
 * @author OD00147
 *
 */
public class ImageMatProcessing {

	private ImageMat image;

	public ImageMatProcessing(ImageMat image){
		this.image = image;
	}

	public ImageMatProcessing(File file){
		this.image = new ImageMat( Imgcodecs.imread( file.getAbsolutePath() ));
	}

	public ImageMat getImage(){
		return image;
	}


	/**
	 * Converts the image to an array of "row" images represented by ImageMats
	 * Done by using histogram analysis to find the pixel rows with non white pixels
	 * in them
	 *
	 * @return array of rows in the image represented as ImageMats
	 */
	public ImageMat[] extractRowsUsingPixelData(){
		PixelLocation[] rowPixelLocations = getRowPixelLocations();
		ImageMat[] images = new ImageMat[rowPixelLocations.length];
		for(int i = 0; i < rowPixelLocations.length; i++){
			int pixelStart = rowPixelLocations[i].start;
			int pixelEnd = rowPixelLocations[i].end;

			//add 1 to include the end pixel
			Rect bound = new Rect( 0, pixelStart, this.image.cols(), pixelEnd - pixelStart + 1 );
			images[i] = new ImageMat( image, bound );
		}

		return images;
	}

	private PixelLocation[] getRowPixelLocations() {
		PixelLocation[] spaceRowPixelLocs = getSpaceRowPixelLocs(image);

		//note: spaceRowPixelLocs will always have a space at the begging and end
		PixelLocation[] rowPixelLocations = new PixelLocation[spaceRowPixelLocs.length - 1];
		for(int i = 0; i < rowPixelLocations.length; i++){
			int startIndex = spaceRowPixelLocs[i].end;
			int endIndex = spaceRowPixelLocs[i+1].start;
			rowPixelLocations[i] = new PixelLocation(startIndex, endIndex);
		}
		return rowPixelLocations;
	}

	private static PixelLocation[] getSpaceRowPixelLocs(ImageMat image) {
		int numPixelRows = image.rows();
		ArrayList<PixelLocation> spaceRowPixelLocs = new ArrayList<PixelLocation>();

		//get any empty space at end
		int pixelIndex = numPixelRows -1;
		while(!image.rowContainsNonWhitePixels(pixelIndex)){
			pixelIndex--;
		}
		PixelLocation finalSpaceRowLoc = new PixelLocation(pixelIndex, numPixelRows - 1);

		//get any empty space at beginning
		pixelIndex = 0;
		while(!image.rowContainsNonWhitePixels(pixelIndex)){
			pixelIndex++;
		}
		spaceRowPixelLocs.add(new PixelLocation(0, pixelIndex));

		int consecutiveSpaceCount = 0;
		while(pixelIndex < finalSpaceRowLoc.start){
			if(!image.rowContainsNonWhitePixels(pixelIndex)){
				consecutiveSpaceCount++;
			}
			else {
				if(consecutiveSpaceCount >= Const.CONSECUTIVE_COL_SPACE_THRESHOLD){
					int start = pixelIndex - consecutiveSpaceCount;
					int end = pixelIndex;
					spaceRowPixelLocs.add(new PixelLocation(start, end));
				}
				consecutiveSpaceCount = 0;
			}
			pixelIndex++;
		}
		spaceRowPixelLocs.add(finalSpaceRowLoc);
		PixelLocation[] result = new PixelLocation[spaceRowPixelLocs.size()];
		return spaceRowPixelLocs.toArray(result);
	}

	/**
	 * Segments entire page into character images and their associated text
	 * @param textFile
	 * @param charSizes
	 * @return
	 * @throws Exception
	 */
	public ImageAndText[] extractAllCharImages(File textFile) throws Exception {

		ImageMat[] rows = extractRowsUsingPixelData();
		String[] charTruths = UtilityMethods.parseTextFileToStringArray(textFile);
		List<ImageAndText> charImageList = new ArrayList<>();

		for(int i = 0; i < rows.length; i++){
			ImageMat row = rows[i];
			String rowText = charTruths[i];
			List<ImageAndText> rowChars = extractCharsFromRow(rowText, row);
			charImageList.addAll(rowChars);
		}
		return charImageList.toArray(new ImageAndText[0]);
	}


	private List<ImageAndText> extractCharsFromRow(String rowText, ImageMat rowImage) throws Exception {
		ImageMat[] rowImages = extractNonSpaceImagesFromRow(rowImage);
		char[] nonSpaceCharacters = extractNonSpaceCharsFromString(rowText);
		List<ImageAndText> result = new ArrayList<>();

		for(int i = 0; i < rowImages.length; i++){
			result.add(new ImageAndText(rowImages[i], "" + nonSpaceCharacters[i]));
		}
		return result;
	}

	private char[] extractNonSpaceCharsFromString(String rowText) {
		String[] words =  rowText.split("\\s+");
		int size = 0;
		for(String word : words){
			size+= word.length();
		}
		char[] result = new char[size];
		int index = 0;
		for(String word : words){
			for(int charIndex = 0; charIndex < word.length(); charIndex++){
				result[index] = word.charAt(charIndex);
				index++;
			}
		}
		return result;
	}

	public static ImageMat[] extractCharImagesFromRowSpacesAsNulls(ImageMat rowImage){
		ImageMat[] result = extractSegmentedImagesFromRow(rowImage).toArray(new ImageMat[0]);
		for(int i = 0; i < result.length; i++){
			if(isWhiteSpaceImage(result[i])){
				result[i] = null;
			}
		}
		return result;
	}

	private static boolean isWhiteSpaceImage(ImageMat charImage) {
		List<MatOfPoint> contourPoints = charImage.getContourPoints();
		if(contourPoints != null && contourPoints.size() > 0){
			//check to make sure the edge of a character didn't get clipped in a space
			int maxSize = -1;
			for(MatOfPoint pointMat: contourPoints){
				int size = pointMat.toList().size();
				if(size > maxSize){
					maxSize = size;
				}
			}
			if(maxSize > 5){
				return false;
			}
		}
		return true;
	}

	private static List<ImageMat> extractSegmentedImagesFromRow(ImageMat rowImage){
		List<ImageMat> result = new ArrayList<>();
		for(int i = 0; i < Const.CHARS_PER_ROW; i++){
			int boundsX = (int)(Const.RIGHT_MARGIN_OFFSET + i * Const.CHAR_WIDTH);
			int boundsWidth = (int) (Math.round(Const.CHAR_WIDTH));
			Rect bounds = new Rect(boundsX, 0, boundsWidth, rowImage.rows());
			ImageMat charImage = new ImageMat(rowImage, bounds);

			List<MatOfPoint> contours = charImage.getContourPoints();
			if(contours.size() > 0){
				bounds = UtilityMethods.getBoundingRectFromMulitpleContours(contours);
				charImage = new ImageMat(charImage, bounds);
			}
			result.add(charImage);
		}

		return result;
	}

	public static ImageMat[] extractNonSpaceImagesFromRow(ImageMat rowImage) {
		List<ImageMat> allSegmentedImages = extractSegmentedImagesFromRow(rowImage);
		allSegmentedImages.removeIf(m -> isWhiteSpaceImage(m));
		return allSegmentedImages.toArray(new ImageMat[0]);
	}

	public static ImageMat[] extractNonSpaceImagesFromRowOld(ImageMat rowImage) {
		List<ImageMat> result = new ArrayList<>();
		for(int i = 0; i < Const.CHARS_PER_ROW; i++){
			int boundsX = (int)(Const.RIGHT_MARGIN_OFFSET + i * Const.CHAR_WIDTH);
			int boundsWidth = (int) (Math.round(Const.CHAR_WIDTH));
			Rect bounds = new Rect(boundsX, 0, boundsWidth, rowImage.rows());
			ImageMat charImage = new ImageMat(rowImage, bounds);
			List<MatOfPoint> contourPoints = charImage.getContourPoints();
			if(contourPoints != null && contourPoints.size() > 0){
				//check to make sure the edge of a character didn't get clipped in a space
				int maxSize = -1;
				for(MatOfPoint pointMat: contourPoints){
					int size = pointMat.toList().size();
					if(size > maxSize){
						maxSize = size;
					}
				}
				if(maxSize > 5){
					result.add(charImage);
				}
			}
		}

		return result.toArray(new ImageMat[0]);
	}

	private static class PixelLocation{
		int start;
		int end;

		public PixelLocation(int s , int e){
			start = s;
			end = e;
		}

	}
}
