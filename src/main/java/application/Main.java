package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import library.Bootstrap;
import library.FFmpegLoader;
import library.Tess4jAssetsLoader;
import utility.Files;

/**
 * Created by jedrzej on 2017-07-30.
 */
public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Vision system to recognize & identify fuel prices");
        primaryStage.initStyle(StageStyle.UNIFIED);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            Files.cleanAfterQuit();
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        Files.createUtilsFolder();
        new Bootstrap();
        new FFmpegLoader();
        new Tess4jAssetsLoader();
        launch(args);
    }
}
