from scenedetect.video_manager import VideoManager
from scenedetect.scene_manager import SceneManager
from scenedetect.detectors import ContentDetector, AdaptiveDetector

# create a VideoManager object to open and read video files(better use absolute file path?)
filePath = input()
video_manager = VideoManager([filePath])
video_manager.set_downscale_factor(2)

# create a SceneManager object to manage scenes
scene_manager = SceneManager()
#scene_manager.add_detector(ContentDetector())
scene_manager.add_detector(AdaptiveDetector())
# initialize the video manager and start processing the video
video_manager.start()
scene_manager.detect_scenes(frame_source=video_manager)

# get the list of detected scenes and print their start/end times
scene_list = scene_manager.get_scene_list()
for i, scene in enumerate(scene_list):
    # print(f"Scene {i+1}: Start time: {scene[0].get_seconds()} seconds, End time: {scene[1].get_seconds()} seconds")
    print(f"{scene[0].get_frames()}")

# release the video manager resources
video_manager.release()