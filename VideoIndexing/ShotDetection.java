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

public class ShotDetection {

    public static List<Integer> ShotSeparation(String inputRGBFile) throws IOException {
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
        keyframeIndices.add(0);

        // Threshold value for scene change detection
        int thresholdValue = 8000000;

        boolean isFirstFrame = true;
        int frameNumber = 0;

        // Loop through the video frames
        while (inputStream.read(frameData) != -1) {
            frameNumber++;
            int total = 0;

            currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);

            BufferedImage grayFrame = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g = grayFrame.createGraphics();
            g.drawImage(currentFrame, 0, 0, null);
            g.dispose();

            // Calculate the difference between the current frame and the previous frame
            if (!isFirstFrame) {
                int[] currentPixels = grayFrame.getRaster().getPixels(0, 0, width, height, (int[]) null);
                int[] previousPixels = previousFrame.getRaster().getPixels(0, 0, width, height, (int[]) null);
//                int[] differencePixels = new int[numPixels];
                boolean isSceneChange = false;
                for (int i = 0; i < numPixels; i++) {
                    total += Math.abs(currentPixels[i] - previousPixels[i]);
                    if (total > thresholdValue) {
                        isSceneChange = true;
                        break;
                    }
                }

//                boolean isSceneChange = PixelWiseComparetor.pixelwiseCompare(currentFrame, previousFrame);
                if (isSceneChange) {
                    keyframeIndices.add(frameNumber - 1);
                }
            }

            previousFrame = grayFrame;

            isFirstFrame = false;
        }
        keyframeIndices.add(frameNumber - 1);
        System.out.println(keyframeIndices);
        return keyframeIndices;
    }
}
