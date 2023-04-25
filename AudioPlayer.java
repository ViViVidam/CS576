
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingQueue;

public class AudioPlayer implements Runnable{
    private BlockingQueue<Double> messageQ;
    private BlockingQueue<Float> syncQ;
    private String filename;
    public AudioPlayer(String filename, BlockingQueue<Double> messageQ,BlockingQueue<Float> syncQ){
        this.syncQ = syncQ;
        this.filename = filename;
        this.messageQ = messageQ;
    }
    @Override
    public void run(){
        int fps = 30;

        File file2 = new File(this.filename); //
        try {
            FileInputStream ip = new FileInputStream(file2);
            FileChannel fc = ip.getChannel();
            InputStream bufferedIn = new BufferedInputStream(ip);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            AudioFormat format = audioStream.getFormat();
            System.out.println(audioStream.getFrameLength() + " " + format.getFrameSize() + " " + format.getFrameRate() + " " + format.getSampleSizeInBits() + " " + format.getSampleRate());
            System.out.println("Audio time length: " + audioStream.getFrameLength() / format.getFrameRate());
            int buffersize = (int) format.getFrameRate() / fps;
            double audioTime = audioStream.getFrameLength() / format.getFrameRate();
            System.out.println("Audio length per frame: " + format.getFrameRate() / fps);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();
            byte[] bytesBuffer = new byte[buffersize*5];
            int bytesRead = -1;
            System.out.println(audioStream.getFrameLength()+" " + 289*format.getFrameRate());
            long time1 = System.currentTimeMillis();
            while(true) {
                if(this.messageQ.size() > 0){
                    long second = (long) (this.messageQ.poll().doubleValue() * audioTime);
                    audioLine.drain();
                    fc.position((long)(second * format.getFrameRate() * format.getFrameSize()));
                }
                bytesRead = audioStream.read(bytesBuffer);
                if (bytesRead == -1) break;
                Thread.sleep(1000/30); // do not do so much loop
                //System.out.println(audioLine.getLongFramePosition());
                if(this.syncQ.size()>0) {
                    this.syncQ.poll();
                }
                this.syncQ.add(audioLine.getLongFramePosition()/ format.getFrameRate());
                audioLine.write(bytesBuffer, 0, bytesRead);
                //System.out.println("Audio: "+ audioLine.getLongFramePosition()/ format.getFrameRate());
            }
            System.out.println(audioStream.getFrameLength()+" " + audioLine.getLongFramePosition());
            System.out.println("audio time using: "+(System.currentTimeMillis() - time1)/1000);
            audioLine.drain();
            audioLine.close();
            audioStream.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
