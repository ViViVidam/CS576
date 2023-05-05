
import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.concurrent.BlockingQueue;

public class AudioPlayer implements Runnable{
    private BlockingQueue<Message> messageQ;
    private String filename;
    public AudioPlayer(String filename, BlockingQueue<Message> messageQ){
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
            //AudioFormat format1 = new AudioFormat()
            int buffersize = (int) format.getFrameRate() / fps;
            double audioTime = audioStream.getFrameLength() / format.getFrameRate();
            System.out.println("Audio length per frame: " + format.getFrameRate() / fps);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            SourceDataLine audioLine = (SourceDataLine) AudioSystem.getLine(info);
            audioLine.open(format);
            audioLine.start();
            byte[] bytesBuffer = new byte[2048];
            int bytesRead = -1;
            System.out.println(audioStream.getFrameLength()+" " + 289*format.getFrameRate());
            long time1 = System.currentTimeMillis();
            boolean pause = false;
            while(true) {
                if(this.messageQ.size() > 0){
                    while(this.messageQ.size()>0) {
                        Message message = this.messageQ.poll();
                        if (message.message == Message.SWITCH) {
                            pause = !pause;
                        }
                        long second = (long) (message.target * audioTime); // round to 1 second
                        //System.out.println(second);
                        audioLine.drain();
                        fc.position((long) (second * format.getFrameRate() * format.getFrameSize()));
                    }
                }
                if(pause){
                    //Thread.sleep(1000/30); // do not do so much loop
                   continue;
                }
                bytesRead = audioStream.read(bytesBuffer);
                if (bytesRead == -1) {
                    //System.out.println(ip.available());
                    //System.out.println(audioStream.available());
                    //System.out.println(audioLine.getFramePosition());
                    //audioStream.close();
                    if(ip.available()>0){
                        long tmp = fc.position();
                        audioLine.drain();
                        audioLine.close();
                        audioStream.close();
                        file2 = new File(this.filename);
                        ip = new FileInputStream(file2);
                        fc = ip.getChannel();
                        bufferedIn = new BufferedInputStream(ip);
                        audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                        info = new DataLine.Info(SourceDataLine.class, audioStream.getFormat());
                        audioLine = (SourceDataLine) AudioSystem.getLine(info);
                        audioLine.open(format);
                        audioLine.start();
                        fc.position(tmp);
                    }
                    else{
                        break;
                    }
                    bytesRead = audioStream.read(bytesBuffer);
                    //System.out.println(bytesRead);
                }
                //Thread.sleep(1000/30); // do not do so much loop
                audioLine.write(bytesBuffer, 0, bytesRead);

            }
            //System.out.println(audioLine.);
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
