package com.pepper.autobtransfer.bluetooth;

import java.nio.file.Path;
import java.nio.file.Paths;
import com.intel.bluetooth.obex.OBEXClientSessionImpl;
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
    private static Operation putOperation = null;
    private static final int BUFFER_SIZE = 1024;
    private static Path path;
    private static String deviceName;

    public void watchFolder(Path path, String deviceName, int period){
        BTConnection.path = path;
        BTConnection.deviceName = deviceName;
        try {
            if(path != null && period != 0)
            {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                System.out.println("path registered");
                // Create a scheduled executor that will run a task every 5 seconds
                ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
                executorService.scheduleAtFixedRate(() -> checkForNewFiles(watchService), 0, period, TimeUnit.SECONDS);
              
            } 
               
        } catch (IOException   e) {
            e.printStackTrace();
        }
    }
    private static void checkForNewFiles(WatchService watchService) {
        try {
            System.out.println("checkForNewFiles");
            WatchKey key = watchService.poll();
            if (key != null && path != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        Path newFile = (Path) event.context();
                        // Process the new file as needed
                        System.out.println("New file created: " + newFile);
                        
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
                                System.out.println("obexURL is null ");
                            }
                        } else {
                            System.out.println("remoteDevice is null");
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void checkForNewFilesAndSendNow(WatchService watchService) {
        try {
            System.out.println("checkForNewFilesAndSendNow");
            while(true)
            {
                WatchKey key = watchService.poll();
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path newFile = (Path) event.context();
                            // Process the new file as needed
                            System.out.println("New file created: " + newFile);

                            Path newFilePath = (Path) event.context();

                            newFilePath = Paths.get(path+"\\"+newFilePath.getFileName());

                            RemoteDevice remoteDevice = discoverDevice(deviceName);

                            if (remoteDevice != null) 
                            {
                                searchObexPushProfile(remoteDevice);

                                if(obexURL != null){
                                    connectAndSendFile( newFilePath.toFile(), obexURL);
                                } else {
                                    System.out.println("obexURL is null ");
                                }
                            } else {
                                System.out.println("remoteDevice is null");
                            }
                        } else {
                            System.out.println(" 0 new file found");
                        }
                    }
                    key.reset();
                }
            
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
                System.out.println("File transfer completed.");
            } catch (IOException e) {
                System.out.println("Error during file transfer: " + e.getMessage());
                System.out.println(file.getAbsolutePath() + " "+ file.length());
                e.printStackTrace();
            }
        } else {
            System.out.println("Wrong responseCode " + headerSet.getResponseCode());
        }
        
        connection.close();

        System.out.println("Streams and connection closed.");
    } catch (IOException ex) {
        System.out.println("Error during file transfer: " + ex.getMessage());
        System.out.println(ex.getStackTrace());
    }
}
}
