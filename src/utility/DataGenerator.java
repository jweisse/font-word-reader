package utility;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import imageMat.ImageMat;
import imageMat.ImageMatProcessing;

public class DataGenerator {

	public static void generateImageAndTextFilesFromPDF() throws Exception {
		String outputDir = Const.BASE_DIR + "train_images/";
		String pdfFileName = Const.BASE_DIR + "source_files/sourceFile.pdf";
		String textFileName = Const.BASE_DIR + "source_files/sourceFile.txt";

		UtilityMethods.createAllDirs(outputDir);
		File sourcePDFFile = new File(pdfFileName);
		int numPages = Const.NUM_PAGES;
		BufferedImage[] allImages = FileUtility.convertPDFFileToImages(sourcePDFFile, numPages);
		for(int i = 0; i < allImages.length; i++){
			BufferedImage image = allImages[i];
			File outputfile = new File(outputDir + "img"+ i +".jpg");
			ImageIO.write(image, "jpg", outputfile);
			System.out.println("Image Created -> "+ outputfile.getName());
		}
		generateTextFiles(outputDir, textFileName, numPages);

	}

	private static void generateTextFiles(String imageDir, String textFileName, int numPages) throws Exception {

		String[] allLines = getAllLinesFromFile(textFileName);
		List<String> linesList = Arrays.asList(allLines);
		int currentLine = 0;

		for(int i = 0; i < numPages; i++){
			ImageMat imageMat = new ImageMat(imageDir + "img" + i + ".jpg");
			ImageMatProcessing imgProc = new ImageMatProcessing(imageMat);
			int numRows = imgProc.extractRowsUsingPixelData().length;
			createNewTextFile(imageDir, i, linesList.subList(currentLine, currentLine + numRows));
			currentLine += numRows;
		}

	}

	private static void createNewTextFile(String baseDir, int i, List<String> rowsToWriteOut)
			throws Exception {
		File newTextFile = new File(baseDir + "/img" + i + ".txt");
		BufferedWriter write = new BufferedWriter(new FileWriter(newTextFile));
		for(int j = 0; j < rowsToWriteOut.size(); j++){
			write.write(rowsToWriteOut.get(j));
			write.newLine();
		}
		write.flush();
		write.close();
		System.out.println("New text file created: " + newTextFile.getAbsolutePath());
	}

	/**
	 * Reads in all lines from a text file and converts the lines to an array of Strings
	 *
	 * @param sourceTextFilePathAndName
	 * @return
	 */
	private static String[] getAllLinesFromFile(String sourceTextFilePathAndName) {

		File file = new File(sourceTextFilePathAndName);
		BufferedReader read;
		try {
			read = new BufferedReader(new FileReader(file));
			ArrayList<String> lineList = new ArrayList<String>();
			String line = read.readLine();
			while(line != null){
				lineList.add(line);
				line = read.readLine();
			}
			read.close();
			String[] result = new String[lineList.size()];
			lineList.toArray(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
