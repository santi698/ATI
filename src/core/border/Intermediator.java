package core.border;


import controller.GetNextImage;
import controller.ViewFunctions;
import org.opencv.core.Mat;

public class Intermediator implements Runnable{

    private final BorderSegmentation segmenter;
    private final GetNextImage func;
    private Mat image;

    public Intermediator(BorderSegmentation segmenter, GetNextImage func, Mat image){
        this.segmenter = segmenter;
        this.func = func;
        this.image = image;
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        Mat img = image;
        do{
            segmenter.segment(img);
        }while((img = func.getNextImage()) != null);
        System.out.println("Finished");
        System.out.println("Time: "+ (System.currentTimeMillis()-time)/1000d);
    }
}
