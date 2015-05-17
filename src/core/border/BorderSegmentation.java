package core.border;

import core.helper.Point;
import org.opencv.core.Mat;

import java.util.*;

public class BorderSegmentation {

    private final Set<Point> Lout = new HashSet<>();
    private final Set<Point> Lin = new HashSet<>();
    private Mat original;
    private BorderMat calcMat;
    private final int iterations;

    public BorderSegmentation(int iterations){
        this.iterations = iterations;
    }

    public Set<Point> segmenter(final Mat image, final Point start, final Point end, final double sigma){
        this.original = image;
        int maxExecutions = 10;
        this.calcMat = new BorderMat(image, start, end, sigma);
        for(int x = 0; x<image.rows(); x++){
            for(int y = 0; y<image.cols(); y++){
                final Point loc = new Point(x,y);
                if(x < start.x || x > end.x || y < start.y || y > end.y){
                    calcMat.set(loc, 3);
                }else if(((x == start.x || x==end.x) && (y>= start.y || y<= end.y)) ||
                        ((y == start.y || y==end.y) && (x>=start.x || x<= end.x ))){
                    calcMat.set(loc,1);
                    Lout.add(loc);
                }else if(((x == start.x + 1 ||x == end.x - 1 ) && (y>start.x || y<end.y)) ||
                        ((y == start.y+1 || y== end.y-1) && (x > start.x || x< end.x))){
                    calcMat.set(loc,-1);
                    Lin.add(loc);
                }else if(x > start.x+1 && x < end.x-1 && y > start.y+1 && y< end.y -1){
                    calcMat.set(loc,-3);
                }else{
                    System.out.println("Should not occur");
                }
            }
        }
        for(int w = 0; w < 100; w++) {
            cycle(calcMat, calcMat::calculateVelocity);
            cycle(calcMat, (p) -> -calcMat.getGaussian(p));
        }
        return Lout;
    }

    private void cycle(BorderMat calcMat, Function func){
        for (int i = 0; i < iterations; i++) {
            for (Point p : Lout) {
                if (func.apply(p) > 0) {
                    expand(p, calcMat);
                }
            }
            checkAndRemove(Lin, Lout, -3);
            for (Point p : Lin) {
                if (func.apply(p) < 0) {
                    contract(p, calcMat);
                }
            }
            checkAndRemove(Lout, Lin, 3);
        }
    }

    private void checkAndRemove(Set<Point> origin, Set<Point> result, int value){
        for(Iterator<Point> iterator = origin.iterator(); iterator.hasNext();){
            Point p = iterator.next();
            if(!containsAny(result,calculateNeighbours(iterator.next()))){
                calcMat.set(p,value);
                iterator.remove();
            }
        }
    }

    private boolean containsAny(Collection<Point> original, Collection<Point> data){
        for(Point p: data){
            if(original.contains(p)){
                return true;
            }
        }
        return false;
    }

    private void expand(Point p, BorderMat mat){
        Lout.remove(p);
        Lin.add(p);
        mat.set(p, -1);
        calculateNeighbours(p).stream().filter(i -> mat.get(i) == 3).forEach(i -> {
            Lout.add(i);
            mat.set(i, 1);
        });
    }

    private void contract(Point p, BorderMat mat){
        Lin.remove(p);
        Lout.add(p);
        mat.set(p,1);
        calculateNeighbours(p).stream().filter(i -> mat.get(i) == -3).forEach(i -> {
            Lin.add(i);
            mat.set(i,-1);
        });
    }

    private List<Point> calculateNeighbours(Point p){
        List<Point> list = new LinkedList<>();
        if(p.x > 0){
            list.add(new Point(p.x-1, p.y));
        }
        if(p.x+1 < original.rows()){
            list.add(new Point(p.x+1,p.y));
        }
        if(p.y > 0){
            list.add(new Point(p.x, p.y-1));
        }
        if(p.y+1<original.cols()){
            list.add(new Point(p.x, p.y+1));
        }
        return list;
    }


    private interface Function{
        public double apply(Point x);
    }


}
