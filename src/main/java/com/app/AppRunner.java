package com.app;

import com.app.runnables.ArchivalJob;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class AppRunner {
    private static FileHandler handler;
    private static Logger LOGGER = Logger.getLogger(AppRunner.class.getName());
    private static ArchivalJob job;

    private static void initializeLogger(String action){
        String dtSuffix = "";
        SimpleDateFormat dtFormat = new SimpleDateFormat("ddMMyyyy");
        Date d = new Date();
        dtSuffix = dtFormat.format(d);
        System.out.println(dtSuffix);
        System.out.println(System.getProperty("user.dir"));
        try {
            handler = new FileHandler(System.getProperty("user.dir")
            +"/scheduler_"+action+"_"+dtSuffix+".txt");
            handler.setFormatter(new SimpleFormatter());

            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(handler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {

        ScheduledExecutorService service  = Executors.newScheduledThreadPool(4);

        if(args.length==1) {
            initializeLogger("D");
            delete(args[0]);
        }
        else if(args.length==2) {
            initializeLogger("B");
            backup(args[0], args[1]);
        }

        LOGGER.info("Scheduling the Job!!");
        ScheduledFuture<Boolean> rs = service.schedule(job,2, TimeUnit.SECONDS);

        try {
            LOGGER.info("Scheduler result: "+rs.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    private static void backup(String srcPath, String dstPath) {

        LOGGER.info("Program Initialized!!!");
        LOGGER.info("Source Directory: "+srcPath);
        LOGGER.info("Destination Directory: "+dstPath);

        job = new ArchivalJob(srcPath, dstPath, handler);

    }

    private static void delete(String srcPath) {
        LOGGER.info("Program Initialized!!!");
        LOGGER.info("Source Directory: "+srcPath);

        job = new ArchivalJob(srcPath, handler);

    }
}
