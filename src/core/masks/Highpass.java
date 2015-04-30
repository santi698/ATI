package core.masks;


public class Highpass extends Mask {
	private int size;
	public Highpass(int size) {
		this.size = size;
	}
	@Override
	public double get(int x, int y) {
		if (x == size/2 && y == size/2)
			return size*size-1;
		return -1;
	}
	public int getSize() {
		return size;
	}

}
