package core.filters;


public class NoiseFilter extends PunctualFilter {
	public enum NoiseType {ADDITIVE, MULTIPLICATIVE};
	private NoiseType type;
	private NoiseGenerator noise;
	public NoiseFilter(NoiseGenerator noise, NoiseType type) {
		this.noise = noise;
		this.type = type;
	}
	@Override
	public double apply(double value) {
		switch (type) {
			case ADDITIVE: return value+noise.generate();
			case MULTIPLICATIVE: return value*noise.generate();
			default: return value;
		}
	}
}