package core.filters;


public class Negative extends PunctualFilter {

	@Override
	public double apply(double value) {
		return 255-value;
	}

}
