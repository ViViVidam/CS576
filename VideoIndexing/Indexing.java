package VideoIndexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Indexing {
    private String filename;
    private int fps;
    public Indexing(String filename,int fps){
        this.filename = filename;
        this.fps = fps;
    }
    public List<List<Integer>> runIndexing()throws IOException{
//        SceneDetection sceneDetection = new SceneDetection();
//        List<Integer> keyframeIndices = sceneDetection.SceneSeparation("./InputVideo.rgb");
//        List<List<Integer>> sceneIndices = KeyframeToScene(keyframeIndices);

        ShotDetection shotDetection = new ShotDetection(10);
        //return KeyframeToScene(shotDetection.ShotSeparationJump(this.filename));
        //return KeyframeToScene(shotDetection.ShotSeparationAvg(this.filename));
        return KeyframeToScene(shotDetection.shotSeparationRecursive(this.filename,3,8));//3 is equal to 4 dont go to 64
        //return KeyframeToScene(Arrays.asList(1005, 1470, 1485, 1500, 1515, 1530, 1545, 1560, 1575, 1590, 1605, 1620, 1635, 1650, 1665, 1680, 1845, 1905, 2730, 2820, 3720, 4050, 4065, 4080, 4095, 4155, 4170, 4335, 4365, 4395, 4410, 4440, 4560, 4620, 4650, 4725, 4815, 4830, 4920, 4935, 5010, 5040, 5085, 5100, 5220, 5235, 5250, 5295, 5310, 5325, 5385, 5535));
    }

    public List<List<Integer>> KeyframeToScene(List<Integer> keyframeIndices) {
        SceneDetection sd = new SceneDetection(3,8,32);
        List<Integer> scenes = sd.goBackM(this.filename,keyframeIndices);
        System.out.println(scenes);
        List<List<Integer>> res = new ArrayList<>();
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
