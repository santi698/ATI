package core.filters;

import java.util.Random;

public class SaltAndPepperNoiseFilter extends MCPunctualFilter {
	private double p0;
	private double p1;
	private Random rnd;
	
	public SaltAndPepperNoiseFilter(double saltAmount, double pepperAmount) {
		this.rnd = new Random();
		this.p0 = saltAmount;
		this.p1 = 1-pepperAmount;
	}

	@Override
	public double[] apply(double[] value) {
		double rndValue = rnd.nextDouble();
		return (rndValue<p0)?new double[]{255,255,255}:(rndValue>p1)?new double[]{0,0,0}:value;
	}

}
