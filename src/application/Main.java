package application;
	
import java.io.IOException;

import org.opencv.core.Core;

import controller.RootLayoutController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	private Stage primaryStage;
	private Parent rootLayout;
	
	@Override
	public void start(Stage primaryStage) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		this.primaryStage = primaryStage;
		initRootLayout();
	}
	
	public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../controller/RootLayout.fxml"));
            rootLayout = (Parent) loader.load();
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);            
            primaryStage.setScene(scene);
            scene.getStylesheets().add("controller/chart.css");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	/**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public void setWorking() {
    	primaryStage.getScene().setCursor(Cursor.WAIT);
    }
    public void setIdle() {
    	primaryStage.getScene().setCursor(Cursor.DEFAULT);
    }
	public static void main(String[] args) {
		launch(args);
	}
}
