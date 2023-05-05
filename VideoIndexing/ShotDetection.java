package VideoIndexing;


import jssim.SsimCalculator;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ShotDetection {
    private int fps;

    private double GATEVALUE = 80;

    ShotDetection(int interval) {
        this.fps = interval;
    }

    //fast avg
    private void fastAddImage(BufferedImage source, BufferedImage dst, int cnt) {
        for (int i = 0; i < source.getWidth(); i++) {
            for (int j = 0; j < source.getHeight(); j++) {
                source.setRGB(i, j, source.getRGB(i, j) / cnt + dst.getRGB(i, j) / cnt);
            }
        }
    }

    public List<Integer> shotSeparationRecursive(String inputRGBFile, int loopSize, int beginK) throws IOException {
        int width = 480;
        int height = 270;
        int numPixels = width * height;
        int numChannels = 3;
        FileInputStream inputStream = null;
        byte[] frameData = new byte[numPixels * numChannels];
        List<Integer> nextCandidates = ShotSeparationJump(inputRGBFile, beginK);
        List<Integer> candidates = null;
        BufferedImage previousFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        BufferedImage currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int index = 0;
        for (int i = 0; i < loopSize - 1; i++) {
            beginK = beginK * 2;
            //System.out.println(beginK);
            BlockBaseComparetor comparetor = new BlockBaseComparetor(8, beginK, this.GATEVALUE);
            File inputFile = new File(inputRGBFile);
            FileChannel fn = null;
            try {
                inputStream = new FileInputStream(inputFile);
                fn = inputStream.getChannel();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            candidates = nextCandidates;
            nextCandidates = new ArrayList<>();
            System.out.println("list of canidates: " + candidates);
            int frameNumber = 0;
            for (int j = 0; j < candidates.size(); j++) {
                fn.position((candidates.get(j) - fps) * (long) numPixels * numChannels);
                inputStream.read(frameData);
                previousFrame.getRaster().setDataElements(0, 0, width, height, frameData);
                fn.position(candidates.get(j) * (long) numPixels * numChannels);
                inputStream.read(frameData);
                currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);
                System.out.println(candidates.get(j));
                if (comparetor.compare(currentFrame, Arrays.asList(previousFrame)) > this.GATEVALUE) {
                    nextCandidates.add(candidates.get(j));
                }
            }
            System.out.println(nextCandidates);

        }
        candidates = nextCandidates;
        System.out.println("list of canidates: " + candidates);
        return candidates;
    }

    public List<Integer> ShotSeparationJump(String inputRGBFile, int k) throws IOException {
        File inputFile = new File(inputRGBFile);
        int width = 480;
        int height = 270;
        int numPixels = width * height;
        int numChannels = 3;
        byte[] frameData = new byte[numPixels * numChannels];
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        BufferedImage currentFrame = null;
        BufferedImage previousFrame = null;
        List<Integer> keyframeIndices = new ArrayList<>();
        BlockBaseComparetor comparetor = new BlockBaseComparetor(8, k, this.GATEVALUE);
        boolean isFirstFrame = true;
        int frameNumber = -1;

        // Loop through the video frames
        while (inputStream.read(frameData) != -1) {
            frameNumber++;
            if (frameNumber % this.fps != 0) continue;

            currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);

            BufferedImage grayFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = grayFrame.createGraphics();
            g.drawImage(currentFrame, 0, 0, null);
            g.dispose();

            if (!isFirstFrame) {
                if (comparetor.compareFast(grayFrame, previousFrame) > this.GATEVALUE) {
                    keyframeIndices.add(frameNumber);
                }
            }

            previousFrame = grayFrame;

            isFirstFrame = false;
        }
        inputStream.close();
        keyframeIndices.add(frameNumber - 1);
        //System.out.println(keyframeIndices);
        return keyframeIndices;
    }

    public List<Integer> ShotSeparationAvg(String inputRGBFile) throws IOException {
        int index = 0;
        File inputFile = new File(inputRGBFile);
        int width = 480;
        int height = 270;
        int numPixels = width * height;
        int numChannels = 3;
        byte[] frameData = new byte[numPixels * numChannels];
        FileInputStream inputStream = null;

        try {
            inputStream = new FileInputStream(inputFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        List<BufferedImage> s = new ArrayList<BufferedImage>();
        s.add(null);
        s.add(null);
        boolean[] marker = {false, false};
        BufferedImage currentFrame = null;
        BufferedImage previousFrame = null;
        List<Integer> keyframeIndices = new ArrayList<>();
        keyframeIndices.add(0);
        boolean isFirstFrame = true;
        int frameNumber = -1;
        BlockBaseComparetor comparetor = new BlockBaseComparetor(8, 16, this.GATEVALUE);
        // Loop through the video frames
        while (inputStream.read(frameData) != -1) {
            frameNumber++;
            currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);
            if (frameNumber % this.fps == 0) {
                if (isFirstFrame) {
                    s.set(index, currentFrame);
                    marker[index] = true;
                    isFirstFrame = false;
                } else {
                    fastAddImage(s.get(index), currentFrame, frameNumber % this.fps + 1);
                }
            } else {
                if (marker[0] && marker[1]) {
                    comparetor.compare(s.get(index), Arrays.asList(s.get((index + 1) % 2)));
                    index = (index + 1) % s.size();
                    marker[index] = false;
                    s.set(index, null);
                } else index = (index + 1) % s.size();
                isFirstFrame = true;
            }
        }
        keyframeIndices.add(frameNumber - 1);
        System.out.println(keyframeIndices);
        return keyframeIndices;
    }

    public List<Integer> SubShotSeparation(String inputRGBFile, int k, List<Integer> shots,double fps,int width,int height) throws IOException {
        FileInputStream inputStream = null;
        FileChannel fn = null;
        List<Integer> returnVal = new ArrayList<>();
        File inputFile = new File(inputRGBFile);
        RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
        BufferedImage previousFrame = null;
        BufferedImage currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int numPixels = width * height;
        int numChannels = 3;
        final int INTERVAL = 20;

        byte[] frameData = new byte[numPixels * numChannels];
        BlockBaseComparetor comparetor = new BlockBaseComparetor(8, k);
        SsimCalculator sc = new SsimCalculator();
        shots.add((int) raf.length()/(numChannels*numPixels));
        raf.close();
        try {

            inputStream = new FileInputStream(inputFile);
            fn = inputStream.getChannel();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        for (int i = 1; i < shots.size(); i++) {
            returnVal.add(shots.get(i - 1));
            if (((shots.get(i) - shots.get(i - 1)) / fps) > INTERVAL) {
                System.out.println(shots.get(i)+" "+shots.get(i-1));

                for (int j = shots.get(i - 1); j < shots.get(i); j++) {
                    fn.position(j * (long) numPixels * numChannels);
                    inputStream.read(frameData);
                    currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);
                    if (previousFrame != null) {
                        sc.setRefImage(currentFrame);
                        try {
                            double res = sc.compareTo(previousFrame);
                            //System.out.println("ssim: "+res);
                            if(res<0.7){
                                if((int)(j/fps) == (int)(returnVal.get(returnVal.size()-1)/fps)) continue;
                                returnVal.add(j);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        /*if (comparetor.compareFast(currentFrame, previousFrame) > this.GATEVALUE) {
                            if((int)(j/fps) == (int)(returnVal.get(returnVal.size()-1)/fps)) continue;
                            returnVal.add(j);
                        }*/
                    }
                    previousFrame = new BufferedImage(width,height,BufferedImage.TYPE_3BYTE_BGR);
                    previousFrame.createGraphics().drawImage(currentFrame,0,0,null);
                }
            }
        }
        inputStream.close();
        shots.remove(shots.size()-1);
        System.out.println(returnVal);
        return returnVal;
    }
}
