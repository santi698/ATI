package core.hough;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import org.opencv.core.Mat;

import javafx.geometry.Point2D;

public class Hough {
	HashMap<Point2D, HoughResults> results;
	private BiFunction<Double, Double, Double> f;
	private double minX;
	private double maxX;
	private double minY;
	private double xStep;
	private double yStep;
	private double maxY;
	private double EPS;
	private int maxVotes = 0;
	
	public Hough(BiFunction<Double, Double, Double> f, double minX, double maxX, double xStep, double minY, double maxY, double yStep) {
		this.f = f;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.xStep = xStep;
		this.yStep = yStep;
	}

	public void computeResults(Mat img) {
		
		assert(img.channels() == 1);
		
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				for (double x = minX; x < maxX; x+=xStep) {
					for(double y = minY; y < maxY; y+=yStep) {
						Point2D point = new Point2D(x, y);
						double color = img.get(j, i)[0];
						if (color < 0.5) {
							if (f.apply(x, y) - color < EPS) {
								if (!results.containsKey(point))
									results.put(point, new HoughResults());
								results.get(point).vote(new Point2D(i,j));
								if (results.get(point).getVotes() > maxVotes)
									maxVotes = results.get(point).getVotes();
							}
						}
					}
						
				}
			}
		}
	}
	public List<Point2D> getDetected (BiPredicate<HoughResults, Integer> condition) {
		List<Point2D> passingTuples = new LinkedList<Point2D>();
		results.forEach((point,result)-> {
			if (condition.test(result, maxVotes))
				passingTuples.add(point);
		});
		return passingTuples;
	}
}
