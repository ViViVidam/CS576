package VideoIndexing;

import java.awt.image.BufferedImage;

public class BlockBaseComparetor {
    static private int seconds = 0;
    private int blocksize = 8;
    private int k = 8;
    private double threshold = 255/10;
    BlockBaseComparetor(int blocksize,int k,double threshold){
        this.k = k;
        this.blocksize = blocksize;
        this.threshold = threshold;
        seconds = 0;
    }
    public boolean compare(BufferedImage source,BufferedImage target){
       // new BufferedImage(int 345,int 255,);

        double diff = 0;
        int blockCnt = 0;
        for(int i = 0 ; i<source.getWidth(); i+=blocksize){
            for(int j = 0; j < source.getHeight(); j+=blocksize){
                if(j+blocksize>source.getHeight()||i+blocksize>source.getWidth()){
                    continue;
                }
                double minDiff = 255*blocksize*blocksize*3;
                for(int m = -k; m<k+1; m++){
                    if(m+i<0||m+i>=target.getWidth()) continue;
                    for(int n = -k; n < k+1; n++){
                        if(n+j<0||n+j>=target.getHeight()) continue;
                        minDiff = Math.min(minDiff,blockWiseDiff(source,target,i,j,m+i,j+n));
                        //System.out.println(minDiff);
                    }
                }
                diff += minDiff;
                //System.out.println(minDiff);
                blockCnt++;
            }
        }
        //System.out.println("cnt: " +blockCnt + "difference: " + diff/blockCnt+" seconds: "+(seconds+1));
        //if((diff/blockCnt)>this.threshold)
        System.out.println("loss: "+(diff/blockCnt));
        //seconds++;

        return (diff/blockCnt)>this.threshold;
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
        if(cnt>0) return Math.sqrt(diff)/cnt;
        //System.out.println(targetX+" "+targetY);
        return 0;
    }

    //return the diff on avg of three channels
    static double computeDistanceRGB(int p1, int p2){
        double total = 0;
        for(int i = 0; i < 3; i++) {
            total += (p1&0xff - p2&0xff) * (p1&0xff - p2&0xff);
            p1 = p1 >> 8;
            p2 = p2 >> 8;
        }
        return total / 3.0;
    }
}
/*
class ChildComparetor implements Runnable{
    private int segmentSize;
    private int segmentCnt;
    private int blocksize;
    private int k;
    private int startX;
    private int endX;
    private int startY;
    private int endY;
    private BufferedImage source;
    private BufferedImage target;
    public ChildComparetor(int blocksize, int k, int startX,int endX,int startY,int endY,int segmentSize,int segmentCnt,BufferedImage source,BufferedImage target){
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.segmentSize = segmentSize;
        this.segmentCnt = segmentCnt;
        this.blocksize = blocksize;
        this.k = k;
        this.source = source;
        this.target = target;
    }
    @Override
    public void run(){
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
                        //System.out.println(minDiff);
                    }
                }
                diff += minDiff;
                //System.out.println(minDiff);
                blockCnt++;
            }
        }
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
        if(cnt>0) return Math.sqrt(diff)/cnt;
        //System.out.println(targetX+" "+targetY);
        return 0;
    }

    static double computeDistanceRGB(int p1, int p2){
        double total = 0;
        for(int i = 0; i < 3; i++) {
            total += (p1&0xff - p2&0xff) * (p1&0xff - p2&0xff);
            p1 = p1 >> 8;
            p2 = p2 >> 8;
        }
        return total / 3.0;
    }
}
*/