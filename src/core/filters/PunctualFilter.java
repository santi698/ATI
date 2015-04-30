package core.filters;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

public abstract class PunctualFilter {
	final static Random rnd = new Random();
	public abstract double apply(double value);
	
	public static PunctualFilter compose(PunctualFilter f1, PunctualFilter f2) {
		return new PunctualFilter() {
			@Override
			public double apply(double value) {
				return f1.apply(f2.apply(value));
			}
		};
	}
	public Mat apply(Mat img) {
		return apply(img, 1);
	}
	public Mat apply(Mat img, double amount) {
		ExecutorService ex = Executors.newFixedThreadPool(4);
		Mat destImg = img.clone();
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				ex.execute(apply(i, j, destImg, amount));
			}
		}
		try {
			ex.shutdown();
			ex.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return destImg;
	}
	private Runnable apply(int x, int y, Mat img, double amount) {
		return () -> {
			if (rnd.nextDouble() < amount) {
				double[] color = img.get(y,x);
				for (int k = 0; k < img.channels(); k++) {
					color[k] = apply(color[k]);
				}
				img.put(y, x, color);
			}
			else {
				img.put(y, x, img.get(y, x));
			}
		};
	}
}