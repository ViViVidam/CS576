import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.util.concurrent.BlockingQueue;

// using BlockQueue
public class VideoPlayer implements Runnable{
    private BlockingQueue<Double> videoQ;
    private BlockingQueue<Double> audioQ;
    private BlockingQueue<Float> syncQ;
    private String filename;
    private int numFrames;
    public VideoPlayer(String filename, BlockingQueue<Double> queueVideo, BlockingQueue<Double> queueAudio){
        this.filename = filename;
        this.videoQ = queueVideo;
        this.audioQ = queueAudio;
        this.numFrames = 0;
    }
    @Override
    public void run() {
        File file = new File(this.filename); // "./InputVideo.rgb" name of the RGB video file
        int width = 480; // width of the video frames
        int height = 270; // height of the video frames
        int fps = 30; // frames per second of the video
         // number of frames in the video
        // create the JFrame and JLabel to display the video
        JFrame frame = new JFrame("Video Display");
        JProgressBar progressBar = new JProgressBar(0,100);

        frame.getContentPane().setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(width, height));

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        frame.getContentPane().add(label,c);

        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 1;
        frame.getContentPane().add(progressBar,c);
        Component jprogress = frame.getContentPane().getComponent(1);
        //System.out.println(jprogress.getX());
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //System.out.println("clicked");
                int x = e.getX();
                //System.out.println(progressBar.getWidth()+" " + x);
                audioQ.add(1.0*x/progressBar.getWidth());
                videoQ.add(1.0*x/progressBar.getWidth());
            }
        });
        frame.pack();
        frame.setVisible(true);


        // read the video file and display each frame
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long pixelPerFrame = width * height * 3;
            numFrames = (int) (raf.length() / pixelPerFrame);
            System.out.println(raf.length() + " number of frames: " + this.numFrames);
            double videoTime = this.numFrames*1.0 / fps;
            System.out.println("Video time size: " + videoTime);
            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(width * height * 3);
            long startime = System.currentTimeMillis();
            long time1;
            long bias = 0;
            long sleepTime = 1000 / fps;
            boolean jumped;
            time1 = System.currentTimeMillis();
            for (int i = 0; i < numFrames; i++) {
                jumped = false;
                if(this.videoQ.size()>0){
                    long second = (long) (videoTime * this.videoQ.poll().doubleValue());
                    channel.position(pixelPerFrame*fps*second);
                    bias = second*1000;
                    time1 = System.currentTimeMillis();
                    i = (int) (fps * second);
                    jumped = true;
                }
                buffer.clear();
                channel.read(buffer);
                buffer.rewind();
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int r = buffer.get() & 0xff;
                        int g = buffer.get() & 0xff;
                        int b = buffer.get() & 0xff;
                        int rgb = (r << 16) | (g << 8) | b;
                        image.setRGB(x, y, rgb);
                    }
                }
                label.setIcon(new ImageIcon(image));
                progressBar.setValue(100*i/numFrames);
                frame.validate();
                frame.repaint();
                //audioLine.write(bytesBuffer, 0, bytesRead);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(i%fps == 0 && !jumped) {
                    long eclipse = System.currentTimeMillis() - time1 + bias;
                    double lag = eclipse - i*1000.0/fps;
                    sleepTime -= lag / fps;
                    //sleepTime = Math.max(sleepTime, 0); // avoid negative case
                }
                //System.out.println("Video: "+i*1.0/fps);
            }
            System.out.println("Time using: " + (System.currentTimeMillis() - startime) / 1000);
            channel.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}