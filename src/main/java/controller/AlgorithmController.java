package controller;

import algorithm.Processing;
import algorithm.Regex;
import algorithm.Tuning;
import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import utility.Configuration;
import utility.Conversion;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AlgorithmController implements Initializable {

    private Configuration configuration;
    private Tuning tuning;
    private int counter;

    @FXML
    private StackPane stackPane;

    @FXML
    private JFXHamburger extendHamburger;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private VBox originalImageBox;
    @FXML
    private JFXCheckBox templateFoundCheckBox;
    @FXML
    private GridPane gridPane;
    @FXML
    private Label processingTimeLabel;
    @FXML
    private ImageView originalImage;
    @FXML
    private ImageView processedImage;
    @FXML
    private AnchorPane tuningPane;
    @FXML
    private JFXCheckBox regionReadyCheckBox;

    @FXML
    private Label roiProcessingTime;

    @FXML
    private JFXToggleButton adaptiveToggle;

    @FXML
    private JFXTextField adaptiveBlockSize;

    @FXML
    private JFXToggleButton gradientToggle;

    @FXML
    private JFXTextField gradientDimension;

    @FXML
    private JFXTextField contourSize;

    @FXML
    private JFXTextField rectangleWidth;

    @FXML
    private JFXTextField rectangleHeight;
    @FXML
    private HBox ocrPanel;
    @FXML
    private JFXListView<String> resultList = new JFXListView<>();
    @FXML
    private JFXCheckBox filterOutput;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // get configuration file
        configuration = Configuration.getInstance();
        setUpTuningObject();
        // load drawer Pane
        loadSidePane();
        // set transition on Hamburger
        setHabmurgerTransition();
        // templates exist ??
    }

    @FXML
    private void selectPreviousImage() {

        if (tuning.getTestImages().size() > 0 && this.counter > 0) {
            originalImage.setImage(Conversion.mat2img(tuning.getTestImages().get(--counter)));
            processedImage.setImage(Conversion.mat2img(tuning.getTestImages().get(counter)));
            tuning.setOriginalImage(tuning.getTestImages().get(counter));
        }
    }

    @FXML
    private void selectNextImage() {

        if (tuning.getTestImages().size() > 0 && this.counter < tuning.getTestImages().size() - 1) {
            originalImage.setImage(Conversion.mat2img(tuning.getTestImages().get(++counter)));
            processedImage.setImage(Conversion.mat2img(tuning.getTestImages().get(counter)));
            tuning.setOriginalImage(tuning.getTestImages().get(counter));
        }
    }

    @FXML
    private void loadImages() {

        List<File> files;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose images to test");
        fileChooser.setInitialDirectory(new File(configuration.getUSER_DIR()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.tiff"));

        try { // capture all test images
            files = fileChooser.showOpenMultipleDialog(stackPane.getScene().getWindow());
            // if test images are already loaded clear them, and clear all Window
            if (tuning.clearTestImages()) {
                counter = 0;
                processedImage.setImage(null);
                originalImage.setImage(null);
            }

            for (File file : files) {
                tuning.getPathToTestImages().add(file.getAbsolutePath());
            }
            // create Mat objects based on file paths
            tuning.setTestImages();
            tuning.setOriginalImage(tuning.getTestImages().get(0));
            cleanTabPane();
            setUpImages();
        } catch (RuntimeException ex) {
            ex.toString();
        }

    }

    @FXML
    private void findTemplate() {

        if (tuning.getOriginalImage() != null) {
            long start = System.currentTimeMillis();
            boolean multiThreads = true;
            if ( multiThreads )
            tuning.runMatchingSimultaneously();
            else {
                tuning.matchTemplate();
            }
            long stop = System.currentTimeMillis();
            if (tuning.getTemplateFound() != null) {
                processedImage.setImage(Conversion.mat2img(tuning.getTemplateFound()));
                originalImage.setImage(Conversion.mat2img(tuning.getProcessingImage()));
                tuningPane.setDisable(false);
            } else {
                noTemplatesFoundDialog();
            }
            templateFoundCheckBox.setSelected(true);
            processingTimeLabel.setText("Processing time:\n" + String.valueOf((stop - start) / Math.pow(10, 3)) + "s");
        }
    }

    @FXML
    public void backToOriginal() {

        cleanTabPane();
    }

    @FXML
    public void seekForROI() {
        // parsuj pola

        if (parseUserArguments()) {
            long start = System.currentTimeMillis();
            tuning.setDEBUGGING_MODE(true);
            tuning.findInterestRegions(gradientToggle.isSelected(), Integer.valueOf(gradientDimension.getText()),
                    adaptiveToggle.isSelected(), Integer.valueOf(adaptiveBlockSize.getText()),
                    Integer.valueOf(rectangleWidth.getText()), Integer.valueOf(rectangleHeight.getText()),
                    Integer.valueOf(contourSize.getText()));
            processedImage.setImage(Conversion.mat2img(tuning.getMorphologyProcess()));
            originalImage.setImage(Conversion.mat2img(tuning.getProcessingImage()));
            // GUI update
            regionReadyCheckBox.setSelected(true);
            roiProcessingTime.setText("Processing time:\n" + (System.currentTimeMillis() - start) / Math.pow(10, 3) + "s");
            ocrPanel.setDisable(false);
        }
    }


    @FXML
    void runOCR() {

        new Thread(() -> {
            if (filterOutput.isSelected()) {

                List<String> results = Regex.filterPrices(tuning.getOcr().doOCRFromMatList().split("\n\n"));
                Platform.runLater(() -> {
                    for (String res : results) {
                        resultList.getItems().add(res + " on station " + tuning.getPetrolStationDetected());
                    }
                });

            } else {
                Platform.runLater(() -> {
                    for (String res : tuning.getOcr().doOCRFromMatList().split("\n\n")) {
                        resultList.getItems().add(res + " on station " + tuning.getPetrolStationDetected());
                    }
                });

            }
        })    .start();
        // new thread (may

    }

    @FXML
    public void changeAdaptiveToggleState() {
        if (adaptiveToggle.isSelected()) {
            adaptiveBlockSize.setDisable(false);
        } else {
            adaptiveBlockSize.setDisable(true);
        }
    }

    private void setHabmurgerTransition() {

        HamburgerBackArrowBasicTransition transition = new HamburgerBackArrowBasicTransition(extendHamburger);
        transition.setRate(-1);
        extendHamburger.addEventHandler(MouseEvent.MOUSE_CLICKED, (e) -> {
                    transition.setRate(transition.getRate() * (-1));
                    transition.play();

                    if (drawer.isShown()) {
                        originalImageBox.setVisible(true);
                        drawer.close();
                    } else {
                        originalImageBox.setVisible(false);
                        drawer.open();
                    }

                }
        );
    }

    private void loadSidePane() {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/drawerContent.fxml"));
            VBox box = loader.load();
            processedImage = (ImageView) loader.getNamespace().get("processedImage");
            drawer.setSidePane(box);
            drawer.setOverLayVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot load drawerContent !!!");
        }
    }

    private void setUpImages() {

        originalImage.setImage(Conversion.mat2img(tuning.getOriginalImage()));
        processedImage.setImage(Conversion.mat2img(tuning.getOriginalImage()));

    }

    private void setUpTuningObject() {

        if (configuration.getTemplates().size() > 0) {
            // create new tuning object
            tuning = new Tuning();
            tuning.createTmpFolder();
            tuning.setTemplates(configuration.getTemplates());
            setUpTemplatesImages();

        } else {
            showNoTemplatesDialog();
        }
    }

    private void setUpTemplatesImages() {

        for (int i = 0; i < tuning.getTemplates().size(); i++) {

            ImageView imageView = new ImageView();
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setImage(Conversion.mat2img(tuning.getTemplates().get(i)));
            gridPane.add(imageView, i / 2, i % 2);
        }
    }

    private void showNoTemplatesDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\t\tNo templates found !!!"));
        content.setBody(new Text("Because none template is loaded, the algorithm tuning process cannot be\n" +
                "performed. Go to templates window and add some patterns."));

        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.LEFT);

        JFXButton button = new JFXButton("OK");
        button.setStyle("-fx-background-color: #8BC34A");
        button.setOnAction(event -> {
            dialog.close();
            stackPane.getScene().getWindow().hide();

        });

        content.setActions(button);
        // additional close when user doesn't close window from pop up
        closeStageWhenTemplatesIsNotLoaded();
        dialog.show();
    }

    private void closeStageWhenTemplatesIsNotLoaded() {

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        Duration duration = Duration.millis(5000);
        EventHandler onFinished = event -> stackPane.getScene().getWindow().hide();
        final KeyFrame kf = new KeyFrame(duration, onFinished);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }


    private void noTemplatesFoundDialog() {

        JFXDialogLayout content = new JFXDialogLayout();
        content.setHeading(new Text("\t\t\t\t\t\tFAILURE !!!"));
        content.setBody(new Text("No templates found on the processed image"));
        JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.RIGHT);
        JFXButton button = new JFXButton("OK");
        button.setStyle("-fx-background-color: #536DFE");
        button.setOnAction(event -> {
            dialog.close();
        });

        content.setActions(button);
        dialog.show();

    }

    private boolean parseUserArguments() {

        // better approach is create validators
        try {
            Integer.parseInt(gradientDimension.getText());
            Integer.parseInt(adaptiveBlockSize.getText());
            Integer.parseInt(rectangleWidth.getText());
            Integer.parseInt(rectangleHeight.getText());
            Integer.parseInt(contourSize.getText());
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Incorrect fields !!! Check if arguments are integer ");
            return false;
        }
    }

    private void cleanTabPane() {

        originalImage.setImage(null);
        processedImage.setImage(null);
        templateFoundCheckBox.setSelected(false);
        regionReadyCheckBox.setSelected(false);
        processingTimeLabel.setText("Processing time:");
        roiProcessingTime.setText("Processing time:");
        tuningPane.setDisable(true);
    }

    public void saveCurrentParametrs(Processing processing){
        processing.setGradientActive(!adaptiveToggle.isSelected());
        processing.setGradientSize(Integer.parseInt(gradientDimension.getText()));
        processing.setAdaptiveActive(adaptiveToggle.isSelected());
        processing.setAdaptiveSize(Integer.parseInt(adaptiveBlockSize.getText()));
        processing.setRectangleWidth(Integer.parseInt(rectangleWidth.getText()));
        processing.setRectangleHeight(Integer.parseInt(rectangleHeight.getText()));
        processing.setContourSize(Integer.parseInt(contourSize.getText()));
    }

    // algorithm actions

}
