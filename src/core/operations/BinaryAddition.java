package core.operations;

import core.Color;

public class BinaryAddition extends BinaryOperation{
	@Override
	public Color apply(Color color1, Color color2) {
		return color1.add(color2);
	}
}
