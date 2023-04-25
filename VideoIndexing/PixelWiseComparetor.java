package VideoIndexing;

import java.awt.image.BufferedImage;

public class PixelWiseComparetor {
    static int threhold = 255 / 8;
    static boolean pixelwiseCompare(BufferedImage a, BufferedImage b){
        int height = a.getHeight();
        int width = a.getWidth();
        if(b.getWidth()!=width || b.getHeight()!=height){
            System.out.println("pixelwiseCompare: input size of a and b in not matched");
            return false;
        }
        double total = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixela = a.getRGB(x,y);
                int pixelb = b.getRGB(x,y);
                total += computeDistanceRGB(pixela,pixelb);
            }
        }
        return (total/(height*width)) > threhold;
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
