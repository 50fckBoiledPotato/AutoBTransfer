package com.pepper.autobtransfer.controller;

import com.pepper.autobtransfer.bluetooth.BTConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;


public class Controller implements Initializable
{
    private BTConnection connect;
    private FileManager FileManager;
    private Path folderPath;
    @FXML
    private TextField deviceNameTxtF;
    @FXML
    private Spinner spinner;
    

    @Override  
    public void initialize(URL location, ResourceBundle resources) 
    {
        connect = new BTConnection();
        FileManager = new FileManager(this);
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 24, 0.1, 0.5);
        spinner.setValueFactory(valueFactory);
    }
    
    @FXML
    public void selectFolderToWatch()
    {
        if(!"".equals(deviceNameTxtF.getText()) || deviceNameTxtF.getText() != null)
        {
            double spinValue = (double) spinner.getValue();
            int period = (int) (spinValue * 60*60);
            System.out.println("perdiod " + period);
            folderPath = FileManager.selectDirectory();
            
            connect.watchFolder(folderPath, deviceNameTxtF.getText(), period);
        }
    }
    
    @FXML
    public void sendNow()
    {
        if(!"".equals(deviceNameTxtF.getText()) || deviceNameTxtF.getText() != null)
        {
            System.out.println("SendNow");
            connect.watchFolder(folderPath, deviceNameTxtF.getText());
        } else {
            System.out.println("something wrong");
        }
    }
    
}
