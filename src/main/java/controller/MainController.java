package controller;

import algorithm.ITemplateObserver;
import algorithm.Processing;
import algorithm.Tuning;
import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FileUtils;
import utility.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class MainController implements Initializable, IFFmpegExtractorObserver, ITemplateObserver {

    private Configuration configuration;
    private FFmpegFrameExtractor ffmpegFrameExtractor;
    private Processing processing;
    private int currentProcessingFrame;
    private int FPS_VALUE = 3;

    @FXML
    private StackPane stackPane;
    @FXML
    private JFXCheckBox cameraCalibratedCheckBox;
    @FXML
    private JFXCheckBox templatesReadyCheckBox;
    @FXML
    private JFXCheckBox movieReadyCheckBox;
    @FXML
    private JFXCheckBox autoMode;
    @FXML
    private JFXCheckBox filterOutput;
    @FXML
    private Label parameterSheetLabel;
    @FXML
    private Label templatesPathLabel;
    @FXML
    private Label moviePathLabel;
    @FXML
    private ImageView processingImage;
    @FXML
    private JFXButton playMovieButton;
    @FXML
    private JFXButton previousFramesButton;
    @FXML
    private JFXButton previousFrameButton;
    @FXML
    private JFXButton nextFrameButton;
    @FXML
    private JFXButton nextFramesButton;
    @FXML
    private JFXButton processCurrentFrameButton;
    @FXML
    private JFXCheckBox calibratedFramesCB;
    @FXML
    private JFXDrawer rightDrawer;
    @FXML
    private JFXHamburger rightHamburger;

    @FXML
    private Pane drawerPane;
    @FXML
    private
    JFXListView<String> readPrices;

    private HamburgerBackArrowBasicTransition showRightDrawer;
    // reading frames from video dialog
    private JFXDialog processingDialog;

    public void initialize(URL location, ResourceBundle resources) {

        configuration = Configuration.getInstance();    // call singleton !!!
        processing = new Processing();                  // used for processing !!!
        setUpDrawers();                                 // set up existing drawers !!!
        initHamburgersTransition();                     // and ... initialize Hamburgers actions !!!
    }

    private void initHamburgersTransition() {

        showRightDrawer = new HamburgerBackArrowBasicTransition(rightHamburger);
        showRightDrawer.setRate(-1);

        rightHamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            showRightDrawer.setRate(showRightDrawer.getRate() * -1);
            showRightDrawer.play();
            if (!rightDrawer.isShown()) {
                rightDrawer.open();
            } else {
                rightDrawer.close();
            }
        });
    }

    @FXML
    void openLoadTemplatesWindow() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/templates.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Load templates");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stackPane.getScene().getWindow());
            stage.setOnCloseRequest(event -> {
                configuration = Configuration.getInstance(); // update configuration
                if (configuration.getTemplates().size() > 0) {
                    templatesReadyCheckBox.setSelected(true);
                    processing.setTemplates(configuration.getTemplates());
                    templatesPathLabel.setText(templatesPathLabel.getText() + configuration.getPathsToTemplates().get(0).substring(0, configuration.getPathsToTemplates().get(0).lastIndexOf(File.separator)));
                }
            });

            stage.show();

        } catch (IOException e) {

//            e.printStackTrace();
            System.out.println("Cannot open templates window !!!");
        }
    }

    @FXML
    void changeFrames() {
        int mode = calibratedFramesCB.isSelected() ? 1 : 0;
        switch (mode) {
            case 0:
                unCalibMode();
                break;
            case 1:
                calibMode();
                break;
            default:
                break;
        }
    }

    private void unCalibMode() {
        if (configuration.getReadyFrameSet().size() > 0) {
            configuration.setUseCalibrated(false);
            processingImage.setImage(Conversion.mat2img(configuration.getReadyFrameSet().get(currentProcessingFrame)));
        }

    }

    private void calibMode() {
        if (configuration.getReadyCalibratedSet().size() > 0) {
            configuration.setUseCalibrated(true);
            processingImage.setImage(Conversion.mat2img(configuration.getReadyCalibratedSet().get(currentProcessingFrame)));
        }
    }

    @FXML
    void openCalibrationWindow() {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/calibration.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Calibration window");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stackPane.getScene().getWindow());

            stage.setOnCloseRequest(event -> {
                configuration = Configuration.getInstance();
                if (configuration.isCalibrated()) {
                    cameraCalibratedCheckBox.setSelected(true);
                    parameterSheetLabel.setText(parameterSheetLabel.getText() + configuration.getCalibrationFilePath());
                }
                // entryTransition();
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAlgorithmWindow() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/algorithm.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.initStyle(StageStyle.UTILITY);
            stage.setTitle("Algorithm settings");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stackPane.getScene().getWindow());

            stage.setOnCloseRequest(event -> {
                configuration = Configuration.getInstance();
                // get controller and save current params
                AlgorithmController algorithmController = loader.getController();
                algorithmController.saveCurrentParametrs(processing);
                new Thread(() -> { // clear directories in new thread
                    try {
                        FileUtils.deleteDirectory(new File(Tuning.OUT_TMP_PATH));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                // entryTransition();
            });

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    void loadMovie() {

        if (configuration.checkIfTemplatesExists()) {

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose movie");
            fileChooser.setInitialDirectory(new File(configuration.getUSER_DIR()));
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Movie Files", "*.mp4", "*.avi", "*.mov"));
            this.currentProcessingFrame = 0;
            try { // capture all image and create set
                configuration.setCurrentMovie(fileChooser.showOpenDialog(stackPane.getScene().getWindow()));
                moviePathLabel.setText("Movie path: " + configuration.getCurrentMovie().getAbsolutePath());
                movieReadyCheckBox.setSelected(true);
                configuration.setUseCalibrated(calibratedFramesCB.isSelected());

                new Thread(() -> {
                    Platform.runLater(() -> {
                        showProcessingDialog();
                    });

                    Files.cleanTmpFolder();
                    ffmpegFrameExtractor = new FFmpegFrameExtractor(Files.createTmpFolder(), configuration.getCurrentMovie().getAbsolutePath(), FPS_VALUE);
                    ffmpegFrameExtractor.subscribeObserver(this);
                    ffmpegFrameExtractor.doYourJob();
                }).start();

            } catch (RuntimeException ex) {
                ex.toString();
            }
        } else {
            showNoTemplatesDialog();
        }

    }

    @FXML
    private void showInfo() {
        infoDialog();
    }

    @FXML
    void launchAuto() {

        if (configuration.getCurrentMovie() != null) {

            // start subscribing
            processing.subscribeObserver(this);
            if (configuration.getReadyCalibratedSet().size() > 0 && calibratedFramesCB.isSelected()) {
                configuration.setUseCalibrated(true);
            } else {
                configuration.setUseCalibrated(false);
            }

            processing.runMatchingSimultanesouslyFromSet();
        }
    }

    @FXML
    public void setPreviousFrame() {

        if ((currentProcessingFrame - 1) < (configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().size() : configuration.getReadyFrameSet().size())) {

            currentProcessingFrame--;
            processingImage.setImage(Conversion.mat2img(configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().get(currentProcessingFrame) :
                    configuration.getReadyFrameSet().get(currentProcessingFrame), Conversion.ImageType.PNG));
        }
    }

    @FXML
    public void setNextFrame() {

        if ((currentProcessingFrame + 1) < (configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().size() : configuration.getReadyFrameSet().size())) {

            currentProcessingFrame++;
            processingImage.setImage(Conversion.mat2img(configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().get(currentProcessingFrame) :
                    configuration.getReadyFrameSet().get(currentProcessingFrame), Conversion.ImageType.PNG));
        }
    }

    @FXML
    public void setPreviousPreviousFrame() {

        if ((currentProcessingFrame - FPS_VALUE) < (configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().size() : configuration.getReadyFrameSet().size())) {

            currentProcessingFrame -= FPS_VALUE;
            processingImage.setImage(Conversion.mat2img(configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().get(currentProcessingFrame) :
                    configuration.getReadyFrameSet().get(currentProcessingFrame), Conversion.ImageType.PNG));
        }
    }

    @FXML
    public void setNextNextFrame() {

        if ((currentProcessingFrame + FPS_VALUE) < (configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().size() : configuration.getReadyFrameSet().size())) {

            currentProcessingFrame += FPS_VALUE;
            processingImage.setImage(Conversion.mat2img(configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().get(currentProcessingFrame) :
                    configuration.getReadyFrameSet().get(currentProcessingFrame), Conversion.ImageType.PNG));
        }
    }

    /* PROCESSING SECTION  */
    @FXML
    public void processCurrentFrame() {

        // check on processing not on configuration !!!
        if (!autoMode.isSelected() && configuration.getTemplates().size() > 0 && configuration.getReadyFrameSet().size() > 0) {
            System.out.println("Processing started");
            processing.setOriginalImage(configuration.getReadyFrameSet().get(currentProcessingFrame));
            if (processing.runMatchingSimultaneously()) {
                System.out.println("Template found");
                doJobAfterTemplateFound();
            }
        }
    }

    private void launchAutoMode() {
//        showWarningOnAutoMode();
        switch (autoMode.isSelected() ? 1 : 0) {
            case 0:
                setAutoLayout();
                break;
            case 1:
                setManualLayout();
                break;
            default:
                break;
        }
    }

    private void infoDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\tVision system to recognize and identify fuel prices")); //Vision system to recognize and identify fuel prices

        HBox hbox = new HBox();
        try {
            hbox.getChildren().add(new ImageView(new Image(getClass().getResource("/assets/me.png").toURI().toURL().toString())));
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        hbox.getChildren().add(new Text("\n\n\tTarget: Diploma application\n\n" +
                "\tAuthor: Jędrzej Łukasiuk\n\n" +
                "\tCollege: West Pomeranian University of Technology\n\n" +
                "\tSubject: Information and Communication Technology\n\n" +
                "\tYear of graduation: 2017"));
        content.setBody(hbox);


        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);
        JFXButton button = new JFXButton("OK");
        button.setStyle("-fx-background-color: #8BC34A;-fx-min-width: 50px");
        button.setOnAction(event -> {
            dialog.close();
            // entryTransition();
        });
        content.setActions(button);
        dialog.show();
    }

    private void showWarningOnAutoMode() {
        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\t\tWarning !"));
        content.setBody(new Text("Please be aware that using auto mode can consume large of processor \nresources during processing . Are you sure to use it ?"));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);
        JFXButton button = new JFXButton("OK");
        button.setStyle("-fx-background-color: #8BC34A;-fx-min-width: 50px");
        button.setOnAction(event -> {
            dialog.close();
            autoMode.setSelected(true);
        });
        content.setActions(button);
        dialog.show();
        dialog.setOnDialogClosed(event -> autoMode.setSelected(false));
    }

    private void showNoTemplatesDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\tLoad your templates first !!!")); //Vision system to recognize and identify fuel prices
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);
        JFXButton button = new JFXButton("OK");
        button.setStyle("-fx-background-color: #8BC34A;-fx-min-width: 50px");
        button.setOnAction(event -> {
            dialog.close();
            // entryTransition();
        });
        content.setActions(button);
        dialog.show();
    }

    private void showProcessingDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\tExtracting frames from video ...")); //Vision system to recognize and identify fuel prices
        JFXSpinner spinner = new JFXSpinner();
        content.setBody(spinner);
        processingDialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.BOTTOM);
        processingDialog.overlayCloseProperty().setValue(false);
        processingDialog.show();
    }

    private void setManualLayout() {

        previousFrameButton.setDisable(true);
        previousFramesButton.setDisable(true);
        nextFrameButton.setDisable(true);
        nextFramesButton.setDisable(true);
        playMovieButton.setDisable(false);
        calibratedFramesCB.setDisable(true);
        processCurrentFrameButton.setDisable(true);
    }

    private void setAutoLayout() {

        previousFrameButton.setDisable(false);
        previousFramesButton.setDisable(false);
        nextFrameButton.setDisable(false);
        nextFramesButton.setDisable(false);
        playMovieButton.setDisable(true);
        calibratedFramesCB.setDisable(false);
        processCurrentFrameButton.setDisable(false);
    }

    private void setUpDrawers() {

        try {
            drawerPane = FXMLLoader.load(getClass().getResource("/fxml/settings.fxml"));
        } catch (IOException e) {
            System.out.println("Cannot load drawer !!!");
        }
        rightDrawer.setSidePane(drawerPane);
        readDrawerContent();

    }

    private void readDrawerContent() {

        // BUTTONS
        JFXButton aboutMeButton = (JFXButton) drawerPane.lookup("#aboutMeButton");
        JFXButton calibrationButton = (JFXButton) drawerPane.lookup("#calibrationButton");
        JFXButton templateButton = (JFXButton) drawerPane.lookup("#templatesButton");
        JFXButton algorithmButton = (JFXButton) drawerPane.lookup("#algorithmButton");
        JFXButton loadMovieButton = (JFXButton) drawerPane.lookup("#loadMovieButton");


        // CHECKBOXES
        cameraCalibratedCheckBox = (JFXCheckBox) drawerPane.lookup("#cameraCalibratedCheckBox");
        templatesReadyCheckBox = (JFXCheckBox) drawerPane.lookup("#templatesReadyCheckBox");
        movieReadyCheckBox = (JFXCheckBox) drawerPane.lookup("#movieReadyCheckBox");
        autoMode = (JFXCheckBox) drawerPane.lookup("#autoMode");
        calibratedFramesCB = (JFXCheckBox) drawerPane.lookup("#calibratedFramesCB");
        filterOutput = (JFXCheckBox) drawerPane.lookup("#filterOutput");
        // LABELS
        parameterSheetLabel = (Label) drawerPane.lookup("#parameterSheetLabel");
        templatesPathLabel = (Label) drawerPane.lookup("#templatesPathLabel");
        moviePathLabel = (Label) drawerPane.lookup("#moviePathLabel");

        // HBOXES
        HBox settingsHBox = (HBox) drawerPane.lookup("#settingsHBox");
        VBox parametersVBox = (VBox) drawerPane.lookup("#parametersVBox");
        HBox resultBox = (HBox) drawerPane.lookup("#resultBox");
        settingsHBox.getStyleClass().add("settingsHBox");
        parametersVBox.getStyleClass().add("parametersBox");
        resultBox.getStyleClass().add("resultBox");

        readPrices = (JFXListView<String>) drawerPane.lookup("#resultsList");
        readPrices.setExpanded(true);
        readPrices.depthProperty().set(2);
        //IF RESULTS FOUND ...

        // add events
        aboutMeButton.setOnAction(event -> showInfo());
        calibrationButton.setOnAction(event -> openCalibrationWindow());
        templateButton.setOnAction(event -> openLoadTemplatesWindow());
        algorithmButton.setOnAction(event -> openAlgorithmWindow());
        loadMovieButton.setOnAction(event -> loadMovie());

        autoMode.setOnAction(event -> launchAutoMode());
        filterOutput.setOnAction(event -> setFilterOnOutput());
        calibratedFramesCB.setOnAction(event -> changeFrames());
    }

    private void setFilterOnOutput() {
        this.processing.filterOutput = filterOutput.isSelected();
    }

    /*
        GET NOTIFIED FROM FFMPEG EXTRACTOR - IT MIGHT BE IN DIFFERENT STATES
     */
    @Override
    public void getNotifiedFromFFmpegExtractor() {

        Platform.runLater(() -> {

            switch (ffmpegFrameExtractor.getState()) {
                case NONE:
                    configuration.setReadyFrameSet(ffmpegFrameExtractor.getReadyFrameSet());
                    configuration.setReadyCalibratedSet(ffmpegFrameExtractor.getReadyCalibratedSet());
                    configuration.setUseCalibrated(calibratedFramesCB.isSelected());
                    System.out.println("Set " + (configuration.isUseCalibrated() ? "calibrated" : "uncalibrated") + " frames");
                    processingImage.setImage(Conversion.mat2img(configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().get(0) : configuration.getReadyFrameSet().get(0)));
                    processingDialog.close();
                    break;
                case READING:
                    setProcessingContent("Reading frames ...");
                    break;
                case EXTRACTING:
                    setProcessingContent("Extracting frames ...");
                    break;
                case CALIBRATING:
                    setProcessingContent("Calibrating frames ...");
                    break;
                default:
                    break;
            }
        });
    }

    // method direct intended to getNotifiedFromFFmpegExtractor method
    private void setProcessingContent(String info) {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\t\t" + info));
        content.setBody(new JFXSpinner());
        processingDialog.setContent(content);
    }

    @Override
    public void getNotifiedFromTemplateObserver() {
        processingImage.setImage(Conversion.
                mat2img(configuration.isUseCalibrated() ? configuration.getReadyCalibratedSet().get(processing.imageIndex) :
                        configuration.getReadyFrameSet().get(processing.imageIndex)));
        doJobAfterTemplateFound();
        processing.unsubscribeObserver(this);
        setManualLayout();
    }

    private void doJobAfterTemplateFound() {
        // and OCR
        Platform.runLater(() -> {
            processing.findInterestRegionsAfterTuning();
            readPrices.setStyle("-fx-background-color: #616161;");
            readPrices.getItems().addAll(processing.putFoundResults());
            if (!rightDrawer.isShown()) {
                showRightDrawer.setRate(showRightDrawer.getRate() * -1);
                showRightDrawer.play();
                rightDrawer.open();

            }
        });
    }
}
