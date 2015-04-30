package core.masks;

import java.util.HashMap;

import javafx.geometry.Point2D;

public class LaplacianOfGaussian extends Mask {
	private HashMap<Point2D, Double> preComputed = new HashMap<Point2D, Double>(); 
	private int size;
	private double sigma;
	public LaplacianOfGaussian(int size) {
		this.size = size;
		this.sigma = ((double)size-1)/6;
	}
	@Override
	public double get(int x, int y) {
		Point2D point = new Point2D(x, y);
		if (preComputed.containsKey(point)) {
			return preComputed.get(point);
		}
		double result = -1d/(Math.sqrt(Math.PI*2)*sigma*sigma*sigma)*(2d-((x*x+y*y)/(sigma*sigma)))*Math.exp(-(x*x+y*y)/(2d*sigma*sigma));
		preComputed.put(point, result);
		return result;
	}

	@Override
	public int getSize() {
		return size;
	}

}
