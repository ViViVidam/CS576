package VideoIndexing;

import org.w3c.dom.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Indexing {
    private String filename;
    private int fps;
    private String videoMP4File;
    public Indexing(String filename,String videoMP4File, int fps){
        this.filename = filename;
        this.fps = fps;
        this.videoMP4File = videoMP4File;
    }
    public List<Nodes> runIndexing()throws IOException{
        SceneDetection sd = new SceneDetection(3,8,32);

        ShotDetection shotDetection = new ShotDetection(10);
        //System.out.println(Runtime.getRuntime().totalMemory()+" " +Runtime.getRuntime().maxMemory());
        List<Integer> shots = sd.getSceneStartFrames(this.videoMP4File);
        //System.out.println(Runtime.getRuntime().totalMemory()+" " +Runtime.getRuntime().maxMemory());
        List<Integer> subshots = shotDetection.SubShotSeparation(this.filename,32,shots,30,480,270);
        //System.out.println(Runtime.getRuntime().totalMemory()+" " +Runtime.getRuntime().maxMemory());
        System.out.println(subshots);
        List<Integer> scenes = sd.goBackM(this.filename,shots);
        //System.out.println(Runtime.getRuntime().totalMemory()+" " +Runtime.getRuntime().maxMemory());
        System.out.println(scenes);
        List<Nodes> res = new ArrayList<>(scenes.size());
        int indexP = 0;
        int indexF = 0;
        int i = 0;
        //new Nodes(123);
        //System.gc();
        //System.out.println(Runtime.getRuntime().totalMemory()+" " +Runtime.getRuntime().maxMemory());
        while(indexF<scenes.size()){
            Nodes tempF = new Nodes(scenes.get(indexF));
            res.add(tempF);
            indexF++;
            if(indexF == scenes.size()){
                while(indexP < shots.size()) {
                    Nodes tempP = new Nodes(shots.get(indexP));
                    indexP++;
                    tempF.add(tempP);
                    if(indexP == shots.size()){
                        while(i < subshots.size()) {
                            tempP.add(new Nodes(subshots.get(i)));
                            i++;
                        }
                        continue;
                    }
                    while(i < subshots.size() && subshots.get(i) < shots.get(indexP)){
                        tempP.add(new Nodes(subshots.get(i)));
                        i++;
                    }
                }
                continue;
            }
            while(indexP < shots.size() && shots.get(indexP) < scenes.get(indexF)){
                Nodes tempP = new Nodes(shots.get(indexP));
                indexP++;
                tempF.add(tempP);
                if(indexP == shots.size()){
                    while(i < subshots.size()) {
                        tempP.add(new Nodes(subshots.get(i)));
                        i++;
                    }
                    continue;
                }
                while(i < subshots.size() && subshots.get(i) < shots.get(indexP)){
                    tempP.add(new Nodes(subshots.get(i)));
                    i++;
                }
            }

        }
        System.out.println("res num: "+res.size());
        for(i = 0; i < res.size();i++){
            System.out.println("level1: "+res.get(i).getChildrenCount());
            for(int j = 0; j < res.get(i).getChildrenCount(); j++){
                System.out.println("level2: "+res.get(i).getChild(j).getChildrenCount());
            }
        }
        return res;
        //return KeyframeToScene(shotDetection.shotSeparationRecursive(this.filename,3,8));//3 is equal to 4 dont go to 64
        //return KeyframeToScene(Arrays.asList(1005, 1470, 1485, 1500, 1515, 1530, 1545, 1560, 1575, 1590, 1605, 1620, 1635, 1650, 1665, 1680, 1845, 1905, 2730, 2820, 3720, 4050, 4065, 4080, 4095, 4155, 4170, 4335, 4365, 4395, 4410, 4440, 4560, 4620, 4650, 4725, 4815, 4830, 4920, 4935, 5010, 5040, 5085, 5100, 5220, 5235, 5250, 5295, 5310, 5325, 5385, 5535));
    }

    public List<List<Integer>> KeyframeToScene(List<Integer> keyframeIndices) {
        SceneDetection sd = new SceneDetection(3,8,32);
        List<List<Integer>> res = new ArrayList<>();
        //res.add(keyframeIndices);
       //return res;

        List<Integer> scenes = sd.goBackM(this.filename,keyframeIndices);
        System.out.println(scenes);

        int index = 0;
        List<Integer> scene = null;
        for (int i = 0; i < keyframeIndices.size(); i++) {
            if (index < scenes.size() && scenes.get(index).equals(keyframeIndices.get(i))) {
                if (scene != null) res.add(scene);
                scene = new ArrayList<>();
                scene.add(scenes.get(index));
                index++;
            } else {
                scene.add(keyframeIndices.get(i));
            }
            //index++;
        }
        res.add(scene);
        System.out.println(res);
        return res;
    }
}
