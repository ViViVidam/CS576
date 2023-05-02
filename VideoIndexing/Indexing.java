package VideoIndexing;

import java.io.IOException;
import java.util.ArrayList;
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

        ShotDetection shotDetection = new ShotDetection(fps);
        //return KeyframeToScene(shotDetection.ShotSeparationJump(this.filename));
        //return KeyframeToScene(shotDetection.ShotSeparationAvg(this.filename));
        return KeyframeToScene(shotDetection.shotSeparationRecursive(this.filename,2,8));//3 is equal to 4
    }

    public static List<List<Integer>> KeyframeToScene(List<Integer> keyframeIndices) {
        List<List<Integer>> sceneIndices = new ArrayList<>();
        for (int i = 0; i < keyframeIndices.size() - 1; i++) {
            List<Integer> scene = new ArrayList<>();
            scene.add(keyframeIndices.get(i));
            scene.add(keyframeIndices.get(i + 1));
            sceneIndices.add(scene);
        }
        return sceneIndices;
    }
}
