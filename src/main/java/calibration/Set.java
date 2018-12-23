package calibration;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.List;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class Set {

    private Mat[] uncalibratedSet;
    private Mat[] calibratedSet;

    public Set(List<String> paths) {

        uncalibratedSet = setUncalibratedSet(paths);
        calibratedSet = new Mat[uncalibratedSet.length];
    }

    private Mat[] setUncalibratedSet(List<String> paths) {

        int index = 0;
        Mat[] Set = new Mat[paths.size()];

        for (String path : paths) {
            Set[index++] = Imgcodecs.imread(path, Imgcodecs.IMREAD_COLOR);
        }
        return Set;
    }

    public void setCalibratedSet(Mat[] calibratedSet) {
        this.calibratedSet = calibratedSet;
    }

    public Mat[] getUncalibratedSet() {
        return uncalibratedSet;
    }

    public Mat[] getCalibratedSet() {
        return calibratedSet;
    }
}
