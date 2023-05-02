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
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;

// using BlockQueue
public class VideoPlayer implements Runnable{
    private BlockingQueue<Message> videoQ;
    private BlockingQueue<Message> audioQ;
    private String filename;
    private int numFrames;
    final private float timeLength;
    private int fps;
    private double checkInterval = 0.5;
    //private int checkIntervalNum;
    private boolean paused;
    private List<List<Integer>> arr;
    public VideoPlayer(String filename, BlockingQueue<Message> queueVideo, BlockingQueue<Message> queueAudio, List<List<Integer>> arr,float timeLength){
        this.filename = filename;
        this.videoQ = queueVideo;
        this.audioQ = queueAudio;
        this.numFrames = 0;
        this.arr = new Vector<>(arr);
        this.timeLength = timeLength;
    }
    @Override
    public void run() {
        File file = new File(this.filename); // "./InputVideo.rgb" name of the RGB video file
        int width = 480; // width of the video frames
        int height = 270; // height of the video frames
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
            List vec = this.arr.get(i);
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
                if(path==null) return;
                if(path.getPathCount() == 3){
                    DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path.getLastPathComponent();
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getPathComponent(1);
                    int x = Integer.parseInt(leaf.getUserObject().toString().substring(4));
                    int y = Integer.parseInt(parent.getUserObject().toString().substring(5));
                    int target = arr.get(y-1).get(x-1);
                    System.out.println(numFrames);
                    videoQ.add(new Message(Message.JUMP,target / numFrames));
                    audioQ.add(new Message(Message.JUMP,target / numFrames));
                }
            }
        });

        //video screen
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridheight = 3;
        c.gridy = 0;
        frame.getContentPane().add(label,c);
        //progress bar
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        //c.weightx = c.weighty = 1;
        c.gridheight = 1;
        c.gridx = 1;
        c.gridy = 3;
        frame.getContentPane().add(progressBar,c);

        // button pane
        JPanel buttonPanel = new JPanel();
        Button stop = new Button("stop");
        Button pause = new Button("pause");

        buttonPanel.add(stop);
        buttonPanel.add(pause);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.weighty = 1;
        c.gridx = 1;
        c.gridy = 4;
        frame.getContentPane().add(buttonPanel,c);

        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        //c.weightx = c.weighty = 1;
        c.gridheight = 1;
        c.gridx = 1;
        c.gridy = 3;
        frame.getContentPane().add(progressBar,c);


        JScrollPane pane = new JScrollPane();
        pane.setPreferredSize(new Dimension(width, height+100));
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 5;
        c.gridwidth = 1;
        frame.getContentPane().add(pane.add(tree),c);


        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //System.out.println("clicked");
                int x = e.getX();
                //System.out.println(progressBar.getWidth()+" " + x);
                audioQ.add(new Message(Message.JUMP,1.0*x/progressBar.getWidth()));
                videoQ.add(new Message(Message.JUMP,1.0*x/progressBar.getWidth()));
            }
        });
        frame.pack();
        frame.setVisible(true);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(paused){
                    pause.setLabel("pause");
                }
                else
                    pause.setLabel("play");
                videoQ.add(new Message(Message.SWITCH,0));
            }
        });
        pause.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(paused){
                    pause.setLabel("pause");
                }
                else
                    pause.setLabel("play");
                videoQ.add(new Message(Message.SWITCH,0));

            }
        });

        stop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(!paused) {
                    videoQ.add(new Message(Message.SWITCH, 0));
                    pause.setLabel("play");
                }
                videoQ.add(new Message(Message.JUMP,0));
                audioQ.add(new Message(Message.JUMP,0));
            }
        });

        // read the video file and display each frame
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long pixelPerFrame = width * height * 3;
            numFrames = (int) (raf.length() / pixelPerFrame);
            System.out.println(raf.length() + " number of frames: " + this.numFrames);
            this.fps = Math.round(this.numFrames / this.timeLength);
            double videoTime = this.numFrames*1.0 / fps;
            System.out.println("setting fps to " + fps + ", Video time size: " + videoTime);
            FileChannel channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(width * height * 3);
            long startime = System.currentTimeMillis();
            long time1;
            long bias1 = 0;// count for jump time
            long bias2 = 0;// count for paused time
            long sleepTime = (long) (1000 / fps);
            boolean jumped;
            long pausetime = -1;
            time1 = System.currentTimeMillis();
            for (int i = 0; i < numFrames ; i++) {
                jumped = false;
                if(i%fps == 0 && this.videoQ.size()>0){
                    while(this.videoQ.size()>0) {
                        Message message = this.videoQ.poll();
                        if (message.message == Message.SWITCH) {
                            paused = !paused;
                            if (pausetime != -1) {
                                bias2 += System.currentTimeMillis() - pausetime;
                                pausetime = -1;
                            } else {
                                pausetime = System.currentTimeMillis();
                            }
                            //System.out.println("switch");
                            audioQ.add(new Message(Message.SWITCH, i * 1.0 / numFrames));
                        }else {
                            long second = (long) (message.target * videoTime);
                            channel.position(pixelPerFrame * fps * second);
                            bias1 = second * 1000;
                            bias2 = 0;
                            if(pausetime!=-1) pausetime = System.currentTimeMillis();
                            time1 = System.currentTimeMillis();
                            i = (int) (fps * second); // -1 so next round it's the start of a new second
                            jumped = true;
                        }
                    }
                    if(paused && jumped) i--;
                }
                if(paused && !jumped){
                    i--;
                    continue;
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
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(i%fps == 0 && !jumped) {
                    long eclipse = System.currentTimeMillis() - time1 + bias1 - bias2;
                    double lag = eclipse - i*1000.0/fps;
                    sleepTime -= 1.5 * lag / fps;
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
