package algorithm;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import utility.Configuration;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class Processing extends Algorithm implements ITemplateObservable {

    public static final String OUT_TMP_PATH = System.getProperty("user.dir") + File.separator + "tmp" + File.separator;
    private static float MATCHING_RESULT = 0.02f;
    private static int MATCHING_METHOD = Imgproc.TM_SQDIFF_NORMED;
    public int imageIndex;
    protected boolean DEBUGGING_MODE = false;
    private Set<ITemplateObserver> observers = new HashSet<>();
    private TemplateMatchResult bestMatch = new TemplateMatchResult();
    private ReentrantLock templateFoundLock = new ReentrantLock();
    public boolean filterOutput = false;

    /* crucial fields with default values */
    private boolean gradientActive = true;
    private int gradientSize = 2;
    private boolean adaptiveActive = false;
    private int adaptiveSize = 41;
    private int rectangleWidth = 7;
    private int rectangleHeight = 1;
    private int contourSize = 600;


    public void runMatchingSimultanesouslyFromSet() {

        Configuration cfg = Configuration.getInstance();
        Thread simultaneouslyWorker = new Thread(() -> {
            for (Mat image : cfg.isUseCalibrated() ? cfg.getReadyCalibratedSet() : cfg.getReadyFrameSet()) {
                originalImage = image;
                if (runMatchingSimultaneously()) {
                    imageIndex = cfg.isUseCalibrated()
                            ? cfg.getReadyCalibratedSet().indexOf(image)
                            : cfg.getReadyFrameSet().indexOf(image);
                    notifyObservers();
                    break;
                }
            }
        });
        simultaneouslyWorker.start();
    }

    private void setTemplateMatch() {
        if (bestMatch.cropped != null) {
            try {
                processingImage = originalImage.clone();
                Imgproc.rectangle(processingImage, bestMatch.templatePos, new Point(bestMatch.templatePos.x + bestMatch.template.cols(), bestMatch.templatePos.y + bestMatch.template.rows()),
                        new Scalar(0, 255, 0));
                petrolStationDetected = Configuration.getInstance().getKeyFromValue(bestMatch.template);
            } catch (CvException ex) {
                ex.toString();
            }
        }
        setTemplateFound(bestMatch.cropped);
    }

    public boolean runMatchingSimultaneously() {

        CountDownLatch latch = new CountDownLatch(templates.size());
        bestMatch = new TemplateMatchResult();

        for (Mat template : templates) {
            new Thread(new FindTemplate(template, latch)).start();
        }
        System.out.println("Waiting for workers ...");
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setTemplateMatch();

        return this.templateFound != null;
    }

    protected TemplateMatchResult matchTemplate(Mat template) {

        // variables using in computing
        Mat input = originalImage.clone(), result;
        Mat cropped = null; // store cut image
        Core.MinMaxLocResult mmr;
        Point point;

        int result_cols = input.cols() - template.cols() + 1;
        int result_rows = input.rows() - template.rows() + 1;

        // result image dimensions
        result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

        Imgproc.matchTemplate(input, template, result, MATCHING_METHOD);
        mmr = Core.minMaxLoc(result);
        point = mmr.minLoc;

        boolean boundX = point.x + 2 * template.cols() + 1 < input.cols();
        boolean boundY = point.y + 2 * template.rows() + 1 < input.rows();

        if (mmr.minVal < MATCHING_RESULT && boundX && boundY) { // be careful about matching result !!!
            try {
                cropped = new Mat(input, new Rect((int) point.x, (int) point.y + template.rows(),
                        template.cols(), 3 * template.rows()));
            } catch (CvException ex) {
                ex.toString();
            }
        }

        return new TemplateMatchResult(cropped, mmr.minVal, point, template);
    }

    public Mat matchTemplate() {

        bestMatch = new TemplateMatchResult();

        for (Mat template : this.templates) {
            TemplateMatchResult matchResult = matchTemplate(template);
            if (bestMatch.cropped != null && matchResult.result < bestMatch.result) {
                bestMatch = matchResult;
            }
        }

        setTemplateMatch();

        return bestMatch.cropped;
    }

    public void findInterestRegions(boolean gradientActive, int gradientSize, boolean adaptiveActive, int adaptiveSize, int rectangleWidth, int rectangleHeight, int contourSize) {

        // clone desired image
        Mat processed = templateFound.clone();
        /* RGB -> GRAY (only when input is RGB)*/
        if (processed.channels() == 3) {
            Imgproc.cvtColor(processed, processed, Imgproc.COLOR_RGB2GRAY);
        }

        morphGradient(processed, gradientActive, gradientSize);
        threshold(processed, adaptiveActive, adaptiveSize);
        horizontallyOriented(processed, rectangleWidth, rectangleHeight);

        // set class field
        morphologyProcess = processed;

        // find contours on processed image
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(processed, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // variables uses in seeking for interest regions
        List<MatOfPoint> importantContours = new ArrayList<>();
        List<Rect> interestRegions = new ArrayList<>();
        Mat tmp = templateFound.clone();

        // search for important contours
        for (MatOfPoint contour : contours) {
            if (Imgproc.contourArea(contour) > contourSize) {
                importantContours.add(contour);
                Imgproc.drawContours(tmp, importantContours, importantContours.size() - 1, new Scalar(0, 255, 0));
                interestRegions.add(importantContours.size() - 1, Imgproc.boundingRect(importantContours.get(importantContours.size() - 1)));
            }
        }

        // remove regions with close centers to avoid duplicate entries
        ArrayList<Rect> newRegions = new ArrayList<Rect>();
        for (Rect rect : interestRegions) {
            boolean fit = true;
            for (Rect test : newRegions) {
                if (Math.pow(Math.pow(rect.x - rect.width / 2 - test.x + test.width / 2, 2) + Math.pow(rect.y - rect.height / 2 - test.y + test.height / 2, 2), 0.5) < rect.height / 2) {
                    fit = false;
                    break;
                }
            }
            if (fit) {
                newRegions.add(rect);
            }
        }
        interestRegions = newRegions;

        if (DEBUGGING_MODE) Imgcodecs.imwrite(OUT_TMP_PATH + "contours.jpg", tmp);
        tmp = templateFound.clone();
        for (Rect rect : interestRegions) {
            Imgproc.rectangle(tmp, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect
                    .height), new Scalar(0, 255, 0));
        }
        // set class field
        processingImage = tmp;
        // finally set list of Rectangles
        regions = interestRegions;

        if (DEBUGGING_MODE) Imgcodecs.imwrite(OUT_TMP_PATH + "interests.jpg", tmp);

        convertRectRegionsToMat();
        // prepare mat regions for OCR
        ocr.setRegions(regionsMat);
    }

    public void findInterestRegions() {
        findInterestRegions(true, 2, false, 41, 7, 1, 600);
    }

    public void findInterestRegionsAfterTuning() {

        findInterestRegions(this.gradientActive, this.gradientSize, this.adaptiveActive, this.adaptiveSize, this.rectangleWidth, this.rectangleHeight, this.contourSize);

    }

    protected void morphGradient(Mat processed, boolean active, int size) {

        if (active) {
            Imgproc.morphologyEx(processed, processed, Imgproc.MORPH_GRADIENT, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(size, size)));
            if (DEBUGGING_MODE) Imgcodecs.imwrite(OUT_TMP_PATH + "gradient.jpg", processed);
        }
    }

    protected void threshold(Mat processed, boolean adaptive, int size) {

        if (adaptive) {
            Imgproc.adaptiveThreshold(processed, processed, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc
                    .THRESH_BINARY, size, 1);
        } else {
            Imgproc.threshold(processed, processed, 0.0, 255.0, Imgproc.THRESH_OTSU);
        }
        if (DEBUGGING_MODE) Imgcodecs.imwrite(OUT_TMP_PATH + "binarization.jpg", processed);
    }

    protected void horizontallyOriented(Mat processed, int width, int height) {

        Imgproc.morphologyEx(processed, processed, Imgproc.MORPH_CLOSE, Imgproc.getStructuringElement(Imgproc
                .MORPH_RECT, new Size(width, height)));
        if (DEBUGGING_MODE) Imgcodecs.imwrite(OUT_TMP_PATH + "horizontally.jpg", processed);
    }

    protected void convertRectRegionsToMat() {

        regionsMat.clear();
        for (Rect rect : regions) {
            regionsMat.add(new Mat(templateFound, rect));
        }
    }

    public List<String> putFoundResults() {

        List<String> newResults = new ArrayList<>();
        if (filterOutput) {
            for (String res : Regex.filterPrices(ocr.doOCRFromMatList().split("\n\n"))) {
                newResults.add(res + " on station " + getPetrolStationDetected());
            }
        } else {
            newResults.addAll(Arrays.asList(ocr.doOCRFromMatList().split("\n\n")));
        }
        return newResults;
    }

    @Override
    public void notifyObservers() {

        for (ITemplateObserver observer : observers) {
            observer.getNotifiedFromTemplateObserver();
        }
    }

    @Override
    public void subscribeObserver(ITemplateObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribeObserver(ITemplateObserver observer) {
        observers.remove(observer);
    }

    public class FindTemplate implements Runnable {

        private CountDownLatch latch;
        private Mat template;

        private FindTemplate(Mat template, CountDownLatch latch) {
            this.template = template;
            this.latch = latch;
        }

        @Override
        public void run() {
            TemplateMatchResult result = matchTemplate(template);
            templateFoundLock.lock();
            if (result.cropped != null && result.result < bestMatch.result) {
                bestMatch = result;
            }
            templateFoundLock.unlock();
            latch.countDown();
        }
    }

    public synchronized void setTemplateFound(Mat templateFound) {
        this.templateFound = templateFound;
    }

    public void setTemplates(List<Mat> templates) {
        this.templates = templates;
    }

    public void setOriginalImage(Mat originalImage) {
        this.originalImage = originalImage;
    }

    public String getPetrolStationDetected() {
        return petrolStationDetected;
    }

    public void setGradientActive(boolean gradientActive) {
        this.gradientActive = gradientActive;
    }

    public void setGradientSize(int gradientSize) {
        this.gradientSize = gradientSize;
    }

    public void setAdaptiveActive(boolean adaptiveActive) {
        this.adaptiveActive = adaptiveActive;
    }

    public void setAdaptiveSize(int adaptiveSize) {
        this.adaptiveSize = adaptiveSize;
    }

    public void setRectangleWidth(int rectangleWidth) {
        this.rectangleWidth = rectangleWidth;
    }

    public void setRectangleHeight(int rectangleHeight) {
        this.rectangleHeight = rectangleHeight;
    }

    public void setContourSize(int contourSize) {
        this.contourSize = contourSize;
    }


}
