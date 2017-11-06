package application.view;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import application.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import utility.FileUtility;
import utility.UtilityMethods;

public class UploadPageController {

	@FXML
	private Button translateButton;

	@FXML
	private TextArea textArea;

	@FXML
	private StackPane leftStackPane;

	@FXML
	private BorderPane leftBorderPane;

	private MainApp mainApp;

	private BufferedImage bufImage;

	@FXML
	private void initialize() {
		Font font = new Font(textArea.getFont().getName(), 24);
		textArea.setFont(font);
	}


	@FXML
	private void uploadImageButtonPressed() throws Exception{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		Stage mainStage = mainApp.getPrimaryStage();
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("All Picture Files (*.jpg;*.pdf)", "*.jpg","*.pdf"),
				new ExtensionFilter("JPEG (*.jpg)","*.jpg"),
				new ExtensionFilter("PDF (*.pdf)", "*.pdf"));
		File selectedFile = fileChooser.showOpenDialog(mainStage);

		if(selectedFile != null){
			displayIamge(selectedFile);
			textArea.setVisible(false);
			translateButton.setVisible(true);
		}
	}

	private void displayIamge(File imageFile) throws Exception {
		String extension = UtilityMethods.getFileExtension(imageFile);
		if(extension.toUpperCase().equals("PDF")){
			this.bufImage = FileUtility.convertPDFFileToBufImage(imageFile);
		}
		else {
			this.bufImage = ImageIO.read(imageFile);
		}
		leftStackPane.getChildren().removeIf(x -> true);

    	Image image = UtilityMethods.convertBufImageToFXImage(this.bufImage);
    	ImageView imageView = new ImageView(image);
    	imageView.setPreserveRatio(true);

    	ScrollPane sp = new ScrollPane();
    	sp.setContent(imageView);
    	imageView.fitWidthProperty().bind(leftStackPane.widthProperty());
    	imageView.fitHeightProperty().bind(leftStackPane.heightProperty());
    	//imageView.setFitWidth(leftStackPane.getWidth());
    	//imageView.setFitHeight(leftStackPane.getHeight());

    	leftStackPane.getChildren().add(sp);

	}


	@FXML
	private void translateButtonPressed() throws Exception{
		String imageText = mainApp.getImageText(bufImage);
		textArea.setText(imageText);
		textArea.setVisible(true);
		translateButton.setVisible(false);
	}


	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;

	}

}
