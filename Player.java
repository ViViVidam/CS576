
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import VideoIndexing.Indexing;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class Player {
    static float getTimeLength(String filename){
        File file = new File(filename);
        try{
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = audioStream.getFormat();
            float temp = audioStream.getFrameLength() / format.getFrameRate();
            audioStream.close();
            return temp;
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    public static void main(String[] args) {
        System.load("D:\\BaiduNetdiskDownload\\opencv\\build\\java\\x64\\opencv_java470.dll");
        String audioFile = "./The_Great_Gatsby_rgb/InputAudio.wav";
        String videoFile = "./The_Great_Gatsby_rgb/InputVideo.rgb";
        //call preIndexing
        float timeLength = getTimeLength(audioFile);
        List<List<Integer>> arr = null;
        try {
            System.out.println("Indexing....");
            Indexing indexer = new Indexing(videoFile,30);
            arr = indexer.runIndexing();

        }catch (IOException e){
            e.printStackTrace();
        }
        if(arr==null){
            System.out.println("not correctly indexed, terminated");
            return;
        }
        //System.out.println(arr);
        BlockingQueue<Message> audioQ = new ArrayBlockingQueue<Message>(100);
        BlockingQueue<Message> videoQ = new ArrayBlockingQueue<Message>(100);

        AudioPlayer ap = new AudioPlayer(audioFile,audioQ);
        VideoPlayer vp = new VideoPlayer(videoFile,videoQ,audioQ,arr,timeLength);
        Thread apThread = new Thread(ap);
        Thread vpThread = new Thread(vp);
        //processOne.command("java .\\VideoPlayer.java");
        byte[] output = new byte[2048];
        apThread.start();
        vpThread.start();
        try {
            //apThread.notify();
            apThread.join();
            vpThread.join();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

