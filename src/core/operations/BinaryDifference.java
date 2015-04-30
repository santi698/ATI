package core.operations;

import core.Color;

public class BinaryDifference extends BinaryOperation{
	@Override
	public Color apply(Color color1, Color color2) {
		return color1.sub(color2);
	}
}
