package core.hough;

import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import javafx.geometry.Point2D;

import org.opencv.core.Mat;

public class Hough {
	Map<Point2D, HoughResults> results;
	private BiFunction<Point, Point2D, Double> f;
	private double minX;
	private double maxX;
	private double minY;
	private double xStep;
	private double yStep;
	private double maxY;
	private double EPS = 1/Math.sqrt(2);
	private int maxVotes = 0;
	public Hough(BiFunction<Point, Point2D, Double> f, double minX, double maxX, double xStep, double minY, double maxY, double yStep) {
		this.f = f;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.xStep = xStep;
		this.yStep = yStep;
		this.results = new ConcurrentHashMap<Point2D, HoughResults>();
	}

	public void computeResults(Mat img) {
		
		assert(img.channels() == 1);
		List<CompletableFuture<?>> futures = new LinkedList<CompletableFuture<?>>();
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double color = img.get(j, i)[0];
				Point position = new Point(i,j);
				futures.add(CompletableFuture.runAsync(processPixel(color, position)));
			}
		}
		for (CompletableFuture<?> future : futures) {
			try {
				future.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Runnable processPixel(double color, Point position) {
		return ()->{
			for (double x = minX; x <= maxX; x+=xStep) {
				for(double y = minY; y <= maxY; y+=yStep) {
					Point2D parameters = new Point2D(x, y);
					if (color > 0.5) {
						double lineSimilarity = f.apply(position, parameters);
						if (lineSimilarity < EPS) {
							if (!results.containsKey(parameters))
								results.put(parameters, new HoughResults(parameters));
							results.get(parameters).vote(position);
							if (results.get(parameters).getVotes() > maxVotes)
								maxVotes = results.get(parameters).getVotes();
						}
					}
				}
			}
		};
	}
	public List<Point2D> getDetected (BiPredicate<HoughResults, Integer> condition) {
		List<Point2D> passingTuples = new LinkedList<Point2D>();
		results.forEach( (point,result)-> {
			if (condition.test(result, maxVotes))
				passingTuples.add(point);
		});
		return passingTuples;
	}
	public Collection<HoughResults> getPassingResults (BiPredicate<HoughResults, Integer> condition) {
		Collection<HoughResults> collection = results.values();
		collection.removeIf((hr)->!condition.test(hr, maxVotes));
		return collection;
	}
	public List<Point> getPassingPoints(BiPredicate<HoughResults, Integer> condition) {
		List<Point> passingPoints = new LinkedList<Point>();
		results.forEach( (point,result)-> {
			if (condition.test(result, maxVotes))
				passingPoints.addAll(result.getPoints());
		});
		return passingPoints;
	}
}