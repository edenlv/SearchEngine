package View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("SearchEngine.fxml"));
        primaryStage.setTitle("Information Retrieval - Search Engine by Eden & Yakov");
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        scene.getStylesheets().add(new File("src/View/style.css").toURI().toURL().toExternalForm());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
