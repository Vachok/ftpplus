package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 Действия, для выхода

 @since 21.12.2018 (12:15) */
public class ExitApp implements Runnable {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private String reasonExit;

    private String name;

    private Properties properties;

    private long toMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP);

    /*Instances*/
    public ExitApp(String name, String reasonExit) {
        this.reasonExit = reasonExit;
        this.name = name;
    }

    public ExitApp(String fromClass) {
        this.reasonExit = "No Reason. From " + IntoApplication.class.getSimpleName();
        this.name = fromClass;
    }

    @Override
    public void run() {
        this.properties = ConstantsFor.getProps();
        LOGGER.info("ExitApp.run");
        Thread.currentThread().setName("ExitApp.run");
        LOGGER.warn(reasonExit);
        try{
            copyAvail();
        }
        catch(IOException e){
            exitAppDO();
        }
    }

    private void copyAvail() throws IOException {
        String avaPathStr = Paths.get(".").toFile().getCanonicalPath();
        Path logPath = Paths.get(avaPathStr + "\\modules\\networker\\src\\main\\resources\\static\\texts\\available_last.txt");
        File avalInRoot = new File("available_last.txt");
        File avalInTexts = new File(logPath.toString());

        if(avalInRoot.exists() && avalInRoot.canRead()){
            String avalInTextsStr = avalInTexts.toString();
            avalInTextsStr = avalInTextsStr.replace("available", "available_last");
            Files.copy(avalInRoot.toPath(), Paths.get(avalInTextsStr));
            exitAppDO();
        }
        else{
            if(avalInTexts.exists() && avalInTexts.canWrite() && avalInTexts.canRead()){
                Files.deleteIfExists(avalInTexts.toPath());
                Files.copy(avalInTexts.toPath(), Paths.get(avalInTexts
                    .getAbsolutePath().replace("available", "available_last")));
                exitAppDO();
            }
            else{
                LOGGER.error("NO FILES AVAILABLE!");
                LOGGER.info(name);
                exitAppDO();
            }
        }
    }

    private void exitAppDO() {
        ConstantsFor.saveProps(properties);
        IntoApplication.getConfigurableApplicationContext().close();
        FileSystemWorker.delTemp();
        System.exit(Math.toIntExact(toMinutes));
    }
}
