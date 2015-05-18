package core.border;

import controller.RootLayoutController;
import controller.ViewFunctions;
import core.helper.Point;
import org.opencv.core.Mat;

import java.util.*;

public class BorderSegmentation {

    private final Set<Point> Lout = new HashSet<>();
    private final Set<Point> Lin = new HashSet<>();
    private Mat original;
    private BorderMat calcMat;
    private final int iterations;
    private final ViewFunctions viewFunctions;

    public BorderSegmentation(int iterations, ViewFunctions func){
        this.iterations = iterations;
        this.viewFunctions = func;
    }

    public Set<Point> segmenter(final Mat image, final Point start, final Point end, final double sigma){
        this.original = image;
        int maxExecutions = 10;
        this.calcMat = new BorderMat(image, start, end, sigma);
        for(int x = 0; x<image.width(); x++){
            for(int y = 0; y<image.height(); y++){
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
        boolean finish = false;
        for(int w = 0; w < iterations && !finish; w++) {
            finish = cycle(calcMat, calcMat::calculateVelocity, 5, true);
            cycle(calcMat, (p) -> (-calcMat.getGaussian(p)), 5, false);
        }
        return Lout;
    }

    private boolean cycle(BorderMat calcMat, Function func,int iterations, boolean endable){
        boolean end1 = false , end2 = false;
        for (int i = 0; i < iterations || (!end1 && !end2 && endable); i++) {
            end1 = true;
            for(Point p: new HashSet<>(Lout)) {
                if (func.apply(p) > 0) {
                    end1 = false;
                    Lout.remove(p);
                    Lin.add(p);
                    calcMat.set(p, -1);
                    calculateNeighbours(p).stream().filter(j -> calcMat.get(j) == 3).forEach(j -> {
                        Lout.add(j);
                        calcMat.set(j, 1);
                    });
                }
            }
            checkAndRemove(Lin, Lout, -3);
            end2 = true;
            for(Point p: new HashSet<>(Lin)){
                if (func.apply(p) < 0) {
                    end2 = false;
                    Lin.remove(p);
                    Lout.add(p);
                    calcMat.set(p,1);
                    calculateNeighbours(p).stream().filter(j -> calcMat.get(j) == -3).forEach(j -> {
                        Lin.add(j);
                        calcMat.set(j, -1);
                    });
                }
            }
            checkAndRemove(Lout, Lin, 3);
            viewFunctions.applyOverlay(Lout);
        }
        return end1 && end2;
    }

    private void checkAndRemove(Set<Point> origin, Set<Point> result, int value){
        for(Point p: new HashSet<>(origin)){
            if(!containsAny(result,calculateNeighbours(p))){
                calcMat.set(p,value);
                origin.remove(p);
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

    private List<Point> calculateNeighbours(Point p){
        List<Point> list = new LinkedList<>();
        if(p.x > 0){
            list.add(new Point(p.x-1, p.y));
        }
        if(p.x+1 < original.width()){
            list.add(new Point(p.x+1,p.y));
        }
        if(p.y > 0){
            list.add(new Point(p.x, p.y-1));
        }
        if(p.y+1<original.height()){
            list.add(new Point(p.x, p.y+1));
        }
        return list;
    }


    private interface Function{
        public double apply(Point x);
    }


}
