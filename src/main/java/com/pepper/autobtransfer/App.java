package com.pepper.autobtransfer;

import com.pepper.autobtransfer.bluetooth.BTConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        scene = new Scene(loadFXML("primary"));
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void stop() throws Exception 
    {
        super.stop();
        if(BTConnection.watchService != null){
            BTConnection.watchService.close();
            System.out.println("watchService.close()");
        }
        if (BTConnection.putOperation != null) {
            BTConnection.putOperation.close();
            System.out.println("putOperation.close()");
        }
        if(BTConnection.executorService != null && !BTConnection.executorService.isShutdown()){
            BTConnection.executorService.shutdown();
            System.out.println("executorService.shutdown()");
        }
        
        
    }
}