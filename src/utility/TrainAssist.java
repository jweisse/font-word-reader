package utility;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import imageMat.ImageAndText;
import imageMat.ImageMat;
import imageMat.ImageMatProcessing;

public class TrainAssist {


	public static void train(String neuralNetFileName)
			throws Exception{
		//runSegmentationTest();

		char[] ocrChars = Const.getOCRChars();
		long start = System.currentTimeMillis();
		ImageAndText[] allImagesAndText = getAllImagesAndText(ocrChars);
		System.out.print("Segmentation time : ");
		UtilityMethods.printOutElapsedTime(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();

		train(ocrChars, allImagesAndText, neuralNetFileName);

		System.out.print("Train time : ");
		UtilityMethods.printOutElapsedTime(System.currentTimeMillis() - start);
	}


	private static void train(char[] ocrChars, ImageAndText[] allImagesAndText,
			String neuralNetFileName) throws Exception {
		int trainArraySize = (int) (allImagesAndText.length * 0.8);
		ImageMat[] allTrainImageMats = new ImageMat[trainArraySize];
		char[] allTrainCharValues = new char[trainArraySize];
		ImageMat[] allTestImageMats = new ImageMat[allImagesAndText.length - trainArraySize];
		char[] allTestCharValues = new char[allImagesAndText.length - trainArraySize];
		int trainIndex = 0;
		int testIndex = 0;

		for(int i = 0; i < allImagesAndText.length; i++){
			if(testIndex < allTestImageMats.length && Math.random() < 0.25){
				allTestImageMats[testIndex] = allImagesAndText[i].image;
				allTestCharValues[testIndex] = allImagesAndText[i].text.charAt(0);
				testIndex++;
			}
			else {
				allTrainImageMats[trainIndex] = allImagesAndText[i].image;
				allTrainCharValues[trainIndex] = allImagesAndText[i].text.charAt(0);
				trainIndex++;
			}
		}

		NeuralNetHelper neuralNetHelper = new NeuralNetHelper(allTrainImageMats,
				allTrainCharValues, allTestImageMats, allTestCharValues, ocrChars);

		trainOneNeuralNet(neuralNetHelper);
		//trainMultipleNeuralNets(neuralNetHelper);

	}


	private static void trainMultipleNeuralNets(NeuralNetHelper neuralNetHelper) throws Exception {
		File outputFile = new File(Const.BASE_DIR + "results.txt");
		String saveNetFileDir = Const.BASE_DIR + "Saved_Neural_Nets/";
		UtilityMethods.createAllDirs(saveNetFileDir);

		int[] hiddenNodesArray = {91, 170};
		int[] epochArray = {200, 500, 1000};
		int[] trainImgArray = {10000, 15000, 23000, 35000};

		for(int hiddenLayerIndex = 0; hiddenLayerIndex < hiddenNodesArray.length; hiddenLayerIndex++){
			for(int trainImgIndex = 0; trainImgIndex < trainImgArray.length; trainImgIndex++){
				for(int epochIndex = 0; epochIndex < epochArray.length; epochIndex++){
					long start = System.currentTimeMillis();
					int numEpochs = epochArray[epochIndex];
					int numTrainImages = trainImgArray[trainImgIndex];
					int hiddenLayerNodes = hiddenNodesArray[hiddenLayerIndex];

					String saveName = saveNetFileDir + "network_hL" + hiddenLayerNodes + "_epoch" +
							numEpochs + "_numTrainImg" + numTrainImages + ".dat";

					double accuracy = neuralNetHelper.trainAndTestNeuralNet(
							hiddenLayerNodes, numEpochs, numTrainImages, saveName);
					double pdfAccuracy = neuralNetHelper.testOnPDFFile();
					String desc = String.format("HiddenLayers = %s, "
							+ " # epochs = %s, # train imgs = %s ", hiddenLayerNodes, numEpochs, numTrainImages);
					String outputString = desc + "total accuracy = " + accuracy +
							", pdfFileAccuracy = " + pdfAccuracy;
					BufferedWriter write = new BufferedWriter(new FileWriter(outputFile, true));
					write.write(outputString);
					write.newLine();
					write.flush();
					write.close();


					System.out.print("Train and test time : ");
					UtilityMethods.printOutElapsedTime(System.currentTimeMillis() - start);
				}
			}
		}

	}


	private static void trainOneNeuralNet(NeuralNetHelper neuralNetHelper) throws Exception {
		int numEpochs = 500;
		int numTrainImages = 30000;
		int hiddenLayerNodes = 100;

		neuralNetHelper.trainAndTestNeuralNet(
				hiddenLayerNodes, numEpochs, numTrainImages, Const.BASE_DIR + "neuralNet.dat");

	}


	private static ImageAndText[] getAllImagesAndText(char[] ocrChars) throws Exception {
		String baseDir = Const.BASE_DIR + "train_images/";
		int numImages = Const.NUM_PAGES;
		List<ImageAndText> allImages = new ArrayList<>();

		for(int i = 0; i < numImages; i++){
			String imageName = "img" + i;
			String inputImageFileName = baseDir + imageName + ".jpg";
			String inputTextFileName = baseDir + imageName + ".txt";
			File imageFile = new File(inputImageFileName);
			File textFile = new File(inputTextFileName);
			ImageMatProcessing imageProc = new ImageMatProcessing(imageFile);
			ImageAndText[] pageImages = imageProc.extractAllCharImages(textFile);
			List<ImageAndText> pageImageList = Arrays.asList(pageImages);
			allImages.addAll(pageImageList);
			System.out.println("Segmented " + inputImageFileName + ", found " +
					pageImages.length + " characters");
		}

		return allImages.toArray(new ImageAndText[0]);
	}


	private static void runSegmentationTest() throws Exception {
		String baseDir = Const.BASE_DIR + "train_images/";
		String imageName = "img4";

		String outputDir = Const.BASE_DIR + "Segmentation_Test/" + imageName;
		String inputImageFile = baseDir + imageName + ".jpg";
		String inputTextFile = baseDir + imageName + ".txt";
		long start = System.currentTimeMillis();
		runSegmentationTest(inputImageFile, inputTextFile, outputDir);
		System.out.println("time : " + (System.currentTimeMillis() - start) + "ms");

	}

	/**
	 * Segments one composite image into row images and individual char images.
	 * Prints these images out to outputDirName
	 *
	 * @param inputImageFileName
	 * @param outputDirName
	 * @param charBounds
	 * @throws Exception
	 */
	private static void runSegmentationTest(String inputImageFileName, String inputTextFileName,
			String outputDirName) throws Exception {
		File imageFile = new File(inputImageFileName);
		UtilityMethods.createAllDirs(outputDirName);
		ImageMatProcessing imageProc = new ImageMatProcessing(imageFile);
		ImageMat[] rowImages = imageProc.extractRowsUsingPixelData();
		FileUtility.writeImagesToFiles(rowImages, outputDirName, "rowNum");
		imageProc.getImage().writeToFile(outputDirName + "/image.jpg");

		File textFile = new File(inputTextFileName);
		UtilityMethods.createAllDirs(outputDirName + "/characters");

		ImageAndText[] charImages = imageProc.extractAllCharImages(textFile);
		FileUtility.writeImagesToFiles(charImages,outputDirName + "/characters");
	}


	/**
	 * Translates pixels from a specified BufferedImage into a String of text, based on the
	 * neural net contained in the specified neuralNetHelper instance
	 *
	 * @param image
	 * @param neuralNetHelper
	 * @return
	 * @throws Exception
	 */
	public static String getTextFromImage(BufferedImage image, NeuralNetHelper neuralNetHelper)
			throws Exception {
		ImageMat imageMat = new ImageMat(image);

		ImageMatProcessing imgProc = new ImageMatProcessing(imageMat);
		ImageMat[] rowImages = imgProc.extractRowsUsingPixelData();
		String result = "";
		for(int i = 0; i < rowImages.length; i++){
			String rowText = "";
			ImageMat[] charsInRow = ImageMatProcessing.extractCharImagesFromRowSpacesAsNulls(rowImages[i]);
			for(int j = 0; j < charsInRow.length; j++){
				if(charsInRow[j] == null){
					rowText += " ";
				}
				else {
					char predictedChar = neuralNetHelper.getPredictedChar(charsInRow[j]);
					rowText += predictedChar;
				}
			}
			result += rowText.trim() + "\n";
		}

		return result;
	}
}
