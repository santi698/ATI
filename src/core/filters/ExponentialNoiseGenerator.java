package core.filters;

import java.util.Random;

public class ExponentialNoiseGenerator implements NoiseGenerator {
	private Random rnd;
	private double lambda;
	private static double rndExp(double value, double lambda) {
		return (-1/lambda)*Math.log(value);
	}
	public ExponentialNoiseGenerator(double lambda) {
		this.rnd = new Random();
		this.lambda = lambda;
	}
	@Override
	public double generate() {
		return rndExp(rnd.nextDouble(), lambda);
	}

}
