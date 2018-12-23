package utility;

import library.FFmpegLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class FFmpegFrameExtractor implements IFFmpegFrameExtractorObserverable {

    private String TMP_DIR;
    private String MOVIE_DIR;
    public int fpsValue;
    private List<Mat> readyFrameSet = new ArrayList<>();
    private List<Mat> readyCalibratedSet = new ArrayList<>();
    private Set<IFFmpegExtractorObserver> observers;
    private STATE state;

    public FFmpegFrameExtractor(String TMP_DIR, String MOVIE_DIR, int fpsValue) {
        this.TMP_DIR = TMP_DIR;
        this.MOVIE_DIR = MOVIE_DIR;
        this.fpsValue = fpsValue;
        this.observers = new HashSet<>();
        this.state = STATE.NONE;
    }

    public enum STATE {
        NONE,
        EXTRACTING,
        READING,
        CALIBRATING
    }

    private void extractFrames() {

        ProcessBuilder builder = new ProcessBuilder(
                "cmd.exe", "/c", "\"" + FFmpegLoader.FFMPEG_DIR + "\"" + " -i \"" + MOVIE_DIR + "\" -vf fps=" + fpsValue + " \"" + TMP_DIR + "\"" + File.separator + "out_%d.png");
        builder.redirectErrorStream(true);
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            try {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        p.destroy();
    }

    private void readFrames() {

        try {
            List<File> frames = Files.walk(Paths.get(TMP_DIR))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            Collections.sort(frames, (o1, o2) -> {
                int n1 = utility.Files.extractNumber(o1.getName());
                int n2 = utility.Files.extractNumber(o2.getName());
                return n1 - n2;
            });
            for (File file : frames) {
                System.out.println("Adding frame to set" + file.getName());
                readyFrameSet.add(Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_COLOR));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void calibrateFrames() {
        Mat intrinsic = Configuration.getInstance().getIntrinsicParam();
        Mat distCoeff = Configuration.getInstance().getDistCoeffs();
        String imagePath = TMP_DIR + File.separator + "outC_";
        Mat calibrated;
        long start, stop;

        for (int i = 0; i < readyFrameSet.size(); i++) {
            start = System.currentTimeMillis();
            calibrated = new Mat();
            Imgproc.undistort(readyFrameSet.get(i), calibrated, intrinsic, distCoeff);
            readyCalibratedSet.add(calibrated);
            stop = System.currentTimeMillis();
            System.out.println("Frame calibrated !!! Processing time: " + (stop - start) + "ms");
            Imgcodecs.imwrite(imagePath + (i + 1) + ".png", readyCalibratedSet.get(i));
        }

    }

    public void doYourJob() {

        // notify observers after each step
        state = STATE.EXTRACTING;
        notifyObservers();
        extractFrames();
        state = STATE.READING;
        notifyObservers();
        readFrames();
        if (Configuration.getInstance().isUseCalibrated()) {
            state = STATE.CALIBRATING;
            notifyObservers();
            calibrateFrames();
        }
        state = STATE.NONE;
        notifyObservers();
    }

    @Override
    public void notifyObservers() {

        for (IFFmpegExtractorObserver observer : observers) {
            observer.getNotifiedFromFFmpegExtractor();
        }
    }

    @Override
    public void subscribeObserver(IFFmpegExtractorObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribeObserver(IFFmpegExtractorObserver observer) {
        observers.remove(observer);
    }

    /* GETTERS */
    public List<Mat> getReadyFrameSet() {
        return readyFrameSet;
    }
    public List<Mat> getReadyCalibratedSet() {
        return readyCalibratedSet;
    }
    public String getTMP_DIR() {
        return TMP_DIR;
    }
    public STATE getState() {
        return state;
    }
}
