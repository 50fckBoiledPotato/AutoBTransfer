package com.pepper.autobtransfer.ui;

import com.pepper.autobtransfer.bluetooth.BTConnection;
import com.pepper.autobtransfer.controller.Controller;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;


public class BTconnectAnimation 
{
    private Controller controller;
    private BTConnection bTConnection;
    private String svgPathData = "M292.6 171.1 L249.7 214 l-0.3 -86 l43.2 43.1 m-43.2 219.8 l43.1 -43.1 l-42.9 -42.9 l-0.2 86 Z M416 259.4 C416 465 344.1 512 230.9 512 S32 465 32 259.4 S115.4 0 228.6 0 S416 53.9 416 259.4 Z m-158.5 0 l79.4 -88.6 L211.8 36.5 v176.9 L138 139.6 l-27 26.9 l92.7 93 l-92.7 93 l26.9 26.9 l73.8 -73.8 l2.3 170 l127.4 -127.5 l-83.9 -88.7 Z";
    private SVGPath bluetoothLogo;
    public static Timeline timeline;
    private FadeTransition fadeTransition;
    List<Circle> circles;
    List<Color> greens;
    private int count = 0;
    
    public BTconnectAnimation(Controller controller, BTConnection bTConnection) {
        this.controller = controller;
        this.bTConnection = bTConnection;
        bluetoothLogo = new SVGPath();
        bluetoothLogo.setContent(svgPathData);
        bluetoothLogo.setFill(Color.BLACK);
        bluetoothLogo.setScaleX(0.1);
        bluetoothLogo.setScaleY(0.1);
        controller.getLogo().getChildren().add(bluetoothLogo);
        
        setCircleFill("unavailable");
        
        startAnimation(controller.circles, 1);
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
                @Override
                public void run() 
                {
                    Platform.runLater(() -> {
                        if(bTConnection.checkBluetoothAvailability()){
                            fadeTransition.stop();    
                            controller.getInfoLabel().setText("Bluetooth available: \n" + "" + bTConnection.getLocalDeviceName());
                            setCircleFill("available");
                        } else {
                            setCircleFill("error");
                            controller.getInfoLabel().setText("Error while sending file.");
                            controller.getInfoLabel1().setText("To try again, press Send now");
                            System.out.println("fasz " + bTConnection.isBluetoothAvailable());
                        }                  
                    });
                }
            }, 5000);
        
    }
    
    public void startAnimation(List<Circle> circles, int state)
    {
        this.circles = circles;
        for (int i = 0; i < circles.size(); i++) 
        {
             switch(state){
                 case 0: animateAvailable(circles.get(i));
                 break;
                 case 1: animateConnected(circles.get(i), i);
                 break;
                 case 2: animateUnavailable(circles.get(i), i);
                 break;
             }
                         
         }
    }
    
    private Circle createCircle(double radius, Color color) {
        Circle circle = new Circle(radius);
        circle.setFill(color);
        return circle;
    }

    private void animateAvailable(Circle circle) {
        fadeTransition = new FadeTransition(Duration.seconds(2.5), circle);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(0.6);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setDelay(Duration.millis(0.7));

        fadeTransition.play();
    }
    
    private void animateConnected(Circle circle, int delayMillis) {        
        fadeTransition = new FadeTransition(Duration.seconds(2.5), circle);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1);
        fadeTransition.setCycleCount(FadeTransition.INDEFINITE);
        fadeTransition.setDelay(Duration.millis(0.7));

        fadeTransition.play();
    }
    
    private void animateUnavailable(Circle circle, int delayMilis){
        
    }
    
    public void setLogoFill(Color color)
    {
        bluetoothLogo.setFill(color);
    }
    
    public void setCircleFill(String availability)
    {
        controller.circles.forEach(circle -> {
            circle.getStyleClass().add(availability);
        });
    }
}











/*Timer timer = new Timer();
        timer.schedule(new TimerTask() 
            {
                @Override
                public void run() 
                {
                    Platform.runLater(() -> {
                        if(isOn){
                            timeline.stop();
                            System.out.println("timer stopped");
                        } else {
                            System.out.println("timer null");
                        }
                        
                    });
                }
            }, 2000);*/