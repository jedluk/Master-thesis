package controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import utility.Configuration;
import utility.Conversion;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class TemplatesController implements Initializable {

    private Configuration configuration;
    private int counter;

    @FXML
    private StackPane stackPane;

    @FXML
    private ImageView image;

    @FXML
    private Label PathLabel;

    @FXML
    private JFXButton loadTemplateButton;

    @FXML
    private JFXButton previousTemplateButton;

    @FXML
    private JFXButton nextTemplateButton;
    @FXML
    private JFXCheckBox templatesLoadedComboBox;
    @FXML
    private JFXTextField templateNameLabel;

    @FXML
    private JFXComboBox<String> templatesNameComboBox;

    @FXML
    private JFXButton bindTemplateButton;
    @FXML
    private Label templatesPath;


    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        configuration = Configuration.getInstance();
        checkIfTemplatesAreAlreadySet();

    }


    @FXML
    void bindTemplates() {
        if (templatesNameComboBox.getItems().size() != 0) {
            String newElement = templateNameLabel.getText().toUpperCase();
            templatesNameComboBox.getItems().add(counter, newElement);
            templatesNameComboBox.getItems().remove(counter + 1, counter + 2); // remove next value from list - now its old value
            templateNameLabel.setText("");
            setTemplatesMap();
            if (counter < configuration.getTemplates().size() - 1) {
                image.setImage(Conversion.mat2img(configuration.getTemplates().get(++counter)));
                templatesNameComboBox.getSelectionModel().select(counter);

            }
        }

    }

    @FXML
    void nextTemplate() {
        if (configuration.getPathsToTemplates().size() != 0 && this.counter < configuration.getPathsToTemplates().size() - 1) {
            image.setImage(Conversion.mat2img(configuration.getTemplates().get(++counter)));
            PathLabel.setText(configuration.getPathsToTemplates().get(counter));
            templatesNameComboBox.getSelectionModel().select(counter);
        }
    }

    @FXML
    void previousTemplate() {
        if (configuration.getPathsToTemplates().size() != 0 && this.counter > 0) {
            image.setImage(Conversion.mat2img(configuration.getTemplates().get(--counter)));
            PathLabel.setText(configuration.getPathsToTemplates().get(counter));
            templatesNameComboBox.getSelectionModel().select(counter);
        }
    }
    @FXML
    private void setSelectedImage() {

        if(templatesNameComboBox.getSelectionModel().getSelectedItem() != null){
            image.setImage(Conversion.mat2img(configuration.getTemplates().get(templatesNameComboBox.getSelectionModel().getSelectedIndex())));
            counter = templatesNameComboBox.getSelectionModel().getSelectedIndex();
        }
    }


    @FXML
    void loadTemplates() {

        List<File> files;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose templates");
        fileChooser.setInitialDirectory(new File(configuration.getUSER_DIR()));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.tiff"));

        try { // capture all image and create set
            files = fileChooser.showOpenMultipleDialog(templatesPath.getScene().getWindow());

            if (configuration.clearTemplates()){ // check if templates are loaded already
                counter = 0;
                templatesNameComboBox.getItems().clear();
            }
            for (File file : files) {
                configuration.getPathsToTemplates().add(file.getPath());
            }
            configuration.setTemplates();
            setUpWindow();

        } catch (RuntimeException ex) {
            ex.toString();
        }
    }

    public void setUpWindow() {

        templateNameLabel.setDisable(false);
        templatesNameComboBox.setDisable(false);
        templatesLoadedComboBox.setSelected(true);
        loadComboBoxValues();
        templatesNameComboBox.getSelectionModel().select(0); // set selected in Combo as 0 item
        templatesPath.setText(configuration.getUSER_DIR());
        PathLabel.setText(configuration.getPathsToTemplates().get(counter));
        image.setImage(Conversion.mat2img(configuration.getTemplates().get(counter)));
    }

    private void checkIfTemplatesAreAlreadySet() {

        if (configuration.getTemplates().size() > 0) {
            setUpWindow();
        }
    }

    private void loadComboBoxValues() {
        // check if templatesMap is already set
        if (configuration.getTemplatesMap().size() > 0) {
            templatesNameComboBox.getItems().addAll(configuration.getTemplatesMap().keySet());
        } else {
            for (int i = 0; i < configuration.getTemplates().size(); i++) {
                templatesNameComboBox.getItems().add("Template" + String.valueOf(i));
            }
            configuration.setTemplatesLinkedHashMap(templatesNameComboBox.getItems());
        }

    }

    private void setTemplatesMap() {

        if (configuration.getTemplatesMap().size() > 0) {
            configuration.getTemplatesMap().clear();
            configuration.setTemplatesLinkedHashMap(templatesNameComboBox.getItems());
        } else {
            configuration.setTemplatesLinkedHashMap(templatesNameComboBox.getItems());
        }
    }


}
