/**
 * The main application for a Courier New size 36 font reader program.
 *
 * Author: Jonah Weisse
 */

package application;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Core;

import application.view.UploadPageController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utility.Const;
import utility.DataGenerator;
import utility.NeuralNetHelper;
import utility.TrainAssist;


public class MainApp extends Application {
	/**
	 * Note: if running in runnableJarMode, make sure openCV library dll
	 * and neuralNet.dat file are in main jar directory
	 */
	private static final boolean runnableJarMode = false;

	private Stage primaryStage;
	private NeuralNetHelper neuralNetHelper;

	public static void main(String[] args) throws Exception {
		loadOpenCVLibrary();

		//to train a new neural net, call train instead of launch
		launch(args);
		//train();
	}

	private static void loadOpenCVLibrary() throws Exception {
		if(runnableJarMode){
			InputStream inputStream = null;
			File fileOut = null;

			String osName = System.getProperty("os.name");
			if(osName.startsWith("Windows")) {
				inputStream = MainApp.class.getClassLoader().getResourceAsStream(
						Core.NATIVE_LIBRARY_NAME + ".dll");
				fileOut = File.createTempFile("lib", ".dll");
			}
			else {
				throw new Exception("Program only compatible with Windows 64 bit systems");
			}
			OutputStream out = FileUtils.openOutputStream(fileOut);
			IOUtils.copy(inputStream, out);
			inputStream.close();
			out.close();
			System.load(fileOut.toString());
		}
		else {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		}
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Font Reader");
		this.primaryStage.setMaximized(true);
		initUploadPage();
		initNeuralNetHelper();
	}

	private void initNeuralNetHelper() {
		if(runnableJarMode){
			InputStream inputStream = MainApp.class.getClassLoader().
					getResourceAsStream("neuralNet.dat");
			this.neuralNetHelper = new NeuralNetHelper(inputStream, Const.getOCRChars());
		}
		else {
			this.neuralNetHelper = new NeuralNetHelper("Neural_Net_Files/neuralNet.dat",
					Const.getOCRChars());
		}
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	private void initUploadPage() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/UploadPage.fxml"));
			BorderPane layout = (BorderPane) loader.load();
			Scene scene = new Scene(layout);
			primaryStage.setScene(scene);
			primaryStage.show();
			UploadPageController controller = loader.getController();
			controller.setMainApp(this);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Trains a new instance of a neural net, and saves the result and the accuracy output
	 * to files
	 *
	 * @throws Exception
	 */
	private static void train() throws Exception {
		DataGenerator.generateImageAndTextFilesFromPDF();

		TrainAssist.train("Neural_Net_Files/neuralNet.dat");

		System.exit(0);
	}

	public String getImageText(BufferedImage image) throws Exception {
		return TrainAssist.getTextFromImage(image, neuralNetHelper);
	}
}
