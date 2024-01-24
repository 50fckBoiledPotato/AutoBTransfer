package com.pepper.autobtransfer.bluetooth;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.intel.bluetooth.obex.OBEXClientSessionImpl;
import com.pepper.autobtransfer.controller.Controller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.obex.Operation;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.obex.HeaderSet;
import javax.obex.ResponseCodes;

public class BTConnection 
{
    
    
    static final UUID OBEX_PUSH_PROFILE = new UUID(0x1105);
    static String obexURL = null;
    public static WatchService watchService;
    public static ScheduledExecutorService executorService;
    public static Operation putOperation = null;
    private static final int BUFFER_SIZE = 1024;
    private static Path path;
    private static String deviceName;
    private static String localDeviceName;
    private static boolean isBTavailable = false;
    private static Controller controller;
    private static WatchKey key;
    // kihelyeztem osztályváltozónak a key-t és csak egyszer lesz != null a checkForNewFiles()-nál

    public BTConnection(Controller controller) {
        this.controller = controller;
    }    
    

    public void watchFolder(Path path, String deviceName, int period){
        BTConnection.path = path;
        BTConnection.deviceName = deviceName;
        try 
        {
            if(path != null && period != 0)
            {
                watchService = FileSystems.getDefault().newWatchService();
                
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("path registered");
                // Create a scheduled executor that will run a task every 5 seconds
                executorService = Executors.newScheduledThreadPool(1);
                executorService.scheduleAtFixedRate(() -> checkForNewFiles(watchService), 5, period, TimeUnit.SECONDS);
            } 
               
        } catch (IOException   e) {
            e.printStackTrace();
        }
    }
    public void watchFolder(Path path, String deviceName) {
        BTConnection.path = path;
        BTConnection.deviceName = deviceName;
        try {
            if (path != null) {
                if(watchService == null){
                    watchService = FileSystems.getDefault().newWatchService();
                    path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                    System.out.println("Path registered");
                    checkForNewFiles(watchService);
                } else {
                    checkForNewFiles(watchService);
                }                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void checkForNewFiles(WatchService watchService) {
        try {
            System.out.println("checkForNewFiles");
            key = watchService.poll();
            if (key != null && path != null) 
            {
                for (WatchEvent<?> event : key.pollEvents()) 
                {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) 
                    {
                        Path newFile = (Path) event.context();
                        // Process the new file as needed
                        controller.setErrorLblText("New file found, sending...");
                                                
                        Path newFilePath = (Path) event.context();
                        
                        newFilePath = Paths.get(path+"\\"+newFilePath.getFileName());
                        
                        //Thread.sleep(5000);
                        RemoteDevice remoteDevice = discoverDevice(deviceName);
                        
                        if (remoteDevice != null) 
                        {
                            searchObexPushProfile(remoteDevice);
                            
                            if(obexURL != null){
                                connectAndSendFile( newFilePath.toFile(), obexURL);
                            } else {
                                controller.setErrorLblText("Bluetooth connection error.");
                                System.out.println("obexURL is null ");
                            }
                        } else {
                            controller.setErrorLblText("Device not found");
                            System.out.println("remoteDevice not found");
                        }
                    }
                }
                
            } else {
                controller.setErrorLblText("No changes in directory");
                System.out.println("key == null OR path == null" + key + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    
    
    private static RemoteDevice discoverDevice(String deviceName) throws BluetoothStateException {
        
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        DiscoveryAgent discoveryAgent = localDevice.getDiscoveryAgent();

        RemoteDevice[] remoteDevices = discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);        
        
        for (RemoteDevice remoteDevice : remoteDevices) 
        {            
            try {
                System.out.println("remoteDevice :" + remoteDevice.getFriendlyName(false));
                String friendlyName = remoteDevice.getFriendlyName(false);
                if (friendlyName.equals(deviceName)) {
                    System.out.println("Device found");
                    return remoteDevice;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    } 
    
    private static void searchObexPushProfile(RemoteDevice remoteDevice) {
        try {
            // Create the UUID array for the service search
            UUID[] uuidSet = {OBEX_PUSH_PROFILE};

            // Create the attribute set for the service search (empty for all attributes)
            int[] attrIDs = null;

            // Create the object to synchronize with the completion of the service search
            final Object serviceSearchCompletedEvent = new Object();

            // Create a discovery listener to handle the service search events
            DiscoveryListener listener = new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    // Ignore, not used in this example
                }

                @Override
                public void inquiryCompleted(int discType) {
                    // Ignore, not used in this example
                }

                @Override
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                    // Process the discovered services
                    for (ServiceRecord service : servRecord) {
                        String url = service.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                        if (url != null) {
                            obexURL = url;
                            System.out.println("Service found: " + url);                            
                        }
                    }
                }
                @Override
                public void serviceSearchCompleted(int transID, int respCode) {
                    // Notify that the service search is completed
                    synchronized (serviceSearchCompletedEvent) {
                        serviceSearchCompletedEvent.notifyAll();
                    }
                }
            };

            // Start the service search
            LocalDevice.getLocalDevice().getDiscoveryAgent()
                    .searchServices(attrIDs, uuidSet, remoteDevice, listener);

            // Wait for the service search to complete
            synchronized (serviceSearchCompletedEvent) {
                serviceSearchCompletedEvent.wait();
            }

        } catch (Exception e) {
            System.out.println("AT: searchObexPushProfile");
            e.printStackTrace();
        }
    }
    
    private static void connectAndSendFile(File file, String serviceURL) {
    try {
        // Open a connection and cast it to OBEXClientSession
        OBEXClientSessionImpl connection = (OBEXClientSessionImpl) Connector.open(serviceURL);
        System.out.println("Connection opened to: " + serviceURL);
        
        // Open output stream for sending file
        HeaderSet headerSet = connection.createHeaderSet();
        headerSet.setHeader(HeaderSet.NAME, file.getName());
        headerSet.setHeader(HeaderSet.LENGTH, file.length());

        headerSet = connection.connect(headerSet);
        if(headerSet.getResponseCode() == ResponseCodes.OBEX_HTTP_OK)
        {
            System.out.println("Device connected. Proceeding with file transfer.");
            // Create a new HeaderSet for put
            HeaderSet putHeaderSet = connection.createHeaderSet();
            putHeaderSet.setHeader(HeaderSet.NAME, file.getName());
            putHeaderSet.setHeader(HeaderSet.LENGTH, file.length());

            // Create PUT Operation
            putOperation = connection.put(putHeaderSet);

            // Open output stream for sending file
            OutputStream outputStream = putOperation.openOutputStream();
            try (FileInputStream fileInputStream = new FileInputStream(file)) 
            {
                System.out.println("FileTransfer starting");
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    
                    outputStream.write(buffer, 0, bytesRead);
                }
                // Close streams and connection
                fileInputStream.close();
                outputStream.close();
                key.reset();
                controller.setErrorLblText("File transfer completed.");
            }
            catch (IOException e) 
            {
                controller.setErrorLblText("Error during file transfer.");
                System.out.println("Error during file transfer: " + e.getMessage());
                System.out.println(file.getAbsolutePath() + " "+ file.length());
                e.printStackTrace();
            }
        } else {
            System.out.println("Wrong responseCode " + headerSet.getResponseCode());
        }
        putOperation.close();
        connection.close();

        System.out.println("Streams and connection closed.");
    } catch (IOException ex) {
        System.out.println("Error during file transfer: " + ex.getMessage());
        System.out.println(ex.getStackTrace());
    }
}
    
    public static boolean checkBluetoothAvailability() {
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();            
            System.out.println("Bluetooth is available on this device.");
            System.out.println("Address: " + localDevice.getBluetoothAddress());
            System.out.println("Name: " + localDevice.getFriendlyName());
            BTConnection.localDeviceName = localDevice.getFriendlyName();
            isBTavailable = true;
        } catch (Exception e) {
            System.out.println("Bluetooth is not available on this device.");
            e.printStackTrace();
            isBTavailable =  false;
        }
        return isBTavailable;
    }
    
    public boolean isBluetoothAvailable()
    {
        return isBTavailable;
    }

    public String getLocalDeviceName() {
        return localDeviceName;
    }
    
}
