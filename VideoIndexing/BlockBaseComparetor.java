package VideoIndexing;

import java.awt.image.BufferedImage;

public class BlockBaseComparetor {
    private BufferedImage source;
    private BufferedImage target;
    private int blocksize = 8;
    private int k = 8;
    private int threshold = 255/10;
    BlockBaseComparetor(BufferedImage source,BufferedImage target,int blocksize,int k,int threshold){
        this.k = k;
        this.source = source;
        this.target = target;
        this.blocksize = blocksize;
        this.threshold = threshold;
    }

    public boolean compare(){
        double diff = 0;
        int blockCnt = 0;
        for(int i = 0 ; i<this.source.getHeight(); i+=blocksize){
            for(int j = 0; j < this.source.getWidth(); j+=blocksize){
                double minDiff = 255*blocksize*blocksize*3;
                for(int m = -8; m<9; m++){
                    if(m+i<0||m+i>=this.target.getHeight()) continue;
                    for(int n = -8; n < 9; n++){
                        if(n+j<0||n+j>=this.target.getWidth()) continue;
                        minDiff = Math.min(minDiff,blockWiseDiff(i,j,m+i,j+n));
                    }
                }
                diff += minDiff;
                blockCnt++;
            }
        }

        return (diff/blockCnt)>this.threshold;
    }
    public double blockWiseDiff(int x,int y,int targetX,int targetY){
        double cnt = 0;
        double diff = 0;
        for(int i = 0; i < this.blocksize; i++){
            for(int j = 0; j < this.blocksize; j++) {
                if(i+x<source.getHeight() && j+y<this.source.getWidth() && targetX+i < this.target.getHeight() && targetY+j<this.target.getWidth()) {
                    diff += computeDistanceRGB(this.source.getRGB(i + x, j + y),this.target.getRGB(targetX+i, targetY+j));
                    cnt++;
                }
            }
        }
        return Math.sqrt(diff)/cnt;
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
