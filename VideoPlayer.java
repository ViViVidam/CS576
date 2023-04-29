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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

// using BlockQueue
public class VideoPlayer implements Runnable{
    private BlockingQueue<Double> videoQ;
    private BlockingQueue<Double> audioQ;
    private String filename;
    private int numFrames;
    private Vector<Vector<Integer>> arr;
    public VideoPlayer(String filename, BlockingQueue<Double> queueVideo, BlockingQueue<Double> queueAudio, Vector<Vector<Integer>> arr){
        this.filename = filename;
        this.videoQ = queueVideo;
        this.audioQ = queueAudio;
        this.numFrames = 0;
        this.arr = new Vector<>(arr);
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
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Indexing");

        for(int i = 0; i < this.arr.size(); i++){
            DefaultMutableTreeNode tmp = new DefaultMutableTreeNode("scene"+(i+1));
            Vector vec = this.arr.get(i);
            for(int j = 0; j < vec.size(); j++){
                tmp.add(new DefaultMutableTreeNode("shot"+(j+1)));
            }
            //System.out.println(tmp.getUserObject().toString());
            root.add(tmp);
        }

        JTree tree = new JTree(root);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TreePath path = tree.getPathForLocation(e.getX(),e.getY());
                if(path.getPathCount() == 3){
                    DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path.getLastPathComponent();
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getPathComponent(1);
                    int x = Integer.parseInt(leaf.getUserObject().toString().substring(4));
                    int y = Integer.parseInt(parent.getUserObject().toString().substring(5));
                    double target = (double)arr.get(y-1).get(x-1);
                    //System.out.println(target);
                    videoQ.add(target);
                    audioQ.add(target);
                }
            }
        });
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
        frame.getContentPane().add(label,c);

        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 1;
        frame.getContentPane().add(progressBar,c);

        c.fill = GridBagConstraints.CENTER;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 2;
        c.gridwidth = 1;
        frame.getContentPane().add(tree,c);
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
                }
            }
            System.out.println("Time using: " + (System.currentTimeMillis() - startime) / 1000);
            channel.close();
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
