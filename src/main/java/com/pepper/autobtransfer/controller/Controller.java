package com.pepper.autobtransfer.controller;

import static com.pepper.autobtransfer.App.primaryStage;
import com.pepper.autobtransfer.bluetooth.BTConnection;
import com.pepper.autobtransfer.ui.BTconnectAnimation;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;


public class Controller implements Initializable
{
    private BTConnection bTConnection;
    private FileManager FileManager;
    private BTconnectAnimation connAnimation;
    private Path folderPath;
    @FXML
    private TextField deviceNameTxtF;
    @FXML
    private Spinner spinner;   
    @FXML
    private StackPane stackP, logo;
    @FXML
    private Circle outer, outer1, middle, middle1, inner, innerinner, innerinner12, innerinner1, innerinner11;
    public static List<Circle> circles;
    @FXML
    private Label infoLabel, infoLabel1, errorLbl;
    @FXML
    private Button chooseConnectBtn, sendNowBtn;

    

    @Override  
    public void initialize(URL location, ResourceBundle resources) 
    {
        circles = new ArrayList<>();
        circles.add(innerinner11);
        circles.add(innerinner1);
        circles.add(innerinner12);
        circles.add(innerinner);
        circles.add(inner);
        circles.add(middle);
        circles.add(middle1);
        circles.add(outer1);
        circles.add(outer);
        
        bTConnection = new BTConnection(this);
        FileManager = new FileManager(this);
        connAnimation = new BTconnectAnimation(this, bTConnection);
        
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 24, 0.1, 0.5);
        spinner.setValueFactory(valueFactory);
        
        setTooltip();
        
        //connAnimation.startAnimation(circles);
        //connect.checkBluetoothAvailability();
        
    }
    
    @FXML
    public void selectFolderToWatch()
    {
        if(!"".equals(deviceNameTxtF.getText()) && deviceNameTxtF.getText() != null)
        {
            double spinValue = (double) spinner.getValue();
            int period = (int) (spinValue * 60*60);
            System.out.println("period " + period);
            folderPath = FileManager.selectDirectory();
            
            bTConnection.watchFolder(folderPath, deviceNameTxtF.getText(), period);
            sendNowBtn.setDisable(false);
        } else {            
            System.out.println("Enter your device name");
        }
    }
    
    @FXML
    public void sendNow()
    {
        if(!"".equals(deviceNameTxtF.getText()) && deviceNameTxtF.getText() != null)
        {
            System.out.println("SendNow");
            System.out.println(deviceNameTxtF.getText());
            bTConnection.watchFolder(folderPath, deviceNameTxtF.getText());
        } else {
            setErrorLblText("Please check device name");
        }
    }
    
    public void setErrorLblText(String txt)
    {
        Platform.runLater(() -> {
        errorLbl.setText(txt);
        });
    }
    
    private void setTooltip()
    {
        Tooltip tooltip = new Tooltip("Please enter your device name");        
        tooltip.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: #ffffff; " +
            "-fx-font-size: 12px;"
        );
        tooltip.setShowDelay(Duration.ZERO);
        if (deviceNameTxtF.getText().isEmpty()) {
            chooseConnectBtn.setTooltip(tooltip);
        }
        
        deviceNameTxtF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) 
            {
                if (newValue.isEmpty()) {
                    chooseConnectBtn.setTooltip(tooltip);
                } else {
                    chooseConnectBtn.setTooltip(null);
                }
            }
        }); 
    }

    public StackPane getLogo() {
        return logo;
    }

    public StackPane getStackP() {
        return stackP;
    }

    public Circle getOuter() {
        return outer;
    }

    public Circle getMiddle() {
        return middle;
    }

    public Circle getInner() {
        return inner;
    }
    
    public Label getInfoLabel() {
        return infoLabel;
    }
    public Label getInfoLabel1() {
        return infoLabel1;
    }
    public Label getErrorLbl() {
        return errorLbl;
    }
    
}
