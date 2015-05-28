package core.border;

import java.awt.Point;

import core.masks.Gaussian;

import org.opencv.core.Mat;

public class BorderMat {

    private final double[][] mat;
    private final double[] innerRgb;
    private final double[] outerRgb;
    private Mat original;
    private double innerLength = 0;
    private double outerLength = 0;
    private Gaussian gaussian;

    public BorderMat(final Mat mat, double sigma){
        this.mat = new double[mat.width()][mat.height()];
        this.original = mat;
        innerRgb = new double[mat.channels()];
        outerRgb = new double[mat.channels()];
        for(int i = 0; i<mat.channels(); i++){
            innerRgb[i] = 0;
            outerRgb[i] = 0;
        }
        for(int i = 0 ; i< original.width(); i++){
            for(int w = 0; w< original.height(); w++){
                this.mat[i][w] = 0;
            }
        }

        gaussian = new Gaussian(3,sigma);
    }

    public void set(final Point loc, final int value){
        if(value == -3 && mat[loc.x][loc.y] != -3){
            addToInner(loc);
        }else if( value == 3 && mat[loc.x][loc.y] != 3){
            addToOuter(loc);
        }else if(value != -3 && mat[loc.x][loc.y] == -3){
            removeFromInner(loc);
        }else if(value != 3 && mat[loc.x][loc.y] == 3){
            removeFromOutter(loc);
        }
        mat[loc.x][loc.y] = value;
    }

    private void addToInner(final Point p){
        for(int i = 0; i<original.channels(); i++){
            innerRgb[i] += original.get(p.y,p.x)[i];
        }
        innerLength++;
    }

    private void addToOuter(final Point p){
        for(int i = 0; i<original.channels(); i++){
            outerRgb[i] += original.get(p.y,p.x)[i];
        }
        outerLength++;
    }

    private void removeFromInner(final Point p){
        for(int i = 0; i<original.channels(); i++){
            innerRgb[i] -= original.get(p.y,p.x)[i];
        }
        innerLength--;
    }

    private void removeFromOutter(final Point p){
        for(int i = 0; i<original.channels(); i++){
            outerRgb[i] -= original.get(p.y,p.x)[i];
        }
        outerLength--;
    }

    public double calculateVelocity(final Point p){
        final double[] rgb = original.get(p.y,p.x);
        double p1sum = 0, p2sum = 0;
        for(int i = 0; i< original.channels(); i++){
            p1sum += Math.pow((innerRgb[i]/innerLength - rgb[i]),2);
            p2sum += Math.pow((outerRgb[i]/outerLength - rgb[i]),2);
        }
        return Math.sqrt(p2sum) - Math.sqrt(p1sum);
    }

    public double get(final Point p){
        return mat[p.x][p.y];
    }

    public double getGaussian(Point p){
        double sum = 0;
        for(int i = p.x - gaussian.getSize()/2; i <= p.x + gaussian.getSize()/2; i++){
            for(int w= p.y - gaussian.getSize()/2; w <= p.y + gaussian.getSize()/2 ; w++){
                if(insideBounds(i,w)){
                    sum += (mat[i][w] * gaussian.get(i - p.x,w - p.y));
                }
            }
        }
        return sum;
    }

    private boolean insideBounds(int x, int y){
        if(x >= 0 && x < original.width() && y >= 0 && y < original.height()){
            return true;
        }
        return false;
    }

    public void printBorderMat(){
        for(int i = 0; i< original.width(); i++){
            System.out.println();
            for(int w = 0; w< original.height(); w++){
                if(mat[i][w] < 0){
                    System.out.print((int)mat[i][w]);
                }else {
                    System.out.print(" " + (int) mat[i][w]);
                }
            }
        }
    }

    private void reset(){
        for(int h = 0; h<original.channels(); h++){
            innerRgb[h] = 0;
            outerRgb[h] = 0;
        }
        outerLength = 0;
        innerLength = 0;
    }

    public void recalculateRgbValues(Mat original){
        this.original = original;
        reset();
        for(int i = 0; i< mat.length; i++){
            for(int w = 0; w < mat[i].length; w++){
                if(mat[i][w] == 3){
                    for(int h = 0; h<original.channels(); h++){
                        outerRgb[h] += original.get(w,i)[h];

                    }
                    outerLength++;
                }else if(mat[i][w] == -3){
                    for(int h = 0; h<original.channels(); h++){
                        innerRgb[h] += original.get(w,i)[h];
                    }
                    innerLength++;
                }
            }
        }
    }

    public void printAverageByBand(){
        System.out.println("R: " + outerRgb[0]/outerLength + " " + innerRgb[0]/innerLength);
        System.out.println("G: " + outerRgb[1]/outerLength + " " + innerRgb[1]/innerLength);
        System.out.println("B: " + outerRgb[2]/outerLength + " " + innerRgb[2]/innerLength);
        System.out.println("Inner: "+innerLength);
        System.out.println("Outter: " + outerLength);
        System.out.println("");
    }


}
