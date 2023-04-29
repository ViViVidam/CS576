
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Player {
    public static void main(String[] args) {
        String audioFile = "./InputAudio.wav";
        String videoFile = "./InputVideo.rgb";
        //call preIndexing
        Vector<Vector<Integer>> arr = new Vector<>();
        Vector temp = new Vector<>(1,3);
        temp.add(1);
        temp.add(3);
        temp.add(5012);
        arr.add(temp);
        arr.add(temp);
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
