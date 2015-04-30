package core.filters;

import java.util.Random;

public class GaussianNoiseGenerator implements NoiseGenerator {
	private double sigma;
	private Random rnd;
	public GaussianNoiseGenerator(double sigma) {
		this.rnd = new Random();
		this.sigma = sigma;
	}
	private static double gauss(double value1, double value2, double sigma) {
		double result = Math.sqrt(-2*sigma*Math.log(value1))*Math.cos(2*Math.PI*value2);
		return result;
	}
	@Override
	public double generate() {
		return gauss(rnd.nextDouble(), rnd.nextDouble(), sigma);
	}
}