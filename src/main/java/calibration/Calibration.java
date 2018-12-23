package calibration;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.yaml.snakeyaml.Yaml;
import utility.Configuration;

import java.io.*;
import java.util.*;
import java.util.Set;

import static org.opencv.calib3d.Calib3d.drawChessboardCorners;
import static org.opencv.calib3d.Calib3d.findChessboardCorners;
import static org.opencv.imgproc.Imgproc.cornerSubPix;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class Calibration implements ICalibrationObservable {

    private final String CALIBRATION_FILE = "calib.yaml";
    private Set<ICalibrationObserver> observers;

    public Calibration() {
        this.observers = new HashSet<>();
    }

    public void calibrateCamera(int numCornersHor, int numCornersVer, int numBoards, boolean save){
        calibrateCamera(numCornersHor, numCornersVer, numBoards, save, false, 0.0);
    }

    public void calibrateCamera(int numCornersHor, int numCornersVer, int numBoards, boolean save, boolean scaled, double factor) {

        int numSquares = numCornersHor * numCornersVer;
        Size board_sz = new Size(numCornersHor, numCornersVer);

        List<Mat> object_points = new ArrayList<>();
        List<Mat> image_points = new ArrayList<>();

        MatOfPoint2f corners = new MatOfPoint2f();
        int success = 0;

        // get calibrated and uncalibrated set
        Mat[] image = Configuration.getInstance().getSet().getUncalibratedSet();
        Mat[] calibrated = Configuration.getInstance().getSet().getCalibratedSet();

        MatOfPoint3f obj = new MatOfPoint3f();
        // create model grid
        for (int j = 0; j < numSquares; j++) {
            obj.push_back(new MatOfPoint3f(new Point3(j / numCornersHor, (int) j % numCornersHor, 0.0f)));
        }

        int i = 0;
        boolean found;
        Mat gray_image = new Mat(), tmp;

        // TODO: 2017-08-24 what if number of images is not sufficient ?
        while (success < numBoards) {

            Imgproc.cvtColor(image[i], gray_image, Imgproc.COLOR_BGR2GRAY);
            found = findChessboardCorners(image[i], board_sz, corners, Calib3d.CALIB_CB_ADAPTIVE_THRESH
                    | Calib3d.CALIB_CB_NORMALIZE_IMAGE | Calib3d.CALIB_CB_FAST_CHECK);
            if (found) {
                cornerSubPix(gray_image, corners, new Size(11, 11), new Size(-1, -1), new TermCriteria(TermCriteria
                        .MAX_ITER | TermCriteria.EPS, 30, 0.1));
                drawChessboardCorners(gray_image, board_sz, corners, found);
                tmp = image[i].clone();
                Calib3d.drawChessboardCorners(tmp, board_sz, corners, found);
                calibrated[i] = tmp;
                image_points.add(corners);
                object_points.add(obj);
                success++;
                if (success >= numBoards) {
                    break;
                }
            }

            i++;
        }

        Mat intrinsic = new Mat(3, 3, CvType.CV_32FC1);
        Mat distCoeffs = new Mat();

        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();

        /* INTRISIC PARAMETERES MATRIX
            fx  0   cx
            0   fy  cy
            0   0   1
         */
        intrinsic.put(0, 0, 1);
        intrinsic.put(1, 1, 1);

        Calib3d.calibrateCamera(object_points, image_points, image[0].size(), intrinsic, distCoeffs, rvecs, tvecs);
        // save data to Configuration object
        if(scaled){
                intrinsic.put(0,0,intrinsic.get(0,0)[0] * factor);
                intrinsic.put(0,2,intrinsic.get(0,2)[0] * factor);
                intrinsic.put(1,1,intrinsic.get(1,1)[0] * factor);
                intrinsic.put(1,2,intrinsic.get(1,2)[0] * factor);
        }
        Configuration.getInstance().setCalibrated(true);
        Configuration.getInstance().setIntrinsicParam(intrinsic);
        Configuration.getInstance().setDistCoeffs(distCoeffs);
        System.out.println("Camera is calibrated !!!");
        notifyObservers();

        if (save) {
            saveCameraParam(intrinsic, distCoeffs, Configuration.getInstance().getCalibrationFilePath());
            System.out.println("Camera parameters saved to file!");
        }

    }

    public void saveCameraParam(Mat intrinsic, Mat distCoeffs, String path) {

        Map<String, Map> data = new HashMap<>();

        // first save intrinsic
        Map<String, double[]> intrinsicMap = new HashMap<>();

        data.put("Intrinsic parameters", intrinsicMap);

        intrinsicMap.put("fx", intrinsic.get(0, 0));
        intrinsicMap.put("cx", intrinsic.get(0, 2));
        intrinsicMap.put("fy", intrinsic.get(1, 1));
        intrinsicMap.put("cy", intrinsic.get(1, 2));

        // second save ditortion coefficients
        Map<String, double[]> distCoeffMap = new HashMap<>();

        data.put("Distortion coefficients", distCoeffMap);

        distCoeffMap.put("k1", distCoeffs.get(0, 0));
        distCoeffMap.put("k2", distCoeffs.get(0, 1));
        distCoeffMap.put("p1", distCoeffs.get(0, 2));
        distCoeffMap.put("p2", distCoeffs.get(0, 3));
        distCoeffMap.put("k3", distCoeffs.get(0, 4));

        Yaml yaml = new Yaml();
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        yaml.dump(data, writer);

    }


    public void readCameraParam() {

        final String calibrationFilePath = System.getProperty("user.dir") + File.separator + CALIBRATION_FILE;
        Yaml yaml = new Yaml();

        Mat[] params = new Mat[]{
                new Mat(new Size(3, 3), CvType.CV_64FC1, new Scalar(0.0)),
                new Mat(new Size(5, 1), CvType.CV_64FC1, new Scalar(0.0))
        };

          /*
            Intrinsic parameters
            fx  0   cx
            0   fy  cy
            0   0   1
         */
        params[0].put(2, 2, 1.0);
//        params[0].put(0, 0, 1.0);
//        params[0].put(1, 1, 1.0);

         /*
            distortion Cooefficient vector
            k1  k2  p1  p2  k3
         */

        try {
            InputStream ios = new FileInputStream(new File(calibrationFilePath));
            // Parse the YAML file and return the output as a series of Maps and Lists
            Map<String, Map> result = (Map<String, Map>) yaml.load(ios);

            for (String key : result.keySet()) {

                Map<String, double[]> subValues = result.get(key);
//                System.out.println(key);

                for (String subValueKey : subValues.keySet()) {

                    Object obj = subValues.get(subValueKey);
                    ArrayList a = (ArrayList) obj;
                    Double value = (Double) a.get(0);

                    switch (subValueKey) {
                        case "fx":
                            params[0].put(0, 0, value);
                            break;
                        case "fy":
                            params[0].put(1, 1, value );
                            break;
                        case "cx":
                            params[0].put(0, 2, value);
                            break;
                        case "cy":
                            params[0].put(1, 2, value );
                            break;
                        case "p1":
                            params[1].put(0, 2, value);
                            break;
                        case "p2":
                            params[1].put(0, 3, value);
                            break;
                        case "k1":
                            params[1].put(0, 0, value);
                            break;
                        case "k2":
                            params[1].put(0, 1, value);
                            break;
                        case "k3":
                            params[1].put(0, 4, value);
                            break;
                        default:
                            System.out.println("Incorrect YAML file");
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Configuration.getInstance().setIntrinsicParam(params[0]);
        Configuration.getInstance().setDistCoeffs(params[1]);
    }

    @Override
    public void notifyObservers() {

        for (ICalibrationObserver observer : observers) {
            observer.getNotified();
        }
    }

    @Override
    public void subscribeObserver(ICalibrationObserver observer) {
        observers.add(observer);
    }

    @Override
    public void unsubscribeObserver(ICalibrationObserver observer) {
        observers.remove(observer);

    }
}
