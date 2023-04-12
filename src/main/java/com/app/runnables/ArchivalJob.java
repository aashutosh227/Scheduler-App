package com.app.runnables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.System.*;

public class ArchivalJob implements Callable<Boolean> {

    private Logger LOGGER = Logger.getLogger(ArchivalJob.class.getName());
    private String srcPath;
    private String dstPath;
    private String action;

    public ArchivalJob(String s, String d, FileHandler handler) {
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);

        LOGGER.info("Logging Configuration done!!");

        LOGGER.info("Action: Backup");
        action = "B";
        boolean isErr = false;
        if(!Files.isDirectory(Paths.get(s))) {
            isErr = true;
            LOGGER.warning("Source Path Does Not Exists");
        }
        if(!Files.isDirectory(Paths.get(d))) {
            isErr = true;
            LOGGER.warning("Destination Path Does Not Exists");
        }
        if(isErr){
            System.exit(0);
        }
        srcPath = s;
        dstPath = d;
    }

    public ArchivalJob(String s, FileHandler handler) {
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);

        LOGGER.info("Logging Configuration done!!");

        LOGGER.info("Action: Delete");
        action = "D";
        boolean isErr = false;
        if(!Files.isDirectory(Paths.get(s))) {
            isErr = true;
            LOGGER.warning("Source Path Does Not Exists");
        }

        if(isErr){
            System.exit(0);
        }
        srcPath = s;
    }

    @Override
    public Boolean call() throws Exception {
        if(action.equals("B"))
            return performCopy(srcPath, dstPath);
        else if (action.equals("D"))
            return performDelete(srcPath);
        return false;
    }

    private Boolean performDelete(String sPath) {
        File folder  = new File(sPath);
        File[] files = folder.listFiles();
        LOGGER.info("Total No of Files to be Deleted from "+sPath+": "+Arrays.stream(files).count());
        LOGGER.info("Source Path: "+sPath);
        int c=0;

        for(File file: files){
            if((System.currentTimeMillis()-file.lastModified())/1000>24*60*60*2){
                try {
                    Files.delete(Paths.get(sPath+"/"+file.getName()));
                    LOGGER.info("Deleted: "+file.getAbsolutePath());
                    c++;
                } catch (IOException e) {
                    return false;
                }
            }
        }

        LOGGER.info("Total No of Files Deleted from "+sPath+": "+c);

        return true;
    }

    public boolean performCopy(String sPath, String dPath){
        File folder  = new File(sPath);
        File[] files = folder.listFiles();
        LOGGER.info("Total No of Files to be Archived in "+sPath+": "+Arrays.stream(files).count());
        LOGGER.info("Source Path: "+sPath);
        LOGGER.info("Destination Path: "+dPath);
        int c=0;
        int unModCt = 0;
        for(File file: files){
            String dFileName = dPath+"/"+file.getName();

            if(System.currentTimeMillis()-file.lastModified()>24*60*60*1000){
//            if(System.currentTimeMillis()-file.lastModified()>2*60*1000){
                if(Files.exists(Paths.get(dFileName))){
                    File dFile = new File(dFileName);
                    if(dFile.lastModified()==file.lastModified()){
                        unModCt++;
                        continue;
                    }
//                    Files.delete(Paths.get(dFileName));
                }
                try {
                    Files.copy(Paths.get(sPath+"/"+file.getName()), Paths.get(dPath+"/"+file.getName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage());
                    return false;
                }
                LOGGER.info("Moved file: "+file.getName());
                c++;
                if(file.isDirectory()){
                    //recursive call to handle the subdirectories
                    performCopy(sPath+"/"+file.getName(), dPath+"/"+file.getName());
                }
            }
        }
        LOGGER.info("Count of Unmodified Files: "+unModCt);
        LOGGER.info("Total No of Files Archived from "+dPath+": "+c);

        return true;
    }
}