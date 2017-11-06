package utility;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import imageMat.ImageAndText;
import imageMat.ImageMat;

public class FileUtility {

	/**
	 * Reads all text files from a given directory with a give file name base
	 * and converts them to an array of chars, corresponding to the ImageMat[] generated
	 * @param directory
	 * @param fileNameBase
	 * @param ocrChars all chars to be recognized
	 * @param numFiles
	 * @return
	 */
	public static char[] getAllCharValuesFromDir(String directory, String fileNameBase, char[] ocrChars, int numFiles) {
		char[] result = null;

		for(int i = 0; i < numFiles; i++){
			String fileName = directory + "/" + fileNameBase + i + ".txt";
			File file = new File(fileName);
			char[] charValuesOnPage = readCharsFromTextFile(file, ocrChars);
			result = UtilityMethods.combineArrays(result, charValuesOnPage);
		}
		return result;
	}

	/**
	 * Converts a text file into an array of characters
	 * NOTE: Does not include new line characters, should only be characters
	 * in the specified set of OCR characters
	 *
	 * @param inputTextFile text file to read chars from
	 * @return an array of characters in the file, not including the new line
	 */
	public static char[] readCharsFromTextFile(File inputTextFile, char[] ocrChars) {
		System.out.println("Reading in characters from " + inputTextFile.getAbsolutePath());
		try {
			int totalCharCount = 0;
			BufferedReader read = new BufferedReader(new FileReader(inputTextFile));

			String fileLine = read.readLine();
			List<String> lineList = new ArrayList<String>();
			while(fileLine != null){
				lineList.add(fileLine);
				totalCharCount += fileLine.length();
				fileLine = read.readLine();
			}
			char[] result = new char[totalCharCount];
			int charResultIndex = 0;
			for(String lineInList : lineList){
				for(int i = 0; i < lineInList.length(); i++){
					char lineChar = lineInList.charAt(i);
					if(UtilityMethods.charArrayContainsChar(ocrChars, lineChar)){
						result[charResultIndex] = lineChar;
						charResultIndex++;
					}
					else {
						read.close();
						throw new Exception ("Character '" + lineChar + "' not found in ocrChars");
					}
				}
			}
			read.close();
			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeImagesToFiles(ImageAndText[] matsAndText, String destDir) throws Exception {
		File newDir = new File(destDir);
		if(!newDir.exists()){
			newDir.mkdir();
		}
		for(int i = 0; i < matsAndText.length; i++){
			String fileName = destDir + "/" + i + "_" + matsAndText[i].toFileName()+ ".jpg";
			matsAndText[i].image.writeToFile(fileName);
		}

	}

	public static BufferedImage convertPDFFileToBufImage(File file) throws Exception{
		return convertTo3ByteBGRType(convertPDFFileToImages(file, 1)[0]);
	}

	private static BufferedImage convertTo3ByteBGRType(BufferedImage bufferedImage) {
		BufferedImage result = new BufferedImage(bufferedImage.getWidth(),
				bufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		result.getGraphics().drawImage(bufferedImage, 0, 0, null);
		return result;
	}

	/**
	 * Writes a single array of ImageMat objects out to image files
	 *
	 * @param mats Array of image files stored in ImageMats
	 * @param destDir directory
	 * @param fileNameBase Base of file names (e.g. rowNum_) -> rowNum_0.jpg, rowNum_1.jpg...
	 * @throws Exception
	 */
	public static void writeImagesToFiles(ImageMat[] mats, String destDir, String fileNameBase) throws Exception {
		File newDir = new File(destDir);
		if(!newDir.exists()){
			newDir.mkdir();
		}
		for(int i = 0; i < mats.length; i++){
			String fileName = destDir + "/" + fileNameBase  + i + ".jpg";
			mats[i].writeToFile(fileName);
		}
	}

	public static BufferedImage[] convertPDFFileToImages(File sourcePDFFile, int numPages)
			throws Exception {
		PDDocument document = PDDocument.load(sourcePDFFile);
		PDFRenderer render = new PDFRenderer(document);
		BufferedImage[] result = new BufferedImage[numPages];

		for(int i = 0; i < result.length; i++){
			result[i] = render.renderImage(i);
		}
		document.close();
		return result;
	}

}
