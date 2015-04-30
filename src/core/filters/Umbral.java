package core.filters;


public class Umbral extends PunctualFilter {
	private double umbral;
	public Umbral(double umbral) {
		this.umbral = umbral;
	}
	@Override
	public double apply(double value) {
		return (value<umbral)?0:255;
	}

}
