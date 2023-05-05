import VideoIndexing.Nodes;
import VideoIndexing.ShotDetection;

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
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

// using BlockQueue
public class VideoPlayer implements Runnable {
    private BlockingQueue<Message> videoQ;
    private BlockingQueue<Message> audioQ;
    private String filename;
    private int numFrames;
    final private float timeLength;
    private double fps;
    private double checkInterval = 0.5;
    //private int checkIntervalNum;
    private boolean paused;
    private List<Nodes> arr;
    private TreeNode xNode;
    private int indexX;
    private int indexY;
    private int indexZ;
    private int curX;
    private int curY;
    private int curZ;
    final private Semaphore semp = new Semaphore(1);
    public VideoPlayer(String filename, BlockingQueue<Message> queueVideo, BlockingQueue<Message> queueAudio, List<Nodes> arr, float timeLength) {
        this.filename = filename;
        this.videoQ = queueVideo;
        this.audioQ = queueAudio;
        this.numFrames = 0;
        this.arr = new ArrayList<>(arr);
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
        JProgressBar progressBar = new JProgressBar(0, 100);
        frame.getContentPane().setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(width, height));
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(width, height));
        GridBagConstraints c = new GridBagConstraints();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Indexing");

        int idSc = -1;
        int idSh = -1;
        DefaultMutableTreeNode SceneNode = null;
        DefaultMutableTreeNode ShotNode = null;
        for(int i = 0; i < this.arr.size(); i++) {
            SceneNode = new DefaultMutableTreeNode("scene"+(i+1));
            for(int j = 0; j < this.arr.get(i).getChildrenCount(); j++){
                ShotNode = new DefaultMutableTreeNode("shot"+(j+1));
                SceneNode.add(ShotNode);
                for(int k = 0; k < this.arr.get(i).getChild(j).getChildrenCount(); k++){
                    ShotNode.add(new DefaultMutableTreeNode("subshot"+(k+1)));
                }
                if(ShotNode.getChildCount() == 1) ShotNode.removeAllChildren();
            }
            root.add(SceneNode);
        }

        JTree tree = new JTree(root);


        //video screen
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridheight = 3;
        c.gridy = 0;
        frame.getContentPane().add(label, c);
        //progress bar
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        //c.weightx = 1;//c.weighty = 1;
        c.gridheight = 1;
        c.gridx = 1;
        c.gridy = 3;
        frame.getContentPane().add(progressBar, c);

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
        frame.getContentPane().add(buttonPanel, c);

        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        //c.weightx = c.weighty = 1;
        c.gridheight = 1;
        c.gridx = 1;
        c.gridy = 3;
        frame.getContentPane().add(progressBar, c);


        JScrollPane pane = new JScrollPane(tree);

        pane.setPreferredSize(new Dimension(width - 100, height - 100));
        c.fill = GridBagConstraints.BOTH;
        //c.anchor = GridBagConstraints.CENTER;
        //c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 5;
        c.gridwidth = 1;
        c.weightx = c.weighty = 1;
        frame.getContentPane().add(pane, c);

        //System.out.println(111111);
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //System.out.println("clicked");
                int x = e.getX();
                //System.out.println(progressBar.getWidth()+" " + x);
                audioQ.add(new Message(Message.JUMP, 1.0 * x / progressBar.getWidth()));
                videoQ.add(new Message(Message.JUMP, 1.0 * x / progressBar.getWidth()));
            }
        });
        frame.pack();
        frame.setVisible(true);
        //System.out.println(111111);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (paused) {
                    pause.setLabel("pause");
                } else
                    pause.setLabel("play");
                videoQ.add(new Message(Message.SWITCH, 0));
            }
        });
        pause.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (paused) {
                    pause.setLabel("pause");
                } else
                    pause.setLabel("play");
                videoQ.add(new Message(Message.SWITCH, 0));

            }
        });
        stop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!paused) {
                    videoQ.add(new Message(Message.SWITCH, 0));
                    pause.setLabel("play");
                }
                videoQ.add(new Message(Message.JUMP, 0));
                audioQ.add(new Message(Message.JUMP, 0));
            }
        });
        //System.out.println(2222222);



        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        tree.setToggleClickCount(0);//disable double click
        TreeNode rootNode = ((TreeNode) tree.getModel().getRoot());
        TreePath treePath = new TreePath(new TreeNode[]{rootNode,rootNode.getChildAt(0),rootNode.getChildAt(0).getChildAt(0)});
        tree.addSelectionPath(treePath);

        this.indexX = this.indexY = this.indexZ = 0; // for highlight
        this.curX = this.curY = this.curZ = 0;
        this.indexZ++;
        if(this.indexZ == this.arr.get(curX).getChild(curY).getChildrenCount()){
            this.indexZ = 0;
            this.indexY++;
            if(this.indexY == this.arr.get(curX).getChildrenCount()){
                this.indexY = 0;
                this.indexX = 0;
            }
        }
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                super.mouseClicked(e);
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if(path==null) return; // no clicking the actual box;
                TreeNode node = (TreeNode) path.getLastPathComponent();
                if (path == null) return;
                try {
                    semp.acquire();
                }catch (Exception ee){
                    ee.printStackTrace();
                }
                if (path.getPathCount() == 3) {
                    DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path.getLastPathComponent();
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) path.getPathComponent(1);
                    int y = Integer.parseInt(leaf.getUserObject().toString().substring(4));
                    int x = Integer.parseInt(parent.getUserObject().toString().substring(5));
                    int target = arr.get(x - 1).getChild(y - 1).getVal();
                    indexZ = curZ = 0;
                    indexX = curX = x-1;
                    indexY = curY = y-1;
                    indexZ++;
                    if(arr.get(indexX).getChild(indexY).getChildrenCount()<=indexZ) {
                        indexZ = 0;
                        indexY++;
                        if (arr.get(indexX).getChildrenCount() <= indexY) {
                            indexY = 0;
                            indexX++;
                        }
                    }
                    System.out.println(indexX+" "+indexY+" "+indexZ);
                    videoQ.add(new Message(Message.JUMP, 1.0 * target / numFrames));
                    audioQ.add(new Message(Message.JUMP, 1.0 * target / numFrames));
                }
                else if(path.getPathCount()==4){
                    //System.out.println("main2: "+indexX+" "+indexY+" " +indexZ);
                    DefaultMutableTreeNode leaf = (DefaultMutableTreeNode) path.getLastPathComponent();
                    int z = Integer.parseInt(leaf.getUserObject().toString().substring(7));
                    int y = Integer.parseInt(((DefaultMutableTreeNode)node.getParent()).getUserObject().toString().substring(4));
                    int x = Integer.parseInt(((DefaultMutableTreeNode)node.getParent().getParent()).getUserObject().toString().substring(5));
                    int target = arr.get(x - 1).getChild(y - 1).getChild(z-1).getVal();
                    System.out.println(target + " " + numFrames);
                    indexX = curX = x - 1;
                    indexY = curY = y - 1;
                    indexZ = curZ = z - 1;
                    indexZ++;
                    System.out.println(indexY);
                    System.out.println(arr.get(indexX).getChildrenCount());
                    if(arr.get(indexX).getChild(indexY).getChildrenCount()<=indexZ) {
                        indexZ = 0;
                        indexY++;
                        if (arr.get(indexX).getChildrenCount() <= indexY) {
                            indexY = 0;
                            indexX++;
                        }
                    }
                    System.out.println(indexX+" "+indexY+" "+indexZ);
                    videoQ.add(new Message(Message.JUMP, 1.0 * target / numFrames));
                    audioQ.add(new Message(Message.JUMP, 1.0 * target / numFrames));
                }
                semp.release();
            }
        });

        // read the video file and display each frame
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            long pixelPerFrame = width * height * 3;
            numFrames = (int) (raf.length() / pixelPerFrame);
            System.out.println(raf.length() + " number of frames: " + this.numFrames);
            this.fps = Math.round(this.numFrames / this.timeLength);
            //double videoTime = this.numFrames*1.0 / fps;
            System.out.println("setting fps to " + fps + ", Video time size: " + this.timeLength);
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

            for (int i = 0; i < numFrames; i++) {
                //System.out.println(i);
                jumped = false;
                if (i % fps == 0) {
                    // poll message
                    if (this.videoQ.size() > 0) {
                        while (this.videoQ.size() > 0) {
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
                            } else {
                                long second = (long) (message.target * this.timeLength);
                                channel.position(pixelPerFrame * (long) (fps * second));
                                bias1 = second * 1000;
                                //System.out.println(second);
                                bias2 = 0;
                                i = (int) (fps * second);
                                if (pausetime != -1) pausetime = System.currentTimeMillis();
                                time1 = System.currentTimeMillis();

                                jumped = true;

                            }
                        }

                    }
                    // highlight
                    try {
                        this.semp.acquire();
                    }catch (Exception ee){
                        ee.printStackTrace();
                    }
                    if(curX < arr.size() && arr.get(curX).getChild(curY).getChild(curZ).getVal()>i){
                        while (arr.get(curX).getChild(curY).getChild(curZ).getVal()>i){
                            indexY = curY;
                            indexX = curX;
                            indexZ = curZ;
                            curZ--;
                            if(curZ==-1){
                                curY--;
                                if(curY==-1){
                                    curX--;
                                    curY = arr.get(curX).getChildrenCount()-1;
                                }
                                curZ = arr.get(curX).getChild(curY).getChildrenCount()-1;
                            }
                        }
                        tree.clearSelection();
                        if(arr.get(curX).getChild(curY).getChildrenCount()==1){
                            tree.setSelectionPath(new TreePath(new TreeNode[]{rootNode,rootNode.getChildAt(curX),rootNode.getChildAt(curX).getChildAt(curY)}));
                        }
                        else{
                            tree.setSelectionPath(new TreePath(new TreeNode[]{rootNode,rootNode.getChildAt(curX),rootNode.getChildAt(curX).getChildAt(curY),rootNode.getChildAt(curX).getChildAt(curY).getChildAt(curZ)}));
                        }
                    }
                    else if(indexX < arr.size() && arr.get(indexX).getChild(indexY).getChild(indexZ).getVal()<i) {
                        System.out.println("if: "+indexX+" "+indexY+" "+indexZ);
                        while (indexX < arr.size() && arr.get(indexX).getChild(indexY).getChild(indexZ).getVal() < i) {
                            curX = indexX;
                            curY = indexY;
                            curZ = indexZ;
                            indexZ++;
                            if (indexZ == arr.get(indexX).getChild(indexY).getChildrenCount()) {
                                indexY++;
                                indexZ = 0;
                                if (indexY == arr.get(indexX).getChildrenCount()) {
                                    indexX++;
                                    indexY = 0;
                                }
                            }
                            System.out.println("loop: "+indexX+" "+indexY+" "+indexZ);
                        }
                        if (arr.get(curX).getChild(curY).getChildrenCount() == 1) {
                            tree.setSelectionPath(new TreePath(new TreeNode[]{rootNode, rootNode.getChildAt(curX), rootNode.getChildAt(curX).getChildAt(curY)}));
                        } else {
                            tree.setSelectionPath(new TreePath(new TreeNode[]{rootNode, rootNode.getChildAt(curX), rootNode.getChildAt(curX).getChildAt(curY), rootNode.getChildAt(curX).getChildAt(curY).getChildAt(curZ)}));
                        }
                    }
                    semp.release();
                    if (paused && jumped) i--;
                }
                if (paused && !jumped) {
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
                progressBar.setValue(100 * i / numFrames);
                frame.validate();
                frame.repaint();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (i % fps == 0 && !jumped) {
                    long eclipse = System.currentTimeMillis() - time1 + bias1 - bias2;
                    double lag = eclipse - i * 1000.0 / fps;
                    sleepTime -= 1.5 * lag / fps;
                    sleepTime = Math.max(0, sleepTime);
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
