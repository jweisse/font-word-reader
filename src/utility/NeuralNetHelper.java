package utility;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;

import imageMat.ImageAndText;
import imageMat.ImageMat;
import imageMat.ImageMatProcessing;

public class NeuralNetHelper {

	private ImageMat[] trainImageMats;
	private ImageMat[] testImageMats;
	private char[] trainCharValues;
	private char[] testCharValues;
	private char[] ocrChars;
	private BasicNetwork network;

	public NeuralNetHelper(ImageMat[] trainImageMats, char[] trainCharValues,
			ImageMat[] testImageMats, char[] testCharValues, char[] ocrChars) throws Exception {

		this.trainImageMats = trainImageMats;
		this.testImageMats = testImageMats;
		this.trainCharValues = trainCharValues;
		this.testCharValues = testCharValues;
		this.ocrChars = ocrChars;

		if(trainImageMats.length != trainCharValues.length ||
				testImageMats.length != testCharValues.length){
			throw new Exception("Error in char image array lengths");
		}
	}

	public NeuralNetHelper(String neuralNetFileName, char[] ocrChars){
		this.network = loadNetwork(neuralNetFileName);
		this.ocrChars = ocrChars;
	}

	public NeuralNetHelper(InputStream inputStream, char[] ocrChars) {
		this.network = loadNetwork(inputStream);
		this.ocrChars = ocrChars;
	}

	public double trainAndTestNeuralNet(int numHiddenLayerNodes, int numEpochs,
			String saveFileName) throws Exception{
		return trainAndTestNeuralNet(numHiddenLayerNodes, numEpochs,
				trainImageMats.length, saveFileName);
	}

	/**
	 * Trains a new instance of a neural net based on the parameters supplied and the data from the
	 * constructor.
	 * @param hiddenLayerNodes
	 * @param numEpochs
	 * @param numTrainImages
	 * @param saveFileName name of the file to save the resulting neural net. Must end in ".dat"
	 * @return
	 * @throws Exception
	 */
	public double trainAndTestNeuralNet(int hiddenLayerNodes, int numEpochs, int numTrainImages,
			String saveFileName)
			throws Exception {
		String resultsFileName = saveFileName.substring(0, saveFileName.length() - 4) + "_results.txt";
		System.out.println(String.format("\nTraining with hiddenLayers = %s, "
				+ " # epochs = %s, # train imgs = %s", hiddenLayerNodes, numEpochs, numTrainImages));

		//writeOutRandomTrainAndTestFiles();

		this.network = initializeBasicNetwork(hiddenLayerNodes);
		ImageMat[] trainImages = Arrays.copyOf(trainImageMats, numTrainImages);
		char[] trainChars = Arrays.copyOf(trainCharValues, numTrainImages);
		MLTrain train = getMLTrain(network, trainImages, trainChars);
		for (int i = 0; i < numEpochs; i++) {
			train.iteration();
		}
		double finalAccuracy = testNeuralNet(network, testImageMats, testCharValues, ocrChars,
				true, resultsFileName);
		//testOnPDFFile(network, ocrChars);
		saveNetwork(network, saveFileName);
		System.out.println("Saved neural network with accuracy of " +  finalAccuracy + "%" );

		return finalAccuracy;
	}


	private void writeOutRandomTrainAndTestFiles() throws Exception {
		int numToTest = 4;
		int width = Const.IMG_SIZE_WIDTH;
		int height = Const.IMG_SIZE_HEIGHT;

		System.out.println("train array length: " + trainImageMats.length);
		System.out.println("test array length: " + testImageMats.length);

		String fileBase = Const.BASE_DIR + "debug_images/";
		UtilityMethods.createAllDirs(fileBase);

		for(int i = 0; i < numToTest; i++){
			int trainIndex = (int) (Math.random() * trainImageMats.length);
			int testIndex = (int) (Math.random() * testImageMats.length);

			trainImageMats[trainIndex].writeResizedImageToFile(width, height,
					fileBase + "trainImage" + trainIndex + ".jpg");
			System.out.println("Train image " + trainIndex + " char: " + trainCharValues[trainIndex]);

			testImageMats[testIndex].writeResizedImageToFile(width, height,
					fileBase + "testImage" + testIndex + ".jpg");
			System.out.println("Test image " + testIndex + " char: " + testCharValues[testIndex]);
		}

	}

	private BasicNetwork initializeBasicNetwork(int numHiddenLayerNodes) {
		BasicNetwork network = new BasicNetwork();
		network.addLayer(new BasicLayer(null, true, Const.NUM_FEATURES));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), true, numHiddenLayerNodes ));
		network.addLayer(new BasicLayer(new ActivationSigmoid(), false, ocrChars.length));
		network.getStructure().finalizeStructure();
		network.reset();

		return network;
	}

	/**
	 * Tests input neural net using data supplied in the arguments
	 *
	 * @param network instance of Network to test
	 * @return percent accuracy of the neural net
	 */
	private static double testNeuralNet(BasicNetwork network, ImageMat[] testImageMats,
			char[] testCharValues, char[] ocrChars, boolean printCharAccuracy, String resultsFileName) {
		int numAccurate = 0;
		int numPossible = testImageMats.length;

		HashMap<Character, CharAccuracy> charAccuracyMap = new HashMap<>();
		for(int i = 0; i < testImageMats.length; i++){
			char predictedChar = getPredictedChar(testImageMats[i], network, ocrChars);
			char actualChar = testCharValues[i];
			if(predictedChar == actualChar){
				numAccurate++;
			}
			addToCharAccuracyMap(charAccuracyMap, predictedChar, actualChar);
		}
		double percentAccuracy = (numAccurate * 1.0) / (numPossible * 1.0) * 100.0;
		if(printCharAccuracy)
			printCharAccuracy(charAccuracyMap, ocrChars, percentAccuracy, resultsFileName);
		return percentAccuracy;
	}

	public double testOnPDFFile() throws Exception{
		return testOnPDFFile(this.network, this.ocrChars);
	}

	private static double testOnPDFFile(BasicNetwork network, char[] ocrChars) throws Exception {
		String base = Const.BASE_DIR + "sample_files/sample1";
		File file = new File(base +".pdf");
		File textFile = new File(base + ".txt");
		BufferedImage bufImage = FileUtility.convertPDFFileToBufImage(file);
		ImageMat imageMat = new ImageMat(bufImage);
		ImageMatProcessing imgProc = new ImageMatProcessing(imageMat);
		ImageAndText[] imageAndText = imgProc.extractAllCharImages(textFile);
		ImageMat[] images = new ImageMat[imageAndText.length];
		char[] chars = new char[imageAndText.length];
		for(int i = 0; i < images.length; i++){
			images[i] = imageAndText[i].image;
			chars[i] = imageAndText[i].text.charAt(0);
		}
		double percentAccuracy = testNeuralNet(network, images, chars, ocrChars, false, null);
		System.out.println("PDF File Accuracy: " + percentAccuracy + " %");
		return percentAccuracy;
	}

	private static void printCharAccuracy(HashMap<Character, CharAccuracy> charAccuracyMap, char[] ocrChars,
			double percentAccuracy, String resultsFileName) {
		try{
			File resultsFile = new File(resultsFileName);
			BufferedWriter write = new BufferedWriter(new FileWriter(resultsFile));
			System.out.println();
			UtilityMethods.writeLineToFileAndStandardOut("Total Accuracy: " + percentAccuracy + " %", write);
			UtilityMethods.writeLineToFileAndStandardOut("Accuracies of each character:", write);
			for(int i = 0; i < ocrChars.length; i++){
				if(charAccuracyMap.containsKey(new Character(ocrChars[i]))){
					CharAccuracy accuracy = charAccuracyMap.get(new Character(ocrChars[i]));
					double percent = ((accuracy.numAccurate + 0.0)/ accuracy.numTotal) * 100;
					UtilityMethods.writeLineToFileAndStandardOut("Accuracy for " + ocrChars[i] + ": " +
							percent + "% (" + accuracy.numAccurate + " of " + accuracy.numTotal + ")", write);
				}
				else {
					UtilityMethods.writeLineToFileAndStandardOut(ocrChars[i] + " not tested", write);
				}
			}
			write.flush();
			write.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}

	}

	private static void addToCharAccuracyMap(HashMap<Character, CharAccuracy> charAccuracyMap,
			char predictedChar,	char actualChar) {
		if(charAccuracyMap.containsKey(new Character(actualChar))){
			CharAccuracy charAccuracy = charAccuracyMap.get(new Character(actualChar));
			if(predictedChar == actualChar)
				charAccuracy.addAccuracy();
			else
				charAccuracy.addInaccuracy();
		}
		else {
			CharAccuracy charAccuracy = new CharAccuracy();
			if(predictedChar == actualChar)
				charAccuracy.addAccuracy();
			else
				charAccuracy.addInaccuracy();
			charAccuracyMap.put(new Character(actualChar), charAccuracy);
		}

	}

	private MLTrain getMLTrain(BasicNetwork network, ImageMat[] trainImages, char[] trainChars) {
		MLDataSet trainingSet = buildTrainingSet(trainImages, trainChars);
		MLTrain train = new ResilientPropagation(network, trainingSet);

		return train;
	}

	/**
	 * Saves an Encog neural network to file
	 * @param network
	 * @param string
	 */
	private void saveNetwork(BasicNetwork network, String fileName) {
		File file = new File(fileName);
		System.out.println("Saving network at location: " + file.getAbsolutePath());
		EncogDirectoryPersistence.saveObject(file, network);

	}

	/**
	 * Converts image files (in Mat form) and their corresponding character values
	 * into an MLDataSet to train on
	 *
	 * @param imageMats
	 * @param charValues
	 * @return
	 */
	private MLDataSet buildTrainingSet(ImageMat[] imageMats,
			char[] charValues) {

		double[][] inputVectors = new double[imageMats.length][];
		double[][] outputVectors = new double[charValues.length][];

		for(int i=0; i < inputVectors.length; i++){
			inputVectors[i] = imageMats[i].getNormalizedPixelData(Const.IMG_SIZE_WIDTH, Const.IMG_SIZE_HEIGHT);
			outputVectors[i] = convertCharToOutputVector(charValues[i]);
		}
		return new BasicMLDataSet(inputVectors, outputVectors);
	}

	/**
	 * Returns an array of 0s and 1s representing the output state of the particular
	 * character, i.e. where the character lies in the ocrChars array
	 * @param c
	 * @return
	 */
	private double[] convertCharToOutputVector(char c) {
		double[] ret = new double[ocrChars.length];
		for(int i = 0; i < ocrChars.length; i++){
			if(ocrChars[i] == c){
				ret[i] = 1;
			}
			else {
				ret[i] = 0;
			}
		}
		return ret;
	}

	/**
	 * Loads a network from file and predicts the character values of the images
	 * specified in the inputImages 2D array
	 *
	 * @param networkFileName
	 * @param inputImages
	 * @return
	 */
	public static String[] loadAndPredictChars(String networkFileName,
			ImageMat[][] inputImages, char[] ocrChars){
		String[] predictedChars = new String[inputImages.length];
		BasicNetwork network = loadNetwork(networkFileName);
		for(int i = 0; i < inputImages.length; i++){
			predictedChars[i] = "";
			for(int j = 0; j < inputImages[i].length; j++){
				predictedChars[i] += getPredictedChar(inputImages[i][j], network, ocrChars);
			}
		}

		return predictedChars;
	}

	private static BasicNetwork loadNetwork(String fileName){
		File file = new File(fileName);
		System.out.println("Loading network from " + file.getAbsolutePath());
		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(file);
		return network;
	}

	private static BasicNetwork loadNetwork(InputStream inputStream) {
		BasicNetwork network = (BasicNetwork) EncogDirectoryPersistence.loadObject(inputStream);
		return network;
	}

	/**
	 * Returns the predicted character based on the specified imageMat,
	 * using the saved BasicNetwork
	 * @param imageMat
	 * @return
	 * @throws Exception
	 */
	public char getPredictedChar(ImageMat imageMat) throws Exception{
		if(this.network == null){
			throw new Exception("Network not loaded");
		}
		return getPredictedChar(imageMat, this.network, ocrChars);
	}

	private static char getPredictedChar(ImageMat imageMat, BasicNetwork network,
			char[] ocrChars) {
		double[] inputVector = imageMat.getNormalizedPixelData(Const.IMG_SIZE_WIDTH,
				Const.IMG_SIZE_HEIGHT);
		double[] outputVector = new double[ocrChars.length];
		network.compute(inputVector, outputVector);
		int loc = UtilityMethods.getMaxLoc(outputVector);
		char predictedChar = ocrChars[loc];
		//double confidence = outputVector[loc];

		return predictedChar;
	}

	private static class CharAccuracy{
		private int numTotal;
		private int numAccurate;

		private CharAccuracy(){
			numTotal = 0;
			numAccurate = 0;
		}
		private void addAccuracy(){
			numTotal++;
			numAccurate++;
		}
		private void addInaccuracy(){
			numTotal++;
		}

	}
}
