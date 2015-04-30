package core;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Color {
	
	public static final Color black = new Color(new double[] {0,0,0});
	private double[] color;
	
	public Color(double[] color) {
		this.color = color;
	}
	public int channels() {
		return color.length;
	}
	public double[] get() {
		return color;
	}
	private Color operate(Color color2, BiFunction<Double, Double, Double> op) {
		if (color2.channels() > channels()) { // Promover a tres canales.
			Color c = new Color(new double[] {op.apply(color[0], color2.color[0]), 
								op.apply(color[0], color2.color[1]), 
								op.apply(color[0], color2.color[2])
			});
			return c;
		} else if (channels() > color2.channels()) {
			Color c = new Color(new double[] {op.apply(color[0], color2.color[0]), 
								op.apply(color[1], color2.color[0]), 
								op.apply(color[2], color2.color[0])
			});
			return c;
		} else {
			Color c = new Color(new double[color.length]);
			for (int k = 0; k < color.length; k++) {
				c.color[k] = op.apply(color[k], color2.color[k]);
			}
			return c;
		}
	}
	public Color operate(Function<Double, Double> op) {
		Color c = new Color(Arrays.copyOf(color, color.length));
		for (int k = 0; k < channels(); k++) {
			c.color[k] = op.apply(c.color[k]);
		}
		return c;
	}
	public Color add(Color color2) {
		return operate(color2, (c1, c2)-> c1+c2);
	}
	public Color multiply(Color color2) {
		return operate(color2, (c1, c2) -> c1*c2);
	}
	public Color multiply(double c) {
		return operate((c1) -> c1*c);
	}
	public Color sub(Color color2) {
		return operate(color2, (c1, c2) -> c1-c2);
	}
	public Color clone() {
		return new Color(Arrays.copyOf(color, color.length));
	}
	public Color abs() {
		Color c = operate((v)-> Math.abs(v));
		return c;
	}
	public String toString() {
		return Arrays.toString(color);
	}
}
