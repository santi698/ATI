package core;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import core.filters.ExponentialNoiseGenerator;
import core.filters.GaussianNoiseGenerator;
import core.filters.LinealContrast;
import core.filters.Negative;
import core.filters.NoiseFilter;
import core.filters.NoiseFilter.NoiseType;
import core.filters.RayleighNoiseGenerator;
import core.filters.SaltAndPepperNoiseFilter;
import core.filters.Umbral;
import core.masks.DirectionalMask.Direction;
import core.masks.Gaussian;
import core.masks.Highpass;
import core.masks.Kirsh;
import core.masks.Laplacian;
import core.masks.LaplacianOfGaussian;
import core.masks.Means;
import core.masks.Prewitt;
import core.masks.Sobel;
import core.masks.UnnamedMask;
import core.operations.BinaryAddition;
import core.operations.BinaryDifference;
import core.operations.BinaryMultiplication;

public class Util {
	public static BiFunction<Point, Point2D, Double> line = (position, parameters)-> {
		return Math.abs(position.x*Math.cos(parameters.getX())+position.y*Math.sin(parameters.getX())-parameters.getY());
	};
	public static BiFunction<Point, Point3D, Double> circle = (position, parameters)-> {
		return Math.abs(Math.pow((position.x-parameters.getX()),2) + Math.pow(position.y-parameters.getY(),2) - Math.pow(parameters.getZ(),2));
	};
	public static Mat add (Mat img1, Mat img2) {
		return compressRangeLinear(new BinaryAddition().apply(img1, img2));
	}
	
	public static Mat addExpNoise(Mat image, double lambda) {
		return new NoiseFilter(new ExponentialNoiseGenerator(lambda), NoiseType.MULTIPLICATIVE).apply(image);
	}
	
	public static Mat addGaussianNoise(Mat image, double sigma) {
		return new NoiseFilter(new GaussianNoiseGenerator(sigma), NoiseType.ADDITIVE).apply(image);
	}
	
	public static Mat addRayleighNoise(Mat image, double psi) {
		return new NoiseFilter(new RayleighNoiseGenerator(psi), NoiseType.MULTIPLICATIVE).apply(image);
	}
	
	public static Mat addSaltAndPepper(Mat image, double saltAmount, double pepperAmount) {
		return new SaltAndPepperNoiseFilter(saltAmount, pepperAmount).apply(image);
	}
	public static Mat anisotropicDiffusion(Mat image, Function<Color,Color> g, int t) {
		ExecutorService ex = Executors.newFixedThreadPool(4);
		if (t == 0) {
			return image;
		}
		image = image.clone();
		for (int i = 0; i < image.width(); i++) {
			for (int j = 0; j < image.height(); j++) {
				ex.execute(calculateAndPutColor(i, j, image, g));
			}
		}
		try {
			ex.shutdown();
			ex.awaitTermination(400, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return anisotropicDiffusion(image, g, t-1);
	}
	private static Runnable calculateAndPutColor(int i, int j, Mat nImage, Function<Color, Color> g) {
		return ()-> {
			Color color = new Color(nImage.get(j, i));
			Color dN, dS, dE, dW;
			if (j != 0)
				dN = new Color(nImage.get(j-1,i)).sub(color);
			else
				dN = color;
			if (j != nImage.height()-1)
				dS = new Color(nImage.get(j+1,i)).sub(color);
			else
				dS = color;
			if (i != nImage.width()-1)
				dE = new Color(nImage.get(j,i+1)).sub(color);
			else
				dE = color;
			if (j != 0) 
				dW = new Color(nImage.get(j,i-1)).sub(color);
			else
				dW = color;
			
			Color rN = dN.multiply(g.apply(dN.abs()));
			Color rS = dS.multiply(g.apply(dS.abs()));
			Color rW = dW.multiply(g.apply(dW.abs()));
			Color rE = dE.multiply(g.apply(dE.abs()));
			
			nImage.put(j, i, color.add(rN.add(rS).add(rW).add(rE).multiply(0.25)).get());
			};
	}
	public static double applyRC(double value, double min, double max) {
		return 255*(value-min)/max*Math.log(1+(value-min))/Math.log(1+(max-min));
	}
	
	public static Mat centeredCircle(int width, int height, int circleRadius, double[] bgColor, double[] circleColor) {
		Mat img = new Mat(height, width, CvType.CV_8UC3);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double distance = Math.sqrt(Math.pow(width/2-i, 2) + Math.pow(height/2-j,2));
				if (distance < circleRadius) {
					img.put(j, i, circleColor);
				} else {
					img.put(j, i, bgColor);
				}
			}
		}
		return img;
	}
	
	public static Mat centeredSquare(int width, int height, int side, double[] bgColor, double[] circleColor) {
		Mat img = new Mat(height, width, CvType.CV_8UC3);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (Math.abs(i-width/2) < side && Math.abs(j-height/2) < side) {
					img.put(j, i, circleColor);
				} else {
					img.put(j, i, bgColor);
				}
			}
		}
		return img;
	}
	
	public static Mat colorscale(int width, int height) {
		Mat img = new Mat(height, width, CvType.CV_8UC3);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double levelx = ((i/(double)width)*255);
				double levely = ((j/(double)height)*255);
				img.put(j, i, new double[]{levelx, levely, (levelx+levely)/2});
			}
		}
		return img;
	}
	public static Mat compressRangeDynamic(Mat img) {
		double[] max = new double[] {Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		double[] min = new double[] {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] color = img.get(i, j);
				if (color != null) {
					for (int k = 0; k < img.channels(); k++) {
						if (color[k] > max[k]) max[k] = color[k];
						if (color[k] < min[k]) min[k] = color[k];
					}
				}
			}
		}
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] color = img.get(i, j);
				if (color != null) {
					for (int k = 0; k < img.channels(); k++) {
						color[k] = applyRC(color[k], min[k], max[k]);
					}
					img.put(i, j, color);
				}
			}
		}
		return img;
	}
	public static Mat compressRangeLinear(Mat img) {
		img = img.clone();
		double[] max = new double[] {Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE};
		double[] min = new double[] {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] color = img.get(i, j);
				if (color != null) {
					for (int k = 0; k < img.channels(); k++) {
						if (color[k] > max[k]) max[k] = color[k];
						if (color[k] < min[k]) min[k] = color[k];
					}
				}
			}
		}
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] color = img.get(i, j);
				if (color != null) {
					for (int k = 0; k < img.channels(); k++) {
						color[k] = (color[k]-min[k])/(max[k]-min[k])*255;
					}
					img.put(i, j, color);
				}
			}
		}
		return img;
	}
	public static Mat contrast(Mat img, double min, double max, double amount) {
		return new LinealContrast(min, max, amount).apply(img);
	}
	public static Mat crop(Mat img, int x1, int y1, int x2, int y2) {
		return img.submat(y1, y2, x1, x2);
	}
	public static Mat detectBorderUnnamed4(Mat image) {
		return new UnnamedMask().apply4(image);
	}
	public static Mat detectBordersKirsch4(Mat image) {
		return new Kirsh().apply4(image);
	}
	public static Mat detectBordersLaplacian(Mat image, double threshold) {
		return compressRangeLinear(findzerocrosses(new Laplacian().apply(image), threshold));
	}
	public static Mat detectBordersLaplacianOfGaussian(Mat image, int size, double threshold) {
		return compressRangeLinear(findzerocrosses(new LaplacianOfGaussian(size).apply(image), threshold));
	}
	public static Mat detectBordersPrewitt(Mat image) {
		return new Prewitt().apply(image);
	}
	public static Mat detectBordersPrewitt4D(Mat image) {
		return new Prewitt().apply4(image);
	}

	public static Mat detectBordersSobel(Mat image) {
		return new Sobel().apply(image);
	}
	public static Mat detectBordersSobel4D(Mat image) {
		return new Sobel().apply4(image);
	}
	public static Mat editPixel(Mat img, int x, int y, double[] color) {
		img.put(y,x,color);
		return img;
	}
	public static Mat equalize(Mat img) {
		Mat img2 = new Mat(img.height(), img.width(), img.type());
		double[][] histogram = histogram(img);
		double [][] histCumSum = histogram;
		for (int i = 1; i < histogram.length; i++) {
			for(int j = 0; j < img.channels(); j++)
				histCumSum[i][j] += histCumSum[i-1][j];
		}
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] c = img.get(j, i);
				double[] newColor = new double[img.channels()];
				for (int k=0; k < img.channels(); k++) {
					newColor[k] = (int)((histCumSum[(int)(c[k])][k]-histCumSum[0][k])/(1-histCumSum[0][k])*255);
				}
				img2.put(j, i, newColor);
			}
		}
		return img2;
	}
	public static Mat gaussianFilter(Mat img, int size, double sigma) {
		return new Gaussian(size, sigma).apply(img);
	}
	public static Mat generateExponentialNoise(int width, int height, int channels, double lambda) {
		Mat result = Mat.ones(height, width, CvType.CV_32SC(channels));
		return addExpNoise(result, lambda);
	}
	public static Mat generateGaussianNoise(int width, int height, int channels, double sigma) {
		Mat result = Mat.zeros(height, width, CvType.CV_32SC(channels));
		return addGaussianNoise(result, sigma);
	}
	public static Mat generateRayleighNoise(int width, int height, int channels, double psi) {
		return addRayleighNoise(Mat.ones(height, width, CvType.CV_32SC(channels)), psi);
	}
	public static Mat[] getHSV(Mat img) {
		Mat imgH = new Mat(img.height(), img.width(), CvType.CV_8UC1);
		Mat imgS = new Mat(img.height(), img.width(), CvType.CV_8UC1);
		Mat imgV = new Mat(img.height(), img.width(), CvType.CV_8UC1);
		double h = 0;
		int width = img.width();
		int height = img.height();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double[] c = img.get(i, j);
				double r = c[0], g = c[1], b = c[2];
				double max = Math.max(c[0], Math.max(c[1], c[2]));
				double min = Math.min(c[0], Math.min(c[1], c[2]));
				double v = max;
				double delta = max-min;
				double s = ((max != 0)?(delta/max):0.0);
				if(s==0)
					h = 0;
				else {
					if (r == max)
					h = (g-b)/delta;
					else if (g==max)
					h = 2.0 + (b-r)/delta;
					else if (b==max)
					h = 4.0 + (r-g)/delta;
					h = h * 60.0;
					if (h<0.0) h = h + 360.0;
				}
				imgH.put(j, i, h);
				imgS.put(j, i, s*255);
				imgV.put(j, i, v);
			}
		}
		return new Mat[]{imgH, imgS, imgV};
	}
	public static Mat grayscale(int width, int height) {
		Mat img = new Mat(height, width, CvType.CV_8UC1);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				img.put(j, i, new double[] {(i/(double)width)*255});
			}
		}
		return img;
	}
	public static Mat highpassFilter(Mat img, int size) {
		return new Highpass(size).apply(img);
	}
	public static double[][] histogram(Mat img) {
		double[][] histogram = new double[256][img.channels()];
		int pixels = img.height()*img.width();
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] c = img.get(j, i);
				for (int k=0; k < img.channels(); k++) {
					if (c[k] > 255)
						System.out.println("Valor fuera de rango: " + c[k]);
					else
						histogram[(int)(c[k])][k] += 1d/pixels;
				}
			}
		}
		return histogram;
	}
	public static BufferedImage matToBufferedImage(Mat m){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() == 3 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else if (m.channels() == 4) {
        	type = BufferedImage.TYPE_4BYTE_ABGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte[] b = new byte[bufferSize];
        m.convertTo(m, CvType.CV_8UC(m.channels()));
        m.get(0,0,b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);  
        return image;
    }
	public static Image matToImage(Mat m) {
		return SwingFXUtils.toFXImage(matToBufferedImage(m),null);
	}
	public static Mat meanFilter(Mat img, int size) {
		return new Means(size).apply(img);
	}
	public static double median(double... ds) {
		Arrays.sort(ds);
		return ds[(int)Math.ceil(ds.length/2)];
	}
	public static Mat medianFilter(Mat image, int size) {
		Mat result = image.clone();
		for (int i = 0; i < image.width(); i++) {
			for (int j = 0; j < image.height(); j++) {
				double[] resultColor = new double[image.channels()];
				double[][] values = new double[image.channels()][size*size];
				for (int x = -size/2; x < size/2+1; x++) {
					for (int y = -size/2; y < size/2+1; y++) {
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
						if (color == null) continue;
						for(int k = 0; k < image.channels(); k++) {
							values[k][(x+size/2)+size*(y+size/2)]=color[k];
						}
					}
				}
				for (int k = 0; k < image.channels(); k++) {
					resultColor[k] = median(values[k]);
				}
				result.put(j, i, resultColor);
			}
		}
		return result;
	}
	public static Mat monochrome(Mat image) {
		return monochrome(image, 0.299, 0.587, 0.114);
	}
	public static Mat monochrome(Mat image, double r, double g, double b) {
		if (image.channels() < 3)
			return image;
		List<Mat> channels = new LinkedList<Mat>();
		Core.split(image, channels);
		Core.multiply(channels.get(0), new Scalar(b), channels.get(0));
		Core.multiply(channels.get(1), new Scalar(g), channels.get(1));
		Core.multiply(channels.get(2), new Scalar(r), channels.get(2));
		Core.add(channels.get(0), channels.get(1), channels.get(0));
		Core.add(channels.get(0), channels.get(2), channels.get(0));
		return channels.get(0);
	}
	public static Mat multiply(Mat img1, Mat img2) {
		return compressRangeDynamic(new BinaryMultiplication().apply(img1, img2));
	}
	public static Mat negative(Mat img) {
		return new Negative().apply(img);
	}
	public static Mat openRaw(File file, int width, int height) throws IOException {
		Mat img = new Mat (height, width, CvType.CV_8UC1);
		byte[] buffer = Files.readAllBytes(Paths.get(file.getPath()));
		img.put(0, 0, buffer);
		return img;
	}
	public static List<Mat> splitChannels (Mat img) {
		List<Mat> channels = new LinkedList<Mat>();
		Core.split(img, channels);
		return channels;
	}
	public static Mat sub (Mat img1, Mat img2) {
		return compressRangeLinear(new BinaryDifference().apply(img1, img2));
	}
	
	public static Mat umbralize(Mat img, int umbral) {
		return new Umbral(umbral).apply(img);
	}
	
	public static Mat umbralize(Mat img, double[][] histogram) {
		int total = img.width() * img.height();
		int umbral = 128;
		int iter = 1;
		double m1 = 0, m2 = 0;
		
		while (true) {
			m1=0;
			m2=0;
			double s1= 0, s2=0;
			for (int i = 0; i < histogram.length; i++) {
				if (i <= umbral) {
					s1 += histogram[i][0]*total;
					m1 += i*histogram[i][0]*total;
				}
				else {
					s2 += histogram[i][0]*total;
					m2 += i*histogram[i][0]*total;
				}
			}
			m1 /= s1;
			m2 /= s2;
			if ((int)(m1+m2)/2 == umbral) {
				break;
			}
			umbral = (int)(m1+m2)/2;
			iter++;
		}
		System.out.println(umbral + " en " + iter + " iteraciones.");
		return new Umbral(umbral).apply(img);
	}
	public static Mat umbralizeOtsu(Mat img, double[][] histogram) {
		double sum = 0;
		double total = img.width()*img.height();
		for (int i = 0; i < histogram.length; i++)
			sum += i * histogram[i][0]*total;
		double sumB = 0;

		double wB = 0;
		double wF = 0;
		double mB = 0;
		double mF = 0;
		double max = 0;
		double between = 0;
		double threshold = 0;
		for (int i = 0; i < histogram.length; i++) {
			wB += histogram[i][0] * total;
			if (wB == 0) continue;
			wF = total - wB;
			if (wF == 0) break;
			sumB += histogram[i][0]* i * total;
			mB = sumB/wB;
			mF = (sum - sumB)/wF;
			between = wB * wF * (mB - mF) * (mB - mF);
			if ( between >= max ) {
	            threshold = i;
	            max = between;            
	        }
		}
		return new Umbral(threshold).apply(img);
	}
	public static Mat findzerocrosses (Mat img, double slopeThreshold) {
		Mat res = new Mat(img.size(), CvType.CV_8UC1);
		for (int i = 0; i < img.width() - 1; i++) {
			for (int j = 0; j < img.height() - 1; j++) {
				double[] color = img.get(j,i);
				double[] colorS = img.get(j+1, i);
				double[] colorL = img.get(j, i-1);
				double[] colorR = img.get(j, i+1);
				double[] colorN = img.get(j+1, i);
				double[] resColor = new double[color.length];
				for (int k = 0; k < color.length; k++) {
					double sigC = Math.signum(color[k]);
					double sigR = Math.signum(colorR[k]);
					double sigS = Math.signum(colorS[k]);
					if ( sigC != 0 && (sigC == -sigS || sigR == -sigC)) {
						double slopeH, slopeV;
						if (sigC == -sigR)
							slopeH = Math.abs((color[k]) - (colorR[k]));
						else
							slopeH = 0;
						if (sigC == -sigS)
							slopeV = Math.abs((color[k]) - (colorS[k]));
						else
							slopeV = 0;
						resColor[k] = ((Math.max(slopeH, slopeV))>slopeThreshold)?255:0;
					} else if (sigC == 0){
						double sigL, sigN;
						if (colorL == null)
							sigL = 0;
						else
							sigL = Math.signum(colorL[k]);
						if (colorN == null)
							sigN = 0;
						else
							sigN = Math.signum(colorN[k]);
						if (sigL == -sigR || sigN == -sigS) {
							double slopeH, slopeV;
							if (sigL == -sigR)
								slopeH = Math.abs((colorL[k]) - (colorR[k]));
							else
								slopeH = 0;
							if (sigN == -sigS)
								slopeV = Math.abs((colorN[k]) - (colorS[k]));
							else
								slopeV = 0;
							resColor[k] = Math.max(slopeH, slopeV)>slopeThreshold?255:0;
						} else
							resColor[k] = 0;
					}
					
				}
				res.put(j, i, resColor);
			}
		}
		return res;
	}
	public static Mat hysteresisUmbralization(Mat img, double umbral1, double umbral2) {
		Mat result = compressRangeLinear(img);
		for (int i = 0; i < result.width(); i++) {
			for (int j = 0; j < result.height(); j++) {
				double[] center = result.get(j, i);
				List<double[]> neighbours = neighbours(result, new Point(i, j));
				for (int k = 0; k < center.length; k++) {
					if (center[k] < umbral2 && center[k] > umbral1) {
						boolean isIsolated = true;
						for (double[] color : neighbours) {
							if (color[k] > umbral1) {
								isIsolated = false;
								break;
							}
						}
						if (isIsolated)
							center[k] = 0;
						else
							center[k] = 255;
					} else if (center[k] < umbral1) center[k] = 0;
					else if (center[k] > umbral2) center[k] = 255;
				}
				img.put(j, i, center);
			}
		}
		return img;
	}
	public static Mat removeNonMaximums(Mat img, Mat xImg, Mat yImg) {
		Mat result = img.clone();
		for (int i = 0; i < img.width(); i++) {
			for (int j = 0; j < img.height(); j++) {
				double[] center = img.get(j, i);
				for (int k = 0; k < center.length; k++) {
					Point[] direction = getDirections(xImg, yImg, new Point(i,j));
					double[] color1 = img.get(j-direction[k].y, i-direction[k].x);
					if (color1 == null) color1 = new double[]{Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
					double[] color2 = img.get(j+direction[k].y, i+direction[k].x);
					if (color2 == null) color2 = new double[]{Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
					if (color1[k] > center[k]  || color2[k] > center[k])
						center[k] = 0;
				}
				result.put(j, i, center);
			}
		}
		return result;
	}
	private static Point[] getDirections(Mat xImg, Mat yImg, Point point) {		
		double[] yColor = yImg.get(point.y, point.x);
		double[] xColor = xImg.get(point.y, point.x);
		Point[] directions = new Point[yColor.length];
		for (int k = 0; k < yColor.length; k++) {
			double angle = Math.toDegrees(Math.atan(yColor[k]/xColor[k])+ Math.PI/2);
			if ((angle > 0 && angle < 22.5) || (angle > 157.5 && angle < 180))
				directions[k] = new Point(0,1);
			else if (angle > 22.5 && angle < 67.5)
				directions[k] = new Point(-1,1);
			else if (angle > 67.5 && angle < 112.5)
				directions[k] = new Point(1,0);
			else
				directions[k] = new Point(1,1);
		}
		return directions;
	}

	public static List<double[]> neighbours(Mat img, Point position) {
		List<double[]> neighbours = new LinkedList<double[]>();
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <=1; y++) {
				double[] color = img.get(position.y + y, position.x + x);
				if (!(x == 0 && y == 0) && color != null)
					neighbours.add(color);
			}
		}
		return neighbours;
	}
	public static Mat edgeDetectCanny(Mat img, double sigma, double umbral1, double umbral2) {
		Mat result = img.clone();
		if (sigma != 0)
			result = gaussianFilter(result, 3, sigma);
		Mat resultX = new Sobel().apply(result, Direction.HORIZONTAL);
		Mat resultY = new Sobel().apply(result, Direction.VERTICAL);
		result = removeNonMaximums(new Sobel().apply(result), resultX, resultY);
		result = hysteresisUmbralization(result, umbral1, umbral2);
		return result;
	}
}
