package core.masks;

import java.util.Arrays;

import org.opencv.core.Mat;

public abstract class DirectionalMask {
	public enum Direction {HORIZONTAL, VERTICAL, DIAG1, DIAG2}
	private static int[][] yMask;
	private static int[][] xMask;
	private static int[][] xyMask;
	private static int[][] yxMask;
	public double get(int x, int y, Direction direction){
		switch(direction) {
		case HORIZONTAL:
			return xMask[y+1][x+1];
		case VERTICAL:
			return yMask[y+1][x+1];
		case DIAG1:
			return xyMask[y+1][x+1];
		case DIAG2:
			return yxMask[y+1][x+1];
		}
		return 0;
	};
	public int getSize() {
		return 3;
	};
	protected DirectionalMask(int[] core_t, int[] core_c, int[] core_b) {
		yMask = new int[][]{
			{core_t[0], core_t[1], core_t[2]},
			{core_c[0], core_c[1], core_c[2]},
			{core_b[0], core_b[1], core_b[2]}
		};
		xMask = new int[][]{
			{core_t[0], core_c[0], core_b[0]},
			{core_t[1], core_c[1], core_b[1]},
			{core_t[2], core_c[2], core_b[2]}
		};
		xyMask = new int[][] {
			{core_c[0], core_t[0], core_t[1]},
			{core_b[0], core_c[1], core_t[2]},
			{core_b[1], core_b[2], core_c[2]}
		};
		yxMask = new int[][] {
				{core_t[1], core_t[2], core_c[2]},
				{core_t[0], core_c[1], core_b[2]},
				{core_c[0], core_b[0], core_b[1]}
		};
	}
	public Mat apply(Mat image) {
		Mat result = image.clone();
		for (int i = 0; i < image.width(); i++) {
			for (int j = 0; j < image.height(); j++) {
				double[] resultColorX = new double[image.channels()];
				double[] resultColorY = new double[image.channels()];
				for (int x = -getSize()/2; x < getSize()/2+1; x++) {
					for (int y = -getSize()/2; y < getSize()/2+1; y++) {
						double[] color = null;
						if ((i+x < 0 && j+y < 0) || (i+x < 0 && j+y > image.height()-1) || (i+x > image.width()-1 && j+y < 0) || (i+x>image.width()-1 && j+y > image.height()-1))
							color = new double[]{0,0,0};
						else if (i+x < 0)
							color = image.get(j+y, 0);
						else if (i+x > image.width()-1)
							color  = image.get(j+y, image.width()-1);
						else if (j+y < 0)
							color = image.get(0, i+x);
						else if (j+y > image.height()-1)
							color = image.get(image.height()-1, i+x);
						else
							color = image.get(j+y, i+x);
						for(int k = 0; k < image.channels(); k++) {
							resultColorX[k] += get(x, y, Direction.HORIZONTAL)*color[k];
							resultColorY[k] += get(x,y,Direction.VERTICAL)*color[k];
						}
					}
				}
				result.put(j, i, magnitude(resultColorX, resultColorY));
			}
		}
		return result;
	}
	public Mat apply4(Mat image) {
		Mat result = image.clone();
		for (int i = 0; i < image.width(); i++) {
			for (int j = 0; j < image.height(); j++) {
				double[] resultColorX = new double[image.channels()];
				double[] resultColorY = new double[image.channels()];
				double[] resultColorXY = new double[image.channels()];
				double[] resultColorYX = new double[image.channels()];
				for (int x = -getSize()/2; x < getSize()/2+1; x++) {
					for (int y = -getSize()/2; y < getSize()/2+1; y++) {
						double[] color = null;
						if ((i+x < 0 && j+y < 0) || (i+x < 0 && j+y > image.height()-1) || (i+x > image.width()-1 && j+y < 0) || (i+x>image.width()-1 && j+y > image.height()-1))
							color = new double[]{0,0,0};
						else if (i+x < 0)
							color = image.get(j+y, 0);
						else if (i+x > image.width()-1)
							color  = image.get(j+y, image.width()-1);
						else if (j+y < 0)
							color = image.get(0, i+x);
						else if (j+y > image.height()-1)
							color = image.get(image.height()-1, i+x);
						else
							color = image.get(j+y, i+x);
						for(int k = 0; k < image.channels(); k++) {
							resultColorX[k] += get(x, y, Direction.HORIZONTAL)*color[k];
							resultColorY[k] += get(x,y,Direction.VERTICAL)*color[k];
							resultColorXY[k] += get(x,y,Direction.DIAG1)*color[k];
							resultColorYX[k] += get(x,y,Direction.DIAG2)*color[k];
						}
					}
				}
				result.put(j, i, max(resultColorX, resultColorY, resultColorXY, resultColorYX));
			}
		}
		return result;
	}
	public Mat apply(Mat image, Direction direction) {
		Mat result = image.clone();
		for (int i = 0; i < image.width(); i++) {
			for (int j = 0; j < image.height(); j++) {
				double[] resultColor = new double[image.channels()];
				for (int x = -getSize()/2; x < getSize()/2+1; x++) {
					for (int y = -getSize()/2; y < getSize()/2+1; y++) {
						double[] color = null;
						if ((i+x < 0 && j+y < 0) || (i+x < 0 && j+y > image.height()-1) || (i+x > image.width()-1 && j+y < 0) || (i+x>image.width()-1 && j+y > image.height()-1))
							color = new double[]{0,0,0};
						else if (i+x < 0)
							color = image.get(j+y, 0);
						else if (i+x > image.width()-1)
							color  = image.get(j+y, image.width()-1);
						else if (j+y < 0)
							color = image.get(0, i+x);
						else if (j+y > image.height()-1)
							color = image.get(image.height()-1, i+x);
						else
							color = image.get(j+y, i+x);
						for(int k = 0; k < image.channels(); k++) {
							resultColor[k] += get(x, y, direction)*color[k];
						}
					}
				}
				result.put(j, i, resultColor);
			}
		}
		return result;
	}
	private double[] magnitude(double[] resultColorX, double[] resultColorY) {
		double [] result = new double[resultColorX.length];
		for (int k = 0; k < result.length; k++) {
			result[k] = Math.sqrt(Math.pow(resultColorX[k], 2) + Math.pow(resultColorY[k], 2));
		}
		return result;
	}
	private double[] max(double[]... colors ){
		double[] max = new double[colors[0].length];
		Arrays.fill(max, Double.MIN_VALUE);
		for (double [] color : colors) {
			for (int k = 0; k < color.length; k++) {
				double component = color[k];
				if (component > max[k]) {
					max[k] = component;
				}
			}
		}
		return max;
	}
}
