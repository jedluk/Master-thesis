package algorithm;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import utility.Conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OCR {

    private List<Mat> regions = new ArrayList<>();

    public String doOCRFromMatList() {

        ITesseract instance = new Tesseract();
        StringBuilder stringBuilder = new StringBuilder();

        for (Mat tmp : regions) {
            try {
//              Imgcodecs.imwrite("frame-dump\\aaa" + i++ +".png", tmp);
                instance.setConfigs(Arrays.asList("digits2"));
                stringBuilder.append(instance.doOCR(Conversion.mat2BufferedImage(tmp)));
//              System.out.println(stringBuilder);
            } catch (TesseractException ex) {
                ex.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    /*
        SETTERS
     */
    public void setRegions(List<Mat> regions) {
        this.regions = regions;
    }
    /*
        GETTERS
     */
    public List<Mat> getRegions() {
        return regions;
    }

}
