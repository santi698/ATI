package core.masks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public abstract class Mask {
	public abstract double get(int x, int y);
	public abstract int getSize();
	
	public Mat apply(Mat image) {
		ExecutorService ex = Executors.newFixedThreadPool(4);
		Mat result = image.clone();
		result.convertTo(result, CvType.CV_32SC(image.channels()));
		for (int i = 0; i < image.width(); i++) {
			for (int j = 0; j < image.height(); j++) {
				ex.execute(applyMask(image, i, j, result));
			}
		}
		try {
			ex.shutdown();
			ex.awaitTermination(100, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	private Runnable applyMask(Mat image, int i, int j, Mat result) {
		return () -> {
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
						resultColor[k]+=get(x, y)*color[k];
					}
				}
			}
			result.put(j, i, resultColor);
		};
	}
}