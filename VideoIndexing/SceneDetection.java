package VideoIndexing;

import jssim.SsimCalculator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SceneDetection {
    final private int k;
    private final int blocksize;
    public double GATEVALUE = 40;

    public SceneDetection(int blocksize, int k) {
        this.k = k;
        this.blocksize = blocksize;
    }

    public SceneDetection(int blocksize, int k, double gateVal) {
        this.k = k;
        this.blocksize = blocksize;
        this.GATEVALUE = gateVal;
    }

    public List<Integer> getSceneStartFrames(String filePath) {
        List<Integer> frames = new ArrayList<>();
        try {
            // Start the Python interpreter and run the script through ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder("python", "Pyscript/SceneDetection.py");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // Write the input of the Java program to the standard input of the Python program
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            out.write(filePath);
            out.newLine();
            out.flush();
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int i = 0;
            while ((line = in.readLine()) != null) {
//            System.out.println(line);
                if (i++ > 0) {
                    frames.add(Integer.parseInt(line));
                }
            }
            in.close();
            System.out.println(frames);

            int exitCode = p.waitFor();
//        System.out.println("Python script finished with exit code " + exitCode);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return frames;

    }

    public List<Integer> goBackM(int m,String filename, List<Integer> shots) {
        ArrayList<Integer> scenes = new ArrayList<>(shots.size());
        int width = 480;
        int height = 270;
        int numPixels = width * height;
        int numChannels = 3;
        //BlockBaseComparetor comparetor = new BlockBaseComparetor(this.blocksize, this.k, this.GATEVALUE);
        ColorHistogramClustering chc = new ColorHistogramClustering();
        SsimCalculator sc = new SsimCalculator();
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
                source.getRaster().setDataElements(0, 0, width, height, frameData);
                result.clear();
                for (int j = 1; j <= m; j++) {
                    if (i - j < 0) break;
                    fn.position(shots.get(i - j) * (long) numChannels * numPixels);
                    inputStream.read(frameData);
                    dst.getRaster().setDataElements(0, 0, width, height, frameData);
                    //result.add(comparetor.compareFast(source, dst));
                    //sc.setRefImage(source);
                    //result.add(sc.compareTo(dst));
                    result.add(chc.calculateChiSquaredDistance(source,dst));
                }
                //System.out.println(result);
                int index = getMin(result,1200);
                if (index == -1) {
                    scenes.add(0);
                } else if (result.get(index) > 1200) {
                    scenes.add(i);
                } else {
                    int j = scenes.size() - 1;
                    while (scenes.get(j) > i - index - 1) {
                        scenes.remove(j);
                        j--;
                    }
                }
               // System.out.println(scenes);
            }
            inputStream.close();
            fn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < scenes.size(); i++) {
            temp.add(shots.get(scenes.get(i)));
        }
        return temp;
    }
    int getMin(List<Double> l, double thresh) {
        if (l.size() == 0) return -1;
        int index = 0;
        for (int i = l.size()-1; i >= 0; i--) {
            if (l.get(i) <= thresh) {
                return i;
            }
        }
        return index;
    }

    int getMin(List<Double> l) {
        if (l.size() == 0) return -1;
        int index = 0;
        for (int i = l.size()-1; i >= 0; i--) {
            if (l.get(i) < l.get(index)) {
                index =  i;
            }
        }
        return index;
    }
    int getMax(List<Double> l) {
        if (l.size() == 0) return -1;
        int index = 0;
        for (int i = l.size()-1; i >= 0; i--) {
            if (l.get(i) > l.get(index)) {
                index =  i;
            }
        }
        return index;
    }
}
