package core.filters;

import java.util.Random;

public class RayleighNoiseGenerator implements NoiseGenerator {
	private Random rnd;
	private double psi;
	private static double rayleigh(double value, double psi) {
		return psi*Math.sqrt(-2*Math.log(1-value));
	}
	public RayleighNoiseGenerator(double psi) {
		this.rnd = new Random();
		this.psi = psi;
	}
	@Override
	public double generate() {
		return rayleigh(rnd.nextDouble(), psi);
	}
	
}
