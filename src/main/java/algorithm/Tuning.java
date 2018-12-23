package algorithm;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import utility.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class Tuning extends Processing {

    public Tuning() { }


    public void createTmpFolder() {

        File dir = new File(Tuning.OUT_TMP_PATH);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    public boolean clearTestImages() {

        if (testImages.size() > 0) {

            testImages.clear();
            pathToTestImages.clear();
            originalImage = null;
            processingImage = null;

            return true;
        }

        return false;
    }

    /*
        Setters
     */
    public void setProcessingImage(Mat processingImage) {
        this.processingImage = processingImage;
    }

    public void setMorphologyProcess(Mat morphologyProcess) {
        this.morphologyProcess = morphologyProcess;
    }


    public void setTestImages() {
        for (String path : pathToTestImages) {
            testImages.add(Imgcodecs.imread(path, Imgcodecs.IMREAD_COLOR));
        }
    }



    public synchronized void setTemplateFound(Mat templateFound) {
        this.templateFound = templateFound;
    }

    public void setDEBUGGING_MODE(boolean DEBUGGING_MODE) { this.DEBUGGING_MODE = DEBUGGING_MODE; }

    /*
                Getters
             */
    public Mat getOriginalImage() {
        return originalImage;
    }

    public Mat getProcessingImage() {
        return processingImage;
    }

    public Mat getTemplateFound() {
        return templateFound;
    }

    public Mat getMorphologyProcess() {
        return morphologyProcess;
    }

    public List<Mat> getTestImages() {
        return testImages;
    }

    public List<String> getPathToTestImages() {
        return pathToTestImages;
    }

    public List<Mat> getTemplates() {
        return templates;
    }

    public OCR getOcr() { return ocr; }

}
