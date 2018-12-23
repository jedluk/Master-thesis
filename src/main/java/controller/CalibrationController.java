package controller;

import calibration.ICalibrationObserver;
import calibration.Set;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import utility.Configuration;
import utility.Conversion;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class CalibrationController implements Initializable, ICalibrationObserver {

    private int counter;
    private Configuration configuration;

    @FXML
    private StackPane stackPane;
    @FXML
    private ImageView image;
    @FXML
    private Label PathLabel;
    @FXML
    private JFXCheckBox setCalibratedCheckBox;
    @FXML
    private JFXCheckBox saveParam;
    @FXML
    private JFXCheckBox readySet;
    @FXML
    private Label calibFilePath;
    @FXML
    private JFXCheckBox scaleParametersCheckBox;
    @FXML
    private JFXTextField scaleFactor;

    private JFXDialog processingDialog;

    public void initialize(URL location, ResourceBundle resources) {
        configuration = Configuration.getInstance();
        if (new File(configuration.getCalibrationFilePath()).exists()) {
            foundCalibrationFileDialog();
        }
    }

    public void calibrateSet() {

        double myScaleFactor = 0.0d;
        if(scaleParametersCheckBox.isSelected()) {
            Pattern scalePattern = Pattern.compile("^((0\\.\\d)|1|0)$");
            Matcher m = scalePattern.matcher(scaleFactor.getText());
            if (m.find()) {
                myScaleFactor = Double.valueOf(scaleFactor.getText());
            } else {
                showIncorrectFactorDialog();
            }
        }

        if (configuration.getSet() != null && configuration.getSet().getUncalibratedSet().length > 0) {
            showProcessingDialog();

            double finalMyScaleFactor = myScaleFactor;
            new Thread(() -> {
                configuration.getCalib().subscribeObserver(this);
                configuration.getCalib().calibrateCamera(9, 6, configuration.getSet()
                        .getUncalibratedSet().length, saveParam.isSelected(),scaleParametersCheckBox.isSelected(), finalMyScaleFactor);
            }).start();

        }
    }

    private void showIncorrectFactorDialog() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\t\tIncorrect scale factor!"));
        content.setBody(new Text("Scaling factor must be double value in range 0-1"));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);
        JFXButton button = new JFXButton("OK");
        button.setStyle("-fx-background-color: #00796B;-fx-min-width: 50px");
        button.setOnAction(event -> dialog.close());
        content.setActions(button);
        dialog.show();
    }

    public void PreviousPicture() {
        if (configuration.getCalibrationImagesPaths().size() != 0 && this.counter > 0) {
            if (configuration.isCalibrated()) {
                image.setImage(Conversion.mat2img(configuration.getSet().getCalibratedSet()[--counter]));
            } else {
                image.setImage(Conversion.mat2img(configuration.getSet().getUncalibratedSet()[--counter]));
            }
            PathLabel.setText(configuration.getCalibrationImagesPaths().get(counter));
        }
    }

    public void nextPicture() {
        if (configuration.getCalibrationImagesPaths().size() != 0 && this.counter < configuration.getCalibrationImagesPaths().size() - 1) {
            if (configuration.isCalibrated()) {
                image.setImage(Conversion.mat2img(configuration.getSet().getCalibratedSet()[++counter]));
            } else {
                image.setImage(Conversion.mat2img(configuration.getSet().getUncalibratedSet()[++counter]));
            }
            PathLabel.setText(configuration.getCalibrationImagesPaths().get(counter));
        }
    }

    public void loadSet() {

//        this.readySet.setSelected(false);
//        this.setCalibratedCheckBox.setSelected(false);

        List<File> files;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose images to calibrate camera");
        fileChooser.setInitialDirectory(new File(configuration.getUSER_DIR()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.tiff"));

        try { // capture all image and create set
            files = fileChooser.showOpenMultipleDialog(stackPane.getScene().getWindow());
            for (File file : files) {
                //System.out.println(file.getPath());
                configuration.getCalibrationImagesPaths().add(file.getPath());
            }
            this.readySet.setSelected(true);
            configuration.setSet(new Set(configuration.getCalibrationImagesPaths()));
            image.setImage(Conversion.mat2img(configuration.getSet().getUncalibratedSet()[counter]));
            PathLabel.setText(configuration.getCalibrationImagesPaths().get(counter));
        } catch (RuntimeException ex) {
            ex.toString();
        }
    }

    private void foundCalibrationFileDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\tCalibration file found !!!"));
        content.setBody(new Text("There is a valid calibration file in " + configuration.getUSER_DIR() + " directory. \nDo you want to use it ?"));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.TOP);

        JFXButton accept = new JFXButton("YES");

        accept.setStyle("-fx-background-color: #8BC34A; -fx-min-width: 50px;-fx-border-radius: 3px");
        accept.setOnAction(event -> {
            configuration.setCalibrated(true);
            configuration.getCalib().readCameraParam();
            String parameterSheet = "Parameter sheet: \n" + configuration.getCalibrationFilePath();
            calibFilePath.setText(parameterSheet);
            System.out.println("Calibration file is saved in configuration");
            dialog.close();
        });


//        JFXButton refuse = new JFXButton("NO");
//        refuse.setStyle("-fx-background-color: #00796B; -fx-min-width: 80px; -fx-border-color: #FFFFFF; -fx-border-width: 5;");
//        refuse.setOnAction(event -> dialog.close());

        content.setActions(accept);
        dialog.show();
    }

    private void showProcessingDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\t\tCalibrating camera...")); //Vision system to recognize and identify fuel prices
        JFXSpinner spinner = new JFXSpinner();
        content.setBody(spinner);
        processingDialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);
        processingDialog.overlayCloseProperty().setValue(false);
        processingDialog.show();
    }


    @FXML
    private void toggleScaleParameters() {
        if(scaleParametersCheckBox.isSelected() ){
            scaleFactor.setDisable(false);
        } else {
            scaleFactor.setDisable(true);
        }
    }


    @Override
    public void getNotified() { // now set is calibrated
        Platform.runLater(() -> {
            processingDialog.close();
            setCalibratedCheckBox.setSelected(true);
            image.setImage(Conversion.mat2img(configuration.getSet().getCalibratedSet()[counter]));
            if (saveParam.isSelected()) {
                calibFilePath.setText("Parameter sheet:\n" + configuration.getUSER_DIR());
            }
        });

    }
}
