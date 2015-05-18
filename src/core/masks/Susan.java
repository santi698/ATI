package core.masks;

import org.opencv.core.Mat;

public class Susan extends Mask
{
   private final double [][] susanMask;

    private final double error;

    public Susan(final double error){
        this.error = error;
        double[][] mask = {	{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0}, 
        					{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
        					{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
        					{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
        					{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0},
        					{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
        					{0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0}
		};
        susanMask = mask;
    }


    @Override
    public double get(int x, int y) {
        return susanMask[x][y];
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
            for (int x = -getSize()/2; x < getSize()/2+1; x++) {
                for (int y = -getSize()/2; y < getSize()/2+1; y++) {
                    if(insideBound(x,y,image) && get(x + getSize()/2, y + getSize()/2) != 0) {
                        double[] localColor = image.get(x + getSize()/2, y + getSize()/2);
                        for (int h = 0; h < image.channels(); h++) {
                            nsum[h] += Math.exp(-Math.pow((localColor[h] - originalColor[h]) / 27, 6));
                        }
                    }
                }
            }
            for(int h = 0; h<image.channels(); h++){
                double s = 1d - nsum[h]/37d;
                if(s >= 0.5 - error && s <= 0.5 + error){
                    resultColor[h] = 255;
                }else if(s >= 0.75 - error && s <= 0.75 + error){
                    resultColor[h] = 255;
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