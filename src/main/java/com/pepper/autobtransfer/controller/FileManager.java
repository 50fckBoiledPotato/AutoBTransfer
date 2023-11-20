package com.pepper.autobtransfer.controller;

import com.pepper.autobtransfer.App;
import java.io.File;
import java.nio.file.Path;
import javafx.stage.DirectoryChooser;


public class FileManager 
{
    private Controller controller;

    FileManager(Controller aThis) 
    {
        this.controller = aThis;
    }
    
    public Path selectDirectory()
    {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a Folder");
        Path path = null;

        // Show the directory chooser dialog
        File selectedDirectory = directoryChooser.showDialog(App.primaryStage);

        // Check if the user selected a directory
        if (selectedDirectory != null) {
            String selectedFolderPath = selectedDirectory.getAbsolutePath();
            path = selectedDirectory.toPath();
            System.out.println("Selected Folder: " + selectedFolderPath);
        } else {             
            System.out.println("No folder selected.");
            
        }
       return path;
    }
    

}
