package core.operations;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import core.Color;

public abstract class BinaryOperation {
	public abstract Color apply(Color color1, Color color2);
	public Mat apply(Mat img1, Mat img2) {
		if (!img1.size().equals(img2.size())) {
			throw new RuntimeException();
		}
		Mat result = new Mat(img1.height(), img1.width(), CvType.CV_32SC(img1.channels()));
		for (int i = 0; i < img1.width(); i++) {
			for (int j = 0; j < img1.height(); j++) {
				Color c1 = new Color(img1.get(j, i));
				Color c2 = new Color(img2.get(j, i));
				result.put(j, i, apply(c1, c2).get());
			}
		}
		return result;
	}
}
