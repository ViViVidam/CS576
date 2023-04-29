package VideoIndexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Indexing {
    public static void main(String[] args) throws IOException {
//        SceneDetection sceneDetection = new SceneDetection();
//        List<Integer> keyframeIndices = sceneDetection.SceneSeparation("./InputVideo.rgb");
//        List<List<Integer>> sceneIndices = KeyframeToScene(keyframeIndices);

        ShotDetection shotDetection = new ShotDetection();
        List<Integer> keyShotIndices = shotDetection.ShotSeparation("./InputVideo.rgb");

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
