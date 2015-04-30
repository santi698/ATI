package core.operations;

import core.Color;

public class BinaryMultiplication extends BinaryOperation {
	@Override
	public Color apply(Color color1, Color color2) {
		return color1.multiply(color2);
	}
}
