package core.masks;



public class Gaussian extends Mask {
	private int size;
	private double sigma;
	private double sum;
	private int center;
	private double[][] matrix;
	public Gaussian(int size, double sigma) {
		this.size = size;
		this.sigma = sigma;
		this.center = size/2;
		this.matrix = new double[size][size];
		this.sum = 0;
		fillMatrix();
	}
	public void fillMatrix() {
		for (int i = -center; i < center+1; i++) {
			for(int j = -center; j < center+1; j++) {
				double value = (1/(2*Math.PI*sigma*sigma))*Math.exp(-(i*i+j*j)/(2*sigma*sigma));
				sum+=value;
				matrix[i+center][j+center] = value;
			}
		}
	}
	@Override
	public double get(int x, int y) {
		return matrix[x+center][y+center]/sum;
	}
	public int getSize() {
		return size;
	}
}