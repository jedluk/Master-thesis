package algorithm;

import org.opencv.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jedrzej on 2017-03-28.
 *
 * Algorithm describes only desirable requirements
 *
 */

public abstract class Algorithm {

    protected List<Mat> testImages = new ArrayList<>();
    protected List<String> pathToTestImages = new ArrayList<>();
    protected List<Mat> templates = null;

    protected Mat originalImage = null;
    protected Mat processingImage = null;
    public Mat templateFound = null;
    protected Mat morphologyProcess = null;
    protected List<Rect> regions = new ArrayList<>();
    protected List<Mat> regionsMat = new ArrayList<>();

    protected OCR ocr = new OCR();
    protected String petrolStationDetected = null;

    public class TemplateMatchResult {
        public Mat cropped = null;
        public double result;
        public Point templatePos = null;
        public Mat template = null;

        public TemplateMatchResult(Mat cropped, double result, Point position, Mat template) {
            this.cropped = cropped;
            this.result = result;
            this.templatePos = position;
            this.template = template;
        }

        public TemplateMatchResult() {
            this.cropped = null;
            this.result = 100;
            this.templatePos = null;
            this.template = null;
        }
    }

    abstract public boolean runMatchingSimultaneously();
    abstract protected TemplateMatchResult matchTemplate(Mat Template);
    abstract protected void findInterestRegions(boolean gradientActive, int gradientSize, boolean adaptiveActive, int adaptiveSize, int rectangleWidth, int rectangleHeight, int contourSize);
    abstract protected void morphGradient(Mat processed, boolean active, int size);
    abstract protected void threshold(Mat processed, boolean adaptive, int size);
    abstract void horizontallyOriented(Mat processed, int width, int height);
    abstract void convertRectRegionsToMat();

}