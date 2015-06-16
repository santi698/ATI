package core.hough;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import javafx.geometry.Point3D;

import org.opencv.core.Mat;

public class HoughTridimensional {
	private Map<Point3D, HoughResults3D> results;
	private BiFunction<Point, Point3D, Double> f;
	private double minX;
	private double maxX;
	private double minY;
	private double xStep;
	private double yStep;
	private double maxY;
	private double EPS = 4*Math.sqrt(2);
	private int maxVotes = 0;
	private double minZ;
	private double maxZ;
	private double zStep;
	public HoughTridimensional(BiFunction<Point, Point3D, Double> f, double minX, double maxX, double xStep, double minY, double maxY, double yStep, double minZ, double maxZ, double zStep) {
		this.f = f;
		this.minX = minX;
		this.maxX = maxX;
		this.xStep = xStep;
		this.minY = minY;
		this.maxY = maxY;
		this.yStep = yStep;
		this.minZ = minZ;
		this.maxZ = maxZ;
		this.zStep = zStep;
		this.results = new ConcurrentHashMap<Point3D, HoughResults3D>();
	}

	public void computeResults(Mat img) {
		
		assert(img.channels() == 1);
		
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double color = img.get(j, i)[0];
				Point position = new Point(i,j);
				processPixel(color, position);
			}
		}
	}

	private void processPixel(double color, Point position) {
		for (double x = minX; x <= maxX; x+=xStep) {
			for(double y = minY; y <= maxY; y+=yStep) {
				for (double z = minZ; z <= maxZ; z+=zStep) {
					Point3D parameters = new Point3D(x, y, z);
					if (color > 0.5) {
						double lineSimilarity = f.apply(position, parameters);
						if (lineSimilarity < EPS) {
							if (!results.containsKey(parameters))
								results.put(parameters, new HoughResults3D(parameters));
							results.get(parameters).vote(position);
							if (results.get(parameters).getVotes() > maxVotes)
								maxVotes = results.get(parameters).getVotes();
						}
					}
				}
			}
		}
	}
	public List<Point3D> getDetected (BiPredicate<HoughResults3D, Integer> condition) {
		List<Point3D> passingTuples = new LinkedList<Point3D>();
		results.forEach( (point,result)-> {
			if (condition.test(result, maxVotes))
				passingTuples.add(point);
		});
		return passingTuples;
	}
	public List<Point> getPassingPoints(BiPredicate<HoughResults3D, Integer> condition) {
		List<Point> passingPoints = new LinkedList<Point>();
		results.forEach( (point,result)-> {
			if (condition.test(result, maxVotes))
				passingPoints.addAll(result.getPoints());
		});
		return passingPoints;
	}
}
