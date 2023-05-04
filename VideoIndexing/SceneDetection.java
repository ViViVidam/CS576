package VideoIndexing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SceneDetection {
    final private int k;
    private final int m;
    private final int blocksize;
    public double GATEVALUE = 3;
    SceneDetection(int m,int blocksize,int k){
        this.k = k;
        this.m = m;
        this.blocksize = blocksize;
    }

    SceneDetection(int m,int blocksize,int k,double gateVal){
        this.k = k;
        this.m = m;
        this.blocksize = blocksize;
        this.GATEVALUE = gateVal;
    }


    List<Integer> goBackM(String filename, List<Integer> shots) {
        ArrayList<Integer> scenes = new ArrayList<>();
        int width = 480;
        int height = 270;
        int numPixels = width * height;
        int numChannels = 3;
        BlockBaseComparetor comparetor = new BlockBaseComparetor(this.blocksize,this.k,this.GATEVALUE);
        BufferedImage source = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] frameData = new byte[numPixels * numChannels];
        File file = new File(filename);
        FileChannel fn = null;
        ArrayList<Double> result = new ArrayList<>();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            fn = inputStream.getChannel();
            for (int i = 0; i < shots.size(); i++) {
                fn.position(shots.get(i) * (long) numChannels * numPixels);
                inputStream.read(frameData);
                source.getRaster().setDataElements(0,0,width,height,frameData);
                result.clear();
                for (int j = 1; j <= m; j++) {
                    if (i - j < 0) break;
                    fn.position(shots.get(i-j) * (long) numChannels * numPixels);
                    inputStream.read(frameData);
                    dst.getRaster().setDataElements(0,0,width,height,frameData);
                    result.add(comparetor.compare(source, Arrays.asList(dst)));
                }
                int index = getMin(result);
                if(index==-1){
                    scenes.add(0);
                }
                else if(result.get(index)>this.GATEVALUE){
                    scenes.add(i);
                }
                else{
                    int j = scenes.size() - 1;
                    while (scenes.get(j) > i - index - 1) {
                        scenes.remove(j);
                        j--;
                    }
                }
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i = 0; i < scenes.size(); i++){
            temp.add(shots.get(scenes.get(i)));
        }
        return temp;
    }

    int getMin(List<Double> l){
        if(l.size()==0) return -1;
        int index = 0;
        for(int i = 1; i < l.size(); i++){
            if(l.get(index)>l.get(i)){
                index = i;
            }
        }
        return index;
    }
}
