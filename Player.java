
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import VideoIndexing.Indexing;

public class Player {
    public static void main(String[] args) {
        String audioFile = "./InputAudio.wav";
        String videoFile = "./InputVideo.rgb";
        //call preIndexing
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
        BlockingQueue<Float> interVA = new ArrayBlockingQueue<Float>(1);
        BlockingQueue<Double> audioQ = new ArrayBlockingQueue<Double>(100);
        BlockingQueue<Double> videoQ = new ArrayBlockingQueue<Double>(100);
        AudioPlayer ap = new AudioPlayer(audioFile,audioQ,interVA);
        VideoPlayer vp = new VideoPlayer(videoFile,videoQ,audioQ,arr);
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
