package VideoIndexing;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.opencv.highgui.HighGui;

public class BlockBaseComparetor {
    static private int seconds = 0;
    private int blocksize = 8;
    private int k = 8;
    private double threshold = 255/10;
    BlockBaseComparetor(int blocksize,int k){
        this.k = k;
        this.blocksize = blocksize;
        seconds = 0;
    }
    BlockBaseComparetor(int blocksize,int k,double threshold){
        this.k = k;
        this.blocksize = blocksize;
        this.threshold = threshold;
        seconds = 0;
    }
    public double compareFast(BufferedImage source, BufferedImage target) {
        BlockingQueue<Result> res = new ArrayBlockingQueue<>(6);
        int[] tmp = new int[3];
        for(int i = 0; i < source.getWidth(); i++){
            for(int j = 0; j < source.getHeight();j++){
                int r = (source.getRGB(i,j) >> 16) & 0xff;
                int g = (source.getRGB(i,j) >> 8) & 0xff;
                int b = source.getRGB(i,j) & 0xff;
                ColorTransformer.rgb2luv(r,g,b,tmp);
                source.setRGB(i,j,(tmp[0]<<16)|(tmp[1]<<8)|tmp[2]);
                r = (target.getRGB(i,j) >> 16) & 0xff;
                g = (target.getRGB(i,j) >> 8) & 0xff;
                b = target.getRGB(i,j) & 0xff;
                ColorTransformer.rgb2luv(r,g,b,tmp);
                target.setRGB(i,j,(tmp[0]<<16)|(tmp[1]<<8)|tmp[2]);
            }
        }
        int totalWBlock = source.getWidth() / this.blocksize + source.getWidth() % this.blocksize;
        int totalHBlock = source.getHeight() / this.blocksize + source.getHeight() % this.blocksize;
        ChildComparetor cc1 = new ChildComparetor(this.blocksize,this.k,0,(int)totalWBlock/2*this.blocksize,0,(int)totalHBlock/2*this.blocksize,source,target,res);
        ChildComparetor cc2 = new ChildComparetor(this.blocksize,this.k,(int)(totalWBlock/2)*this.blocksize,source.getWidth(),0,(int)totalHBlock/2*this.blocksize,source,target,res);
        ChildComparetor cc3 = new ChildComparetor(this.blocksize,this.k,0,(int)totalWBlock/2*this.blocksize,(int)(totalHBlock/2)*this.blocksize,source.getHeight(),source,target,res);
        ChildComparetor cc4 = new ChildComparetor(this.blocksize,this.k,(int)(totalWBlock/2)*this.blocksize,source.getWidth(),(int)(totalHBlock/2)*this.blocksize,source.getHeight(),source,target,res);
        Thread thread1 = new Thread(cc1);
        Thread thread2 = new Thread(cc2);
        Thread thread3 = new Thread(cc3);
        Thread thread4 = new Thread(cc4);
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        long cnt = 0;
        double diff = 0;
        for(int i = 0; i < res.size();i++){
            diff+=res.poll().loss;
            cnt+=res.poll().cnt;
        }
        System.out.println(diff/cnt);
        return diff/cnt;
    }

    public double compare(BufferedImage source, List<BufferedImage> targets){
       // new BufferedImage(int 345,int 255,);

        double diff = 0;
        int blockCnt = 0;
        for(int i = 0 ; i<source.getWidth(); i+=blocksize){
            for(int j = 0; j < source.getHeight(); j+=blocksize){
                if(j+blocksize>source.getHeight()||i+blocksize>source.getWidth()){
                    continue;
                }
                double minDiff = 255*blocksize*blocksize*3;
                for(int a = 0; a < targets.size(); a++) {
                    BufferedImage target = targets.get(a);
                    for (int m = -k; m < k + 1; m++) {
                        if (m + i < 0 || m + i >= target.getWidth()) continue;
                        for (int n = -k; n < k + 1; n++) {
                            if (n + j < 0 || n + j >= target.getHeight()) continue;
                            minDiff = Math.min(minDiff, blockWiseDiff(source, target, i, j, m + i, j + n));
                        }
                    }
                }
                diff += minDiff;
                blockCnt++;
            }
        }
        //System.out.println("cnt: " +blockCnt + "difference: " + diff/blockCnt+" seconds: "+(seconds+1));
        //if((diff/blockCnt)>this.threshold)
        //System.out.println("loss: "+(diff/blockCnt));
        //seconds++;

        return diff/blockCnt;
    }
    public double blockWiseDiff(BufferedImage source,BufferedImage target, int x,int y,int targetX,int targetY){
        if(targetY+this.blocksize>target.getHeight()||targetX+this.blocksize>target.getWidth()) return 255*blocksize*blocksize*3;
        double cnt = 0;
        double diff = 0;
        for(int i = 0; i < this.blocksize; i++) {
            for (int j = 0; j < this.blocksize; j++) {
                if (i + x < source.getWidth() && j + y < source.getHeight() && targetX + i < target.getWidth() && targetY + j < target.getHeight()) {
                    diff += computeDistanceRGB(source.getRGB(i + x, j + y), target.getRGB(targetX + i, targetY + j));
                    cnt++;
                }
            }
        }
        //System.out.println(diff);
        if(cnt>0) return Math.sqrt(diff)/cnt;
        //System.out.println(targetX+" "+targetY);
        return 0;
    }

    //return the diff on avg of three channels
    static double computeDistanceRGB(int p1, int p2){

        double total = 0;

        for(int i = 0; i < 2; i++) {
            total += (p1&0xff - p2&0xff) * (p1&0xff - p2&0xff);
            p1 = p1 >> 8;
            p2 = p2 >> 8;
        }
        total = 0.3*Math.sqrt(total);
        total += 0.7*Math.abs(p1&0xff - p2&0xff);
        return total;
    }
}

class ChildComparetor implements Runnable{

    private int blocksize;
    private int k;
    private int startX;
    private int endX;
    private int startY;
    private int endY;
    private BufferedImage source;
    private BufferedImage target;
    private BlockingQueue<Result> resultQ;
    public ChildComparetor(int blocksize, int k, int startX,int endX,int startY,int endY,BufferedImage source,BufferedImage target, BlockingQueue<Result> resultQ){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.blocksize = blocksize;
        this.k = k;
        this.source = source;
        this.target = target;
        this.resultQ = resultQ;
    }
    @Override
    public void run(){
        double diff = 0;
        long blockCnt = 0;
        for(int i = this.startX ; i<this.endX; i+=blocksize){
            for(int j = this.startY; j < this.endY; j+=blocksize){
                if(j+blocksize>source.getHeight()||i+blocksize>source.getWidth()){
                    continue;
                }
                double minDiff = 255*blocksize*blocksize*3;
                for(int m = -k; m<k+1; m++){
                    if(m+i<0||m+i>=target.getWidth()) continue;
                    for(int n = -k; n < k+1; n++){
                        if(n+j<0||n+j>=target.getHeight()) continue;
                        minDiff = Math.min(minDiff,blockWiseDiff(i,j,m+i,j+n));
                    }
                }
                diff += minDiff;
                blockCnt++;
            }
        }
        this.resultQ.add(new Result(blockCnt,diff));
    }

    public double blockWiseDiff(int x,int y,int targetX,int targetY){
        if(targetY+this.blocksize>target.getHeight()||targetX+this.blocksize>target.getWidth()) return 255*blocksize*blocksize*3;
        double cnt = 0;
        double diff = 0;
        for(int i = 0; i < this.blocksize; i++) {
            for (int j = 0; j < this.blocksize; j++) {
                if (i + x < source.getWidth() && j + y < source.getHeight() && targetX + i < target.getWidth() && targetY + j < target.getHeight()) {
                    diff += computeDistanceRGB(source.getRGB(i + x, j + y), target.getRGB(targetX + i, targetY + j));
                    cnt++;
                }
            }
        }
        if(cnt>0) return diff/cnt;
        //System.out.println(targetX+" "+targetY);
        return 0;
    }

    static double computeDistanceRGB(int p1, int p2){
        double total = 0;

        for(int i = 0; i < 2; i++) {
            total += (p1&0xff - p2&0xff) * (p1&0xff - p2&0xff);
            p1 = p1 >> 8;
            p2 = p2 >> 8;
        }
        total = 0.5*Math.sqrt(total);
        total += 0.5*Math.abs(p1&0xff - p2&0xff);
        return total;
    }
}
class Result{
    public long cnt;
    public double loss;
    Result(long cnt,double loss){
        this.cnt = cnt;
        this.loss = loss;
    }
}