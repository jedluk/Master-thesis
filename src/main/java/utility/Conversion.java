package utility;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritablePixelFormat;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class Conversion {

    public enum ImageType {
        PNG, JPG, TIFF
    }

    public static Image mat2img(Mat mat) {

        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    /*
        MAY BE USED LATER
     */
    public static Image mat2img(Mat mat, ImageType imageType) {

        MatOfByte byteMat = new MatOfByte();
        switch(imageType){
            case PNG:
                Imgcodecs.imencode(".png", mat, byteMat);
                break;
            case JPG:
                Imgcodecs.imencode(".jpg", mat, byteMat);
                break;
            case TIFF:
                Imgcodecs.imencode(".tiff", mat, byteMat);
                break;
        }
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }



    public static Mat img2Mat(Image image) {

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        byte[] buffer = new byte[width * height * 4];

        PixelReader reader = image.getPixelReader();
        WritablePixelFormat<ByteBuffer> format = WritablePixelFormat.getByteBgraInstance();
        reader.getPixels(0, 0, width, height, format, buffer, 0, width * 4);

        Mat mat = new Mat(height, width, CvType.CV_8UC3);
        mat.put(0, 0, buffer);
        return mat;
    }

    public static Mat bufferedImage2Mat(BufferedImage bi) {

        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }

    public static BufferedImage mat2BufferedImage(Mat mat){

        BufferedImage tmpImage = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) tmpImage.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, data);
        return tmpImage;
    }
}
