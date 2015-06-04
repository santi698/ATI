package core.masks;

import org.opencv.core.Mat;

public class Susan extends Mask
{
   private final double [][] susanMask;

    private final double min;
    private final double max;

    public Susan(final double min, final double max){
        double[][] mask = {	{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0},
        					{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
        					{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
        					{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
        					{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
        					{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
        					{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0}
		};
        susanMask = mask;
        this.min = min;
        this.max = max;
    }


    @Override
    public double get(int x, int y) {
        return susanMask[x+getSize()/2][y+getSize()/2];
    }

    @Override
    public int getSize() {
        return 7;
    }

    @Override
    protected Runnable applyMask(Mat image, int i, int j, Mat result) {
        return () -> {

            double nsum[] = new double[image.channels()];
            double[] originalColor = image.get(j,i);
            double[] resultColor = new double[image.channels()];
            for (int x = -getSize()/2; x <= getSize()/2; x++) {
                for (int y = -getSize()/2; y <= getSize()/2; y++) {
                    if(insideBound(i+x,j+y,image) && get(x, y) == 1) {
                        double[] localColor = image.get(j + y, i + x);
                        for (int h = 0; h < image.channels(); h++) {
                            if(Math.abs(originalColor[h] - localColor[h]) < 27d ){
                                nsum[h] += 1d;
                            }
                            //nsum[h] += Math.exp(-Math.pow((originalColor[h] - localColor[h]) / 27, 6));
                        }
                    }
                }
            }
            for(int h = 0; h<image.channels(); h++){
                double s = 1d - nsum[h]/37d;
                if(s >= min && s <= max){
                    resultColor[h] += 255;
                }else{
                    resultColor[h] = 0;
                }
            }
            result.put(j,i,resultColor);
        };
    }


    private boolean insideBound(int x, int y, Mat image){
        if(x>=0 && x < image.width() && y>=0 && y< image.height()){
            return true;
        }
        return false;
    }

}