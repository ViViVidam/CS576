package VideoIndexing;
//import org.opencv.core.*;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.video.*;
//import org.opencv.imgcodecs.*;
//import org.opencv.videoio.VideoCapture;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ShotDetection {
    private int fps;
    private double GATEVALUE = 2.2;
    ShotDetection(int fps){
        this.fps = fps;
    }
    //fast avg
    private void fastAddImage(BufferedImage source,BufferedImage dst,int cnt){
        for(int i = 0; i < source.getWidth(); i++){
            for(int j = 0; j < source.getHeight(); j++){
                source.setRGB(i,j,source.getRGB(i,j)/cnt + dst.getRGB(i,j)/cnt);
            }
        }
    }
    public List<Integer> shotSeparationRecursive(String inputRGBFile,int loopSize,int beginK) throws IOException {
        int width = 480;
        int height = 270;
        int numPixels = width * height;
        int numChannels = 3;
        FileInputStream inputStream = null;
        byte[] frameData = new byte[numPixels * numChannels];
        List<Integer> nextCandidates = ShotSeparationJump(inputRGBFile);
        //nextCandidates.add(90);
        //nextCandidates.add(180);
        //nextCandidates.add(270);
        List<Integer> candidates = null;

        int index = 0;
        for (int i = 0; i < loopSize; i++) {

            BufferedImage previousFrame = null;
            BufferedImage currentFrame = null;
            beginK = beginK * 2;
            index = 0;
            BlockBaseComparetor comparetor = new BlockBaseComparetor(8, beginK, this.GATEVALUE);
            File inputFile = new File(inputRGBFile);
            try {
                inputStream = new FileInputStream(inputFile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            candidates = nextCandidates;
            nextCandidates = new ArrayList<>();
            System.out.println("list of canidates: " + candidates);
            int frameNumber = 0;
            while (inputStream.read(frameData) != -1) {
                frameNumber++;
                if (frameNumber % this.fps != 0) continue;
                currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);

                BufferedImage grayFrame = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = grayFrame.createGraphics();
                g.drawImage(currentFrame, 0, 0, null);
                g.dispose();
                if (index<candidates.size() && frameNumber == candidates.get(index)) {
                    index++;
                    if (comparetor.compare(previousFrame, grayFrame)){
                        nextCandidates.add(frameNumber);
                    }
                }
                previousFrame = grayFrame;
            }
        }
        System.out.println("list of canidates: " + candidates);
        return candidates;
    }
    public List<Integer> ShotSeparationJump(String inputRGBFile) throws IOException {
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
        BlockBaseComparetor comparetor = new BlockBaseComparetor(8,16,this.GATEVALUE);
        boolean isFirstFrame = true;
        int frameNumber = -1;

        // Loop through the video frames
        while (inputStream.read(frameData) != -1) {
            frameNumber++;
            if(frameNumber%this.fps!=0) continue;

            currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);

            BufferedImage grayFrame = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayFrame.createGraphics();
            g.drawImage(currentFrame, 0, 0, null);
            g.dispose();

            if (!isFirstFrame) {

                if(comparetor.compare(grayFrame,previousFrame)){
                    keyframeIndices.add(frameNumber);
                }
            }

            previousFrame = grayFrame;

            isFirstFrame = false;
        }
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
        boolean[] marker = {false,false};
        BufferedImage currentFrame = null;
        BufferedImage previousFrame = null;
        List<Integer> keyframeIndices = new ArrayList<>();
        keyframeIndices.add(0);
        boolean isFirstFrame = true;
        int frameNumber = -1;
        BlockBaseComparetor comparetor = new BlockBaseComparetor(8,16,this.GATEVALUE);
        // Loop through the video frames
        while (inputStream.read(frameData) != -1) {
            frameNumber++;
            currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);
            if(frameNumber%this.fps==0) {
                if(isFirstFrame) {
                    s.set(index, currentFrame);
                    marker[index] = true;
                    isFirstFrame = false;
                }
                else {
                    fastAddImage(s.get(index), currentFrame,frameNumber%this.fps+1);
                }
            }
            else{
                if(marker[0] && marker[1]){
                    comparetor.compare(s.get(index),s.get((index+1)%2));
                    index = (index + 1) % s.size();
                    marker[index] = false;
                    s.set(index,null);
                }
                else index = (index + 1) % s.size();
                isFirstFrame = true;
            }
        }
        keyframeIndices.add(frameNumber - 1);
        System.out.println(keyframeIndices);
        return keyframeIndices;
    }
}
