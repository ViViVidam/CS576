package VideoIndexing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

public class ColorHistogramClustering {
    private static int bins = 256;
    //    private int threshold = 18000;
    private int threshold = 110;

    public List<List<Integer>> clusterFrames(List<BufferedImage> frames) {
        List<List<Integer>> clusters = new ArrayList<>();
        List<Integer> currentCluster = new ArrayList<>();
        currentCluster.add(0);

        double preDistance = 0;
        for (int i = 1; i < frames.size(); i++) {
            BufferedImage currentFrame = frames.get(i);
            BufferedImage lastFrame = frames.get(currentCluster.get(currentCluster.size() - 1));
//            List<BufferedImage> lastCluster = new ArrayList<>();
//            for (int j = 0; j < currentCluster.size(); j++) {
//                lastCluster.add(frames.get(currentCluster.get(j)));
//            }
            double distance = calculateChiSquaredDistance(currentFrame, lastFrame);
            if (i == 1) preDistance = distance;
//            System.out.println("frame" + (i - 1) + " to " + (i) + ":  " + (int)distance);
//            System.out.println((int)distance);
//            matrix+= (int)distance + ",";
//            index += i + ",";
//            if (distance <= threshold) {
//                currentCluster.add(i);
//            } else {
//                // new cluster
//                currentCluster = new ArrayList<>();
//                currentCluster.add(i);
//                clusters.add(currentCluster);
//            }

//            if (Math.abs(distance-preDistance) >= threshold) {
//                if(distance>preDistance){
//                    clusters.add(currentCluster);
//                    currentCluster = new ArrayList<>();
//                    currentCluster.add(i);
//                }
//                else{
//                    currentCluster.remove(currentCluster.size()-1);
//                    clusters.add(currentCluster);
//                    currentCluster = new ArrayList<>();
//                    currentCluster.add(i-1);
//                    currentCluster.add(i);
//                }
//            } else {
//                currentCluster.add(i);
//            }
            if (Math.abs(distance - preDistance) >= threshold) {
                clusters.add(currentCluster.subList(0, 1));
                currentCluster = new ArrayList<>();
                currentCluster.add(i);
            } else {
                currentCluster.add(i);
            }
            preDistance = distance;
        }
        return clusters;
    }

    private double calculateDistance(BufferedImage img1, BufferedImage img2) {
        int[] hist1 = calculateColorHistogram(img1);
        int[] hist2 = calculateColorHistogram(img2);

//        int[] hist2 = calculateAverageColorHistogram(imgs2);

        double distance = 0;
        for (int i = 0; i < bins * 3; i++) {
            double diff = (double) (hist1[i] - hist2[i]);
            distance += diff * diff;
        }

        return Math.sqrt(distance);
    }

    private double calculateChiSquaredDistance(BufferedImage img1, BufferedImage img2) {
        int[] hist1 = calculateColorHistogram(img1);
        int[] hist2 = calculateColorHistogram(img2);


        double distance = 0.0;
        for (int i = 0; i < hist1.length; i++) {
            double diff = hist1[i] - hist2[i];
            double sum = hist1[i] + hist2[i];
            if (sum != 0) {
                distance += (diff * diff) / sum;
            }
        }

        return distance / hist1.length;
    }

    private int[] getLargestElements(int[] array) {
        int[] largestElements = new int[10];
        int[] temp = array.clone();
        Arrays.sort(temp);
        for (int i = 3; i < 13; i++) {
            largestElements[i - 3] = temp[temp.length - 1 - i];
        }
        return largestElements;
    }

    private int[] calculateAverageColorHistogram(List<BufferedImage> lastCluster) {
        int[] avgHistogram = new int[bins * 3];
        for (int i = 0; i < lastCluster.size(); i++) {
            int[] histogram = calculateColorHistogram(lastCluster.get(i));
            for (int j = 0; j < bins * 3; j++) {
                avgHistogram[j] += histogram[j];
            }
        }
        for (int i = 0; i < bins * 3; i++) {
            avgHistogram[i] /= lastCluster.size();
        }
        return avgHistogram;
    }

    private int[] calculateColorHistogram(BufferedImage img) {
        int[] histogram = new int[bins * 3];
        int[] pixels = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
            int binR = r * bins / 256;
            int binG = g * bins / 256;
            int binB = b * bins / 256;
            histogram[binR] += 1;
            histogram[bins + binG] += 1;
            histogram[2 * bins + binB] += 1;
        }
        return histogram;
    }

    public static void main(String[] args) throws IOException {
        List<Integer> shots = new ArrayList<Integer>(Arrays.asList(165, 420, 1080, 1140, 1365, 1980, 2175, 2190, 2340, 2460, 2595, 2730, 3150, 3255, 3285, 3315, 3390, 3555, 3570, 3585, 3630, 3645, 3660, 3675, 3690, 3735, 3780, 3795, 3810, 3825, 3840, 3855, 3885, 3915, 3930, 3945, 3960, 3975, 4005, 4035, 4080, 4095, 4110, 4125, 4140, 4500, 4635, 4845, 4875, 4890, 4905, 4920, 4935, 4950, 4965, 5025, 5340, 5445, 5460, 5475, 5490, 5505, 5520, 5535, 5550, 5565, 5580, 5595, 5865, 5880, 5955, 6150, 6195, 6210, 6225, 6240, 6255, 6270, 6285, 6300, 6315, 6330, 6405, 6420, 6465, 6480, 6495, 6525, 6570, 6585, 6600, 6690, 6870, 6885, 6900, 6915, 6930, 6945, 6975, 7050, 7305, 7470, 7605, 7620, 7755, 7860, 8025, 8100, 8160, 8220, 8280, 8295, 8325, 8370, 8460, 8520));
//        for(int i=0;i<shots.size();i++){
//            System.out.println(i+" "+(double)(shots.get(i)/30.0));
//        }
        List<BufferedImage> frames = new ArrayList<>();
        File inputFile = new File("./InputVideo.rgb");
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
        }
        int frameNumber = -1;
        int num = 0;
        BufferedImage currentFrame = null;
        while (inputStream.read(frameData) != -1) {
            frameNumber++;
            if (num < shots.size() && frameNumber == shots.get(num)) {
                currentFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                currentFrame.getRaster().setDataElements(0, 0, width, height, frameData);
                frames.add(currentFrame);
                num++;
            }
        }

//        for(int i=0;i<frames.size();i++){
//            List<String> topColors = ImageColorAnalyzer.getTopColors(frames.get(i));
//            System.out.println("Frame " + i + ": " + topColors);
//        }

        //cluster frames
        ColorHistogramClustering clustering = new ColorHistogramClustering();
        List<List<Integer>> clusters = clustering.clusterFrames(frames);

        List<Integer> output = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            output.add(shots.get(clusters.get(i).get(0)));
        }
        System.out.println("Scene " + 0 + " starts at frame " + 0);
        for (int i = 0; i < output.size(); i++) {
            System.out.println("Scene " + (i + 1) + " starts at frame " + output.get(i));
        }
    }
    //    static class Cluster {
//
//        private List<Integer> clusters;
//        private int[] avgHistogram = new int[bins * 3];
//
//        public Cluster(List<Integer> clusters, int[] avgHistogram) {
//            this.clusters = clusters;
//            this.avgHistogram = avgHistogram;
//        }
//
//        public Cluster(List<Integer> clusters) {
//            this.clusters = clusters;
//        }
//
//        public List<Integer> getClusters() {
//            return clusters;
//        }
//
//        public int[] getAvgHistogram() {
//            return avgHistogram;
//        }
//    }
}