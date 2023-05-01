package VideoIndexing;

import java.awt.image.BufferedImage;

public class BlockBaseComparetor {
    private BufferedImage source;
    private BufferedImage target;
    static private int seconds = 0;
    private int blocksize = 8;
    private int k = 8;
    private double threshold = 255/10;
    BlockBaseComparetor(BufferedImage source,BufferedImage target,int blocksize,int k,double threshold){
        this.k = k;
        this.source = source;
        this.target = target;
        this.blocksize = blocksize;
        this.threshold = threshold;
        System.out.println(source.getHeight()+" "+source.getWidth());
    }
    public boolean compare(){
       // new BufferedImage(int 345,int 255,);

        double diff = 0;
        int blockCnt = 0;
        for(int i = 0 ; i<this.source.getWidth(); i+=blocksize){
            for(int j = 0; j < this.source.getHeight(); j+=blocksize){
                if(j+blocksize>this.source.getHeight()||i+blocksize>this.source.getWidth()){
                    continue;
                }
                double minDiff = 255*blocksize*blocksize*3;
                for(int m = -k; m<k+1; m++){
                    if(m+i<0||m+i>=this.target.getWidth()) continue;
                    for(int n = -k; n < k+1; n++){
                        if(n+j<0||n+j>=this.target.getHeight()) continue;
                        minDiff = Math.min(minDiff,blockWiseDiff(i,j,m+i,j+n));
                        //System.out.println(minDiff);
                    }
                }
                diff += minDiff;
                //System.out.println(minDiff);
                blockCnt++;
            }
        }
        System.out.println("cnt: " +blockCnt + "difference: " + diff/blockCnt+" seconds: "+(seconds+1));
        seconds++;
        return (diff/blockCnt)>this.threshold;
    }
    public double blockWiseDiff(int x,int y,int targetX,int targetY){
        if(targetY+this.blocksize>this.target.getHeight()||targetX+this.blocksize>this.target.getWidth()) return 255*blocksize*blocksize*3;
        double cnt = 0;
        double diff = 0;
        for(int i = 0; i < this.blocksize; i++) {
            for (int j = 0; j < this.blocksize; j++) {
                if (i + x < source.getWidth() && j + y < this.source.getHeight() && targetX + i < this.target.getWidth() && targetY + j < this.target.getHeight()) {
                    diff += computeDistanceRGB(this.source.getRGB(i + x, j + y), this.target.getRGB(targetX + i, targetY + j));
                    cnt++;
                }
            }
        }
        if(cnt>0) return Math.sqrt(diff)/cnt;
        System.out.println(targetX+" "+targetY);
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

class ChildComparetor implements Runnable{
    private int id;
    private int segmentSize;
    private int segmentCnt;
    public ChildComparetor(int id,int segmentSize,int segmentCnt){
        this.id = id;
        this.segmentSize = segmentSize;
        this.segmentCnt = segmentCnt;
    }
    @Override
    public void run(){
        int xStart = this.id/this.segmentCnt * this.segmentSize;
        int yStart = this.id%this.segmentCnt * this.segmentSize;
    }
}
