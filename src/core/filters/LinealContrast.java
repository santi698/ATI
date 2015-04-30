package core.filters;


public class LinealContrast extends PunctualFilter {
	private double min;
	private double max;
	private double b;
	private double a;
	
	public LinealContrast(double min, double max, double amount) {
		super();
		this.min = min;
		this.max = max;
		this.b = amount;
		this.a = (255*255-(255*255-max*max)*b+min*min-max*max)/(min*min);
	}

	@Override
	public double apply(double value) {
		if (value < min)
			return a*value;
		if (value > max)
			return b*value;
		return value;
	}
}
