package VideoIndexing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SceneDetection {
    public static List<Integer> getSceneStartFrames(String filePath)  {
        List<Integer> frames = new ArrayList<>();
        try {
            // Start the Python interpreter and run the script through ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder("python", "path/to/scene_detection.py");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // Write the input of the Java program to the standard input of the Python program
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            out.write(filePath);
            out.newLine();
            out.flush();
            out.close();

            // Read the output of a Python program as input to a Java program
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int i = 0;
            while ((line = in.readLine()) != null) {
//            System.out.println(line);
                if (i++ > 0) {
                    frames.add(Integer.parseInt(line));
                }
            }
            in.close();
            System.out.println(frames);

            int exitCode = p.waitFor();
//        System.out.println("Python script finished with exit code " + exitCode);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return frames;
    }
}
