package utility;

import calibration.Calibration;
import calibration.Set;
import com.sun.org.apache.xpath.internal.axes.FilterExprWalker;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jedrzej on 2017-07-30.
 */
// Configuration is singleton
public class Configuration {

    private static Configuration instance = new Configuration();

    // EXPECTED FIELDS

    // general
    private final String USER_DIR = System.getProperty("user.dir");

    // calibration fields
    private boolean calibrated = false; // calibration state
    private List<String> calibrationImagesPaths; // path to calibration images
    private Set set;            // set of calibration images
    private Calibration calib;  // calibration object
    private String calibrationFilePath = USER_DIR + File.separator + "calib.yaml"; // default calib file
    private Mat intrinsicParam; // intrinsic parameters
    private Mat distCoeffs; // distortion coefficients

    // templates fields
    private List<String> pathsToTemplates; // paths to templates
    private Map<String, Mat> templatesMap; // binded templates map
    private List<Mat> templates; // templates map

    // movie fields
    private List<Mat> readyFrameSet;
    private List<Mat> readyCalibratedSet;
    private boolean useCalibrated = true;

    // current Movie
    private File currentMovie;

    private Configuration() {
        // Exists only to defeat instantation
        this.calibrationImagesPaths = new ArrayList<>();
        this.calib = new Calibration();
        this.set = null;
        this.templatesMap = new LinkedHashMap<>();
        this.pathsToTemplates = new ArrayList<>();
        this.templates = new ArrayList<>();
        this.readyFrameSet = new ArrayList<>();
        this.readyCalibratedSet = new ArrayList<>();
    }

    public static Configuration getInstance() {

        return instance;
    }

    public boolean checkIfTemplatesExists(){
        // only when templates are loaded
        return templates.size() > 0;
    }
    public boolean clearTemplates() {

        if (templates.size() > 0) {

            templates.clear();
            pathsToTemplates.clear();
            templatesMap.clear();

            return true;
        }

        return false;
    }

    public String getKeyFromValue(Object value){

        for(Object o: templatesMap.keySet()){
            if(templatesMap.get(o).equals(value)){
                return (String) o;
            }
        }
        return null;
    }
    /*
        Setters
     */

    public void setSet(Set set) {
        this.set = set;
    }

    public boolean isCalibrated() {
        return calibrated;
    }

    public void setCalibrated(boolean calibrated) {
        this.calibrated = calibrated;
    }

    public void setIntrinsicParam(Mat intrinsicParam) { this.intrinsicParam = intrinsicParam; }

    public void setDistCoeffs(Mat distCoeffs) { this.distCoeffs = distCoeffs; }

    public void setReadyFrameSet(List<Mat> readyFrameSet) { this.readyFrameSet = readyFrameSet; }

    public void setReadyCalibratedSet(List<Mat> readyCalibratedSet) { this.readyCalibratedSet = readyCalibratedSet; }

    public void setUseCalibrated(boolean useCalibrated) { this.useCalibrated = useCalibrated; }

    public void setCurrentMovie(File currentMovie) { this.currentMovie = currentMovie; }

    public void setTemplates() {
        for (String path : pathsToTemplates) {
            templates.add(Imgcodecs.imread(path, Imgcodecs.IMREAD_COLOR));
        }
    }

    public void setTemplatesLinkedHashMap(List<String> templatesName) {

        if (templates.size() > 0 && templates.size() == templatesName.size()) {
            for (int i = 0; i < templates.size(); i++) {
                templatesMap.put(templatesName.get(i), templates.get(i));
            }
        }
    }
    /*
        Getters
     */
    public List<String> getPathsToTemplates() {
        return pathsToTemplates;
    }

    public Set getSet() {
        return set;
    }

    public String getUSER_DIR() {
        return USER_DIR;
    }

    public List<Mat> getTemplates() {
        return templates;
    }

    public List<String> getCalibrationImagesPaths() {
        return calibrationImagesPaths;
    }

    public Calibration getCalib() {
        return calib;
    }

    public Map<String, Mat> getTemplatesMap() {
        return templatesMap;
    }

    public String getCalibrationFilePath() { return calibrationFilePath; }

    public Mat getIntrinsicParam() { return intrinsicParam; }

    public Mat getDistCoeffs() { return distCoeffs; }

    public List<Mat> getReadyFrameSet() { return readyFrameSet; }

    public List<Mat> getReadyCalibratedSet() { return readyCalibratedSet; }

    public boolean isUseCalibrated() { return useCalibrated; }

    public File getCurrentMovie() { return currentMovie; }
}
