package core.masks;


public class Means extends Mask {
	private int size;
	public Means(int size) {
		this.size  = size;
	}
	@Override
	public double get(int x, int y) {
		return 1d/(size*size);
	}
	public int getSize() {
		return size;
	}
}
