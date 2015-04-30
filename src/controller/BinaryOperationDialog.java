package controller;

import static core.Util.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import core.Util;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;

public class BinaryOperationDialog extends Stage {
	private boolean accepted = false;
	@FXML
	private ImageView result;

	@FXML
	private ChoiceBox<String> operation;

	@FXML
	private ImageView img2;

	@FXML
	private ImageView img1;
	
	private Mat image1;
	private Mat image2;
	private Mat resultMat;

	public BinaryOperationDialog(Mat image1) {
	    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("BinaryOperationDialog.fxml"));
	    fxmlLoader.setController(this);
	    // Nice to have this in a load() method instead of constructor, but this seems to be the convention.
	    try
	    {
	        setScene(new Scene((Parent) fxmlLoader.load()));
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }
	    this.image1 = image1;
		img1.setImage(Util.matToImage(image1));
		operation.setItems(FXCollections.observableArrayList("Producto", "Suma", "Diferencia"));
		operation.getSelectionModel().select(0);
		operation.getSelectionModel().selectedIndexProperty().addListener((ov, value, newValue) -> {doOperation(newValue); result.setImage(matToImage(resultMat));});
	}
	public boolean wasAccepted() {
		return accepted;
	}
	public Mat getResult() {
		return resultMat;
	}
	@FXML
	private void handleCancel(ActionEvent event) {
		this.close();
	}

	@FXML
	private void handleAccept(ActionEvent event) {
		accepted = true;
		this.close();
	}

	@FXML
	private void loadImg2(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		try {
			File file = chooser.showOpenDialog(null);
			if (file == null)
				return;
			String fileName = file.getName();
			String extension = "";
			int i = fileName.lastIndexOf('.');
			if (i >= 0)
			    extension = fileName.substring(i+1);
			if (extension.equalsIgnoreCase("raw")) {
				Pair<Integer, Integer> dimensions = getTwoInts("Ancho", "Alto");
				if (dimensions != null)
					image2 = openRaw(file, dimensions.getKey(), dimensions.getValue());
				else
					return;
			}
			else { 
				image2 = Imgcodecs.imread(file.getAbsolutePath());
					if (image2.empty()) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");
						alert.setContentText("Formato no soportado.");
						alert.showAndWait();
						return;
					}
			}
			
			img2.setImage(matToImage(image2));
			int selected = operation.getSelectionModel().getSelectedIndex();
			doOperation(selected);
			if (resultMat != null)
				result.setImage(matToImage(resultMat));
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}	
	}
	@FXML
	public void initialize() {
	}
	private void doOperation(Number newValue) {
		switch (newValue.intValue()) {
		case 0:
			resultMat = multiply(image1, image2);
			break;
		case 1:
			resultMat = add(image1, image2);
			break;
		case 2:
			resultMat = sub(image1, image2);
		default:
			break;
		}
	}
	public Pair<Integer, Integer> getTwoInts(String text1, String text2) {
		// Create the custom dialog.
		Dialog<Pair<Integer, Integer>> dialog = new Dialog<>();
		dialog.setTitle("Ingrese los parámetros");
		dialog.setHeaderText(null);

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Aceptar", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField field1 = new TextField();
		TextField field2 = new TextField();

		grid.add(new Label(text1), 0, 0);
		grid.add(field1, 1, 0);
		grid.add(new Label(text2), 0, 1);
		grid.add(field2, 1, 1);

		Node acceptButton = dialog.getDialogPane().lookupButton(loginButtonType);
		acceptButton.setDisable(true);

		field1.textProperty().addListener((observable, oldValue, newValue) -> {
		    acceptButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> field1.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == loginButtonType) {
		        return new Pair<>(Integer.valueOf(field1.getText()), Integer.valueOf(field2.getText()));
		    }
		    return null;
		});

		Optional<Pair<Integer, Integer>> result = dialog.showAndWait();

		if (result.isPresent())
			return result.get();
		return null;
	}

}
