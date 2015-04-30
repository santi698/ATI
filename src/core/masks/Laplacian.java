package core.masks;

public class Laplacian extends Mask {
	private final static int[][] mask = {
		{0,1,0},
		{1,-4,1},
		{0,1,0}
	};
	@Override
	public double get(int x, int y) {
		return mask[y+1][x+1];
	}

	@Override
	public int getSize() {
		return 3;
	}

}
