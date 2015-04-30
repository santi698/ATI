package core.filters;

import org.opencv.core.Mat;

public abstract class MCPunctualFilter {
	public abstract double[] apply(double[] color);
	public Mat apply(Mat img) {
		Mat destImg = new Mat(img.height(), img.width(), img.type());
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				destImg.put(j,i,apply(img.get(j,i)));
			}
		}
		return destImg;
	}
}
