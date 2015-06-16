package core.border;

import java.awt.Point;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.opencv.core.Mat;
public class BorderSegmentation{

    private final Set<Point> Lout = new HashSet<>();
    private final Set<Point> Lin = new HashSet<>();
    private Mat original;
    private BorderMat calcMat;
    private final int iterations;
    private final Consumer<Collection<Point>> overlaySetter;
    private final double sigma;
    private final Point start;
    private final Point end;

    public BorderSegmentation(int iterations, double sigma, Consumer<Collection<Point>> func, final Point start,
                              final Point end){
        this.iterations = iterations;
        this.overlaySetter = func;
        this.sigma = sigma;
        this.start = start;
        this.end = end;
    }

    public void segment(Mat image){
        this.original = image;
        if(Lin.isEmpty() && Lout.isEmpty()) {
            this.calcMat = new BorderMat(original, sigma);
            for (int x = 0; x < original.width(); x++) {
                for (int y = 0; y < original.height(); y++) {
                    final Point loc = new Point(x, y);
                    if (x < start.x || x > end.x || y < start.y || y > end.y) {
                        calcMat.set(loc, 3);
                    } else if (((x == start.x || x == end.x) && (y >= start.y || y <= end.y)) ||
                            ((y == start.y || y == end.y) && (x >= start.x || x <= end.x))) {
                        calcMat.set(loc, 1);
                        Lout.add(loc);
                    } else if (((x == start.x + 1 || x == end.x - 1) && (y > start.x || y < end.y)) ||
                            ((y == start.y + 1 || y == end.y - 1) && (x > start.x || x < end.x))) {
                        calcMat.set(loc, -1);
                        Lin.add(loc);
                    } else if (x > start.x + 1 && x < end.x - 1 && y > start.y + 1 && y < end.y - 1) {
                        calcMat.set(loc, -3);
                    } else {
                        System.out.println("Should not occur");
                    }
                }
            }
            calcMat.disableAvg();
        }else{
            calcMat.recalculateRgbValues(image);
        }
//        calcMat.printAverageByBand();

        operate();
    }

    private void operate(){
        boolean finish = false;
        for(int w = 0; w < iterations && !finish; w++) {
            finish = cycle(calcMat, calcMat::calculateVelocity, 1, true);
            cycle(calcMat, (p) -> (-calcMat.getGaussian(p)), 1, false);
        }
    }

    private boolean cycle(BorderMat calcMat, Function<Point, Double> func,int iterations, boolean endable){
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
            overlaySetter.accept(Lout);
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
}
