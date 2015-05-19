package controller;

import static core.Util.addExpNoise;
import static core.Util.addGaussianNoise;
import static core.Util.addRayleighNoise;
import static core.Util.addSaltAndPepper;
import static core.Util.anisotropicDiffusion;
import static core.Util.centeredCircle;
import static core.Util.centeredSquare;
import static core.Util.colorscale;
import static core.Util.compressRangeDynamic;
import static core.Util.compressRangeLinear;
import static core.Util.contrast;
import static core.Util.crop;
import static core.Util.detectBordersKirsch4;
import static core.Util.detectBordersLaplacian;
import static core.Util.detectBordersLaplacianOfGaussian;
import static core.Util.detectBordersPrewitt;
import static core.Util.detectBordersPrewitt4D;
import static core.Util.detectBordersSobel;
import static core.Util.detectBordersSobel4D;
import static core.Util.edgeDetectCanny;
import static core.Util.gaussianFilter;
import static core.Util.generateExponentialNoise;
import static core.Util.generateGaussianNoise;
import static core.Util.generateRayleighNoise;
import static core.Util.grayscale;
import static core.Util.highpassFilter;
import static core.Util.histogram;
import static core.Util.line;
import static core.Util.matToImage;
import static core.Util.meanFilter;
import static core.Util.medianFilter;
import static core.Util.monochrome;
import static core.Util.openRaw;
import static core.Util.umbralize;
import static core.Util.umbralizeOtsu;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Pair;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import application.Main;
import core.Util;
import core.border.BorderSegmentation;
import core.border.Intermediator;
import core.hough.Hough;
import core.masks.Susan;

public class RootLayoutController implements Initializable {
	@FXML
	private Menu editMenu;
	@FXML
	private Menu filterMenu;
	@FXML
	private AreaChart<Number, Number> histChart;
	private Mat image;
	private double[][] histogram;
	@FXML
	private ImageView imageView;
	@FXML
	private ImageView overlayImage;
	@FXML
	private Canvas overlayCanvas;
	@FXML
	private TextArea infoLabel;
	private ObjectProperty<Mat> imageProperty = new SimpleObjectProperty<Mat>();
	private ObjectProperty<Mat> overlayImageProperty = new SimpleObjectProperty<Mat>();
	
	private Main mainApp;
	@FXML
	private Rectangle selectionRectangle;
	private int selectionX1, selectionY1, selectionX2, selectionY2, startX, startY;
	
	LinkedList<Mat> undoList = new LinkedList<Mat>();

    private LinkedList<File> fileList = null;

	public void genCenteredCircle() {
		int radius = getParameter("Radio").intValue();
		if (radius > 0)
			showImage(centeredCircle(256, 256, radius, new double[]{0, 0, 0}, new double[]{255, 255, 255}));
	}
	public void genCenteredSquare() {
		int side = getParameter("Lado").intValue();
		if (side > 0)
			showImage(centeredSquare(256, 256, side, new double[] {0,0,0}, new double[] {255,255,255}));
	}
	public void genColorscale() {
		showImage(colorscale(256, 256));
	}
	public void genGrayscale() {
		showImage(grayscale(256,256));
	}

	public Number getParameter(String text) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setGraphic(null);
        dialog.setHeaderText("");
        dialog.setTitle("Ingrese el parámetro");
        dialog.setContentText(text);
        Optional<String> result =  dialog.showAndWait();
        if (result.isPresent())
        	return Double.valueOf(result.get());
        return -1;
	}
	public int[] getPositionAndColor() {
		// Create the custom dialog.
		Dialog<int[]> dialog = new Dialog<>();
		dialog.setTitle("Ingrese los parámetros");
		dialog.setHeaderText(null);

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Aceptar", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField field1 = new TextField();
		TextField field2 = new TextField();
		ColorPicker colorPicker = new ColorPicker();

		grid.add(new Label("X"), 0, 0);
		grid.add(field1, 1, 0);
		grid.add(new Label("Y"), 0, 1);
		grid.add(field2, 1, 1);
		grid.add(new Label("Color"), 0, 2);
		grid.add(colorPicker, 1, 2);

		Node acceptButton = dialog.getDialogPane().lookupButton(loginButtonType);
		acceptButton.setDisable(true);

		field1.textProperty().addListener((observable, oldValue, newValue) -> {
		    acceptButton.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> field1.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == loginButtonType) {
		    	Color c = colorPicker.getValue();
		    	int color = new java.awt.Color((int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)).getRGB();
		        return new int[]{Integer.valueOf(field1.getText()), Integer.valueOf(field2.getText()), color};
		    }
		    return null;
		});

		Optional<int[]> result = dialog.showAndWait();

		if (result.isPresent())
			return result.get();
		return null;
	}
	public Pair<Number, Number> getTwoParameters(String text1, String text2) {
		// Create the custom dialog.
		Dialog<Pair<Number, Number>> dialog = new Dialog<>();
		dialog.setTitle("Ingrese los parámetros");
		dialog.setHeaderText(null);

		// Set the button types.
		ButtonType loginButtonType = new ButtonType("Aceptar", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

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
		        return new Pair<>(Double.valueOf(field1.getText()), Double.valueOf(field2.getText()));
		    }
		    return null;
		});

		Optional<Pair<Number, Number>> result = dialog.showAndWait();

		if (result.isPresent())
			return result.get();
		return null;
	}
	public void handleAnisotropicDiffusion() {
		final CompletableFuture<Mat> task;
		Pair<Number, Number> parameters = getTwoParameters("T", "K");
		double k = parameters.getValue().doubleValue();
		int t = parameters.getKey().intValue();
		int func = getParameter("Ingrese la función (Leclerc = 1, Lorentz = 2, Cte = 3):").intValue();
		long time1 = System.currentTimeMillis();
		if (func == 1) { //Leclerc
			task = CompletableFuture.supplyAsync(()-> anisotropicDiffusion(image, (c)-> c.operate((v)-> Math.exp(-(v*v)/(k*k))), t));
		}
		else if (func == 2) { //Lorentz
			task = CompletableFuture.supplyAsync(()->anisotropicDiffusion(image, (c)-> c.operate((v)-> 1.0/((v*v)/(k*k)+1.0)), t));
			
		} else { //Isotropica
			task = CompletableFuture.supplyAsync(()->anisotropicDiffusion(image, (c)-> c.operate((v)-> 1.0), t));
		}
		task.thenAccept((img)->{Platform.runLater(()->showImage(img)); System.out.println("Computado en: " + (System.currentTimeMillis()-time1)/1000f + " segundos.");});
		
	}
	public void handleBinaryOperation() {
		BinaryOperationDialog dialog = new BinaryOperationDialog(image);
		dialog.showAndWait();
		if (dialog.wasAccepted())
			showImage(dialog.getResult());
	}
	public void handleClick(MouseEvent event) {
//		selectionRectangle.setVisible(false);
	}
	public void handleCrop() {
		if (selectionX1 == selectionX2 || selectionY1 == selectionY2) {
			showError("Debe seleccionar la imagen a recortar");
			return;
		}
		showImage(crop(image, selectionX1, selectionY1, selectionX2, selectionY2));
	}
	public void handleDrag(MouseEvent event) {
		if (event.getX() > imageView.getImage().getWidth() || event.getY()> imageView.getImage().getHeight())
			return;
		if (event.getX() > startX) {
			selectionX1 = startX;
			selectionX2 = (int) event.getX();
		} else {
			selectionX2 = startX;
			selectionX1 = (int) event.getX();
		}
		if (event.getY() > startY) {
			selectionY1 = startY;
			selectionY2 = (int)event.getY();
		} else {
			selectionY2 = startY;
			selectionY1 = (int) event.getY();
		}
		
		updateInfo(selectionX1, selectionY1, selectionX2, selectionY2);
		selectionRectangle.setLayoutX(selectionX1);
		selectionRectangle.setLayoutY(selectionY1);
		selectionRectangle.setWidth(selectionX2-selectionX1);
		selectionRectangle.setHeight(selectionY2-selectionY1);
		selectionRectangle.setVisible(true);
	}
	public void handleEdgeDetectCanny() {
		double sigma = getParameter("Sigma (SD)").doubleValue();
		showImage(edgeDetectCanny(image, sigma));
	}
	public void handleLineDetectHough() {
		Hough hough = new Hough(line, -Math.PI, Math.PI, Math.PI/100, 0, 200, 10);
		hough.computeResults(edgeDetectCanny(image, 10));
		List<Point2D> lines = hough.getDetected((res, max)-> res.getVotes()>0.3*max);
//		List<Point> points = hough.getPassingPoints((res, max)-> res.getVotes()>0.25*max);
//		setOverlayFromSet(points);
		for (Point2D line : lines)
			drawLinePolar(line.getX(), line.getY());
		
	}
	public void handleEnhanceContrast() {
		showImage(contrast(image, 100, 200, 1.2));
	}
	public void handleEqualize() {
		showImage(Util.equalize(image));
	}
	public void handleExponentialNoise() {
		double lambda = getParameter("Lambda").doubleValue();
		if (lambda > 0)
			showImage(compressRangeDynamic(addExpNoise(image, lambda)));
	}
	public void handleGaussianFilter() {
		Pair<Number, Number> result = getTwoParameters("Tamaño", "Sigma");
		if (result != null) {
			int size = result.getKey().intValue();
			double sigma = result.getValue().doubleValue();
			showImage(gaussianFilter(image, size, sigma));
		}
	}
	public void handleGaussianNoise() {
		double sigma = getParameter("Sigma").doubleValue();
		if (sigma > 0)
		showImage(addGaussianNoise(image, sigma));
	}
	public void handleGenExponentialNoise() {
		int width = 200;
		int height = 200;
		double lambda = getParameter("Lambda").doubleValue();
		if (lambda > 0)
			showImage(compressRangeDynamic(generateExponentialNoise(width, height, 1, lambda)));
	}
	public void handleGenGaussianNoise() {
		int width = 200;
		int height = 200;
		double sigma = getParameter("Sigma").doubleValue();
		if (sigma > 0)
			showImage(compressRangeLinear(generateGaussianNoise(width, height, 1, sigma)));
	}
	public void handleGenRayleighNoise() {
		int width = 200;
		int height = 200;
		double psi = getParameter("Psi").doubleValue();
		if (psi > 0)
			showImage(compressRangeDynamic(generateRayleighNoise(width, height, 1, psi)));
	}
	public void handleHighpassFilter() {
		int size = getParameter("Tamaño").intValue();
		if (size < 1) {
			showError("El tamaño debe ser positivo");
		}
		else if (size % 2 != 1) {
			showError("Ingrese un tamaño de máscara impar");
		}
		else
			showImage(highpassFilter(image, size));
	}
	public void handleKirsch4() {
		showImage(detectBordersKirsch4(image));
	}
	public void handleLaplacian() {
		double umbral = getParameter("Umbral").doubleValue();
		showImage(detectBordersLaplacian(image, umbral));
	}
	public void handleLaplacianOfGaussian() {
		Pair<Number, Number> parameters = getTwoParameters("Tamaño", "Umbral");
		int size = parameters.getKey().intValue();
		double threshold = parameters.getValue().doubleValue();
		showImage(detectBordersLaplacianOfGaussian(image, size, threshold));
	}
	public void handleLoad() {
		FileChooser chooser = new FileChooser();
		try {
			File file = chooser.showOpenDialog(mainApp.getPrimaryStage());
			if (file == null)
				return;
			String fileName = file.getName();
			String extension = "";
			int i = fileName.lastIndexOf('.');
			if (i >= 0)
			    extension = fileName.substring(i+1);
			Mat myPicture = null;
			if (extension.equalsIgnoreCase("raw")) {
				Pair<Number, Number> dimensions = getTwoParameters("Ancho", "Alto");
				if (dimensions != null)
					myPicture = openRaw(file, dimensions.getKey().intValue(), dimensions.getValue().intValue());
				else
					return;
			}
			else { 
				myPicture = Imgcodecs.imread(file.getAbsolutePath());
					if (myPicture.empty()) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");
						alert.setContentText("Formato no soportado.");
						alert.showAndWait();
						return;
					}
			}
			
			showImage(myPicture);
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}	
	}

    public void handleBorderFolderLoad(){
        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(mainApp.getPrimaryStage());
        if (file == null)
            return;
        if(file.isDirectory()){
            fileList = new LinkedList<>(Arrays.asList(file.listFiles()));
            setNextImage();
        }else{
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Formato no soportado.");
            alert.showAndWait();
            return;
        }

    }

    private Mat setNextImage(){
        if(fileList != null && !fileList.isEmpty()){
            Mat mat = Imgcodecs.imread(fileList.poll().getAbsolutePath());
            if(mat != null && !mat.empty()){
                showImage(mat);
                return mat;
            }else{
                return setNextImage();
            }
        }
        return null;
    }

    public void handleBorderVideoLoad(){
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(mainApp.getPrimaryStage());
        if (file == null)
            return;
        VideoCapture video = new VideoCapture();
        video.open(file.getAbsolutePath());
        Mat mat = new Mat();
        if (video.isOpened()) {
            video.read(mat);
            showImage(mat);
        }
    }

	public void handleMeanFilter() {
		int size = getParameter("Tamaño").intValue();
		showImage(meanFilter(image, size));
	}
	public void handleMedianFilter() {
		int size = getParameter("Tamaño").intValue();
		showImage(medianFilter(image, size));
	}
	public void handleMonochrome() {
		showImage(monochrome(image));
	}
	public void handleNegative() {
		showImage(Util.negative(image));
	}
	public void handleNew() {
		showImage(Mat.zeros(200, 200, CvType.CV_8UC1));
	}
	public void handlePress(MouseEvent event) {
		startX = (int) event.getX();
		startY = (int) event.getY();
	}
	public void handlePrewitt() {
		showImage(detectBordersPrewitt(image));
	}
	public void handlePrewitt4() {
		showImage(detectBordersPrewitt4D(image));
	}
	public void handleQuit() {
		System.exit(0);
	}
	public void handleRayleighNoise() {
		double psi = getParameter("Psi").doubleValue();
		if (psi > 0)
			showImage(compressRangeDynamic(addRayleighNoise(image, psi)));
	}
	public void handleSaltAndPepperNoise() {
		double s = 0.01;
		double p = 0.02;
		showImage(addSaltAndPepper(image, s, p));
	}
	public void handleSave() {
		FileChooser chooser = new FileChooser();
		ExtensionFilter imageFilter = new ExtensionFilter(
			    "Image files", ImageIO.getReaderFileSuffixes());
		chooser.getExtensionFilters().add(imageFilter);
		Image img = imageView.getImage();
		File file = chooser.showSaveDialog(mainApp.getPrimaryStage());
		if (file != null) {
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	public void handleSobel() {
		showImage(detectBordersSobel(image));
	}
	public void handleSobel4() {
		showImage(detectBordersSobel4D(image));
	}
	public void handleUmbralizeGlobal() {
		showImage(umbralize(image, histogram));
	}
	public void handleUmbralizeManual() {
		int umbral = getParameter("Umbral").intValue();
		if (umbral > 0)
			showImage(umbralize(image, umbral));
	}
	public void handleUmbralizeOtsu() {
		showImage(umbralizeOtsu(image, histogram));
	}
	public void handleUndo () {
		image = undoList.pop();
		showImage(image);
	}

    public void handleSusan(){
        Susan susan = new Susan(0.1);
        Mat result = susan.apply(image);
        showImage(result);
    }

    public void handleBorderSegmentation(){
    	Pair<Number, Number> parameters = getTwoParameters("Cantidad de iteraciones", "Sigma");
    	int iterations = parameters.getKey().intValue();
    	double sigma = parameters.getValue().doubleValue();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        BorderSegmentation segmentation = new BorderSegmentation(iterations, sigma,
                (Collection<Point> oBorder) -> setOverlayFromSet(oBorder),new Point(selectionX1,selectionY1),
                new Point(selectionX2,selectionY2));
        Intermediator interm = new Intermediator(segmentation, ()->setNextImage(), image);
        executor.submit(interm);
    }

    private void setOverlayFromSet(Collection<Point> points){
        Mat result = Mat.zeros(image.rows(),image.cols(),CvType.CV_8UC4);
        for(Point p: points){
            double[] vec = {255,255,0,255};
            result.put(p.y,p.x,vec);
        }
        overlayImageProperty.set(result);
    }

	public void setMainApp(Main main) {
		mainApp = main;
	}
	public void showError(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setContentText(message);
		alert.setHeaderText("");
		alert.showAndWait();
		return;
	}
	public void showImage(Mat img) {
		Platform.runLater(()-> {
			overlayImage.setImage(matToImage(Mat.zeros(1, 1, CvType.CV_8UC4)));
			selectionRectangle.setVisible(false);
			filterMenu.setDisable(false);
			editMenu.setDisable(false);
			histogram = histogram(img);
			Image fxImage = matToImage(img);
			imageView.setImage(fxImage);
			undoList.push(image);
			image = img;
			updateHistogram();
			if (undoList.size() > 20) 
				undoList.removeFirst();
		});
	}
	public void setOverlay(Mat img) {
		Platform.runLater(()-> {
			overlayImage.setImage(matToImage(img));
			overlayImage.resize(img.width(), img.height());
		});
	}
	public void showImageNewWindow(Mat img, String title) {
		//TODO
	}
	@SuppressWarnings("unchecked")
	private void updateHistogram() {
		NumberAxis xAxis = (NumberAxis) histChart.getXAxis();
		xAxis.setLowerBound(0);
		xAxis.setUpperBound(255);
		xAxis.setTickLength(1);
		xAxis.setTickUnit(1);
		histChart.getData().clear();
		if (image.channels() == 1) {
			XYChart.Series<Number, Number> data = new XYChart.Series<>();
			for (int i = 0; i < histogram.length; i++) {
				data.getData().add(new XYChart.Data<Number, Number>(i, histogram[i][0]*image.width()*image.height()));
			}
			histChart.getData().add(data);
		}
		else if (image.channels() == 3) {
			XYChart.Series<Number, Number> r = new XYChart.Series<>();
			XYChart.Series<Number, Number> g = new XYChart.Series<>();
			XYChart.Series<Number, Number> b = new XYChart.Series<>();
			for (int i = 0; i < histogram.length; i++) {
				r.getData().add(new XYChart.Data<Number, Number>(i, histogram[i][0]*image.width()*image.height()));
				g.getData().add(new XYChart.Data<Number, Number>(i, histogram[i][1]*image.width()*image.height()));
				b.getData().add(new XYChart.Data<Number, Number>(i, histogram[i][2]*image.width()*image.height()));
			}
			histChart.getData().addAll(r,g,b);
		}
		
        
    }
	public void updateInfo(int x1, int y1, int x2, int y2) {
		if (x1 != x2 || y1 != y2) {
			double meanValue = 0;
			double meanR = 0;
			double meanG = 0;
			double meanB = 0;
			if (x1 > x2) { int aux = x1; x1 = x2; x2 = aux;}
			if (y1 > y2) { int aux = y1; y1 = y2; y2 = aux;}
			int width = x2-x1; 
			int height = y2-y1;
			for (int x = x1; x < x2; x++) {
				for (int y = y1; y < y2; y++) {
					double[] c = image.get(y, x);
					Color color = null;
					if (c.length == 3) {
						color = new Color(c[2]/255, c[1]/255, c[0]/255, 1);
						meanR += c[2];
						meanG += c[1];
						meanB += c[0];
					}
					else {
						color = new Color(c[0]/255, c[0]/255, c[0]/255, 1);
						meanR += c[0];
						meanG += c[0];
						meanB += c[0];
					}
					meanValue += color.getBrightness();
				}
			}
			meanValue /= (width)*(height);
			meanR /= width*height;
			meanG /= width*height;
			meanB /= width*height;
			infoLabel.setText(
					  "Mean value: " + meanValue + 
					"\nMean red: " + meanR + 
					"\nMean green: " + meanG + 
					"\nMean blue: " + meanB);
		} else {
			double[] c = image.get(x1, y1);
			if (c.length == 3)
				infoLabel.setText("R: " + c[0] + "\nG: " + c[1] + "\nB:" + c[2] + "\nMean: " + ((c[0] + c[1] + c[2])/3));
			else		
				infoLabel.setText("R: " + c[0] + "\nG: " + c[0] + "\nB:" + c[0]);
		}
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageProperty.addListener((observable, before, after)-> {
			Platform.runLater(()->showImage(after));
		});
		overlayImageProperty.addListener((obs, o, n)->{
			Platform.runLater(()->setOverlay(n));
		});
		System.out.println(overlayCanvas.getGraphicsContext2D() == null);
	}
	public void drawLinePolar(double phi, double rad) {
		Point point1 = new Point(0, (int) (rad/Math.sin(phi)));
		Point point2 = new Point(image.width(), (int) ((rad-image.width()*Math.cos(phi))/Math.sin(phi)));
		System.out.println(point1 + " " + point2);
		GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
		gc.setStroke(Color.MAGENTA);
		gc.setLineWidth(1);
		gc.strokeLine(point1.x, point1.y, point2.x, point2.y);
	}


}
