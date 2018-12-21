package ru.vachok.networker.config;


import org.slf4j.Logger;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 Действия, для выхода

 @since 21.12.2018 (12:15) */
public class ExitApp extends Thread {

    private static final Logger LOGGER = AppComponents.getLogger();

    private String reasonExit;

    public ExitApp(String name, String reasonExit) {
        super(name);
        this.reasonExit = reasonExit;
    }

    public ExitApp() {
        this.reasonExit = "No Reason. From " + IntoApplication.class.getSimpleName();
    }

    @Override
    public void run() {
        LOGGER.info("ExitApp.run");
        Thread.currentThread().setName("ExitApp.run");

        try {
            copyAval();
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
        LOGGER.warn(reasonExit);
        if (!IntoApplication.getConfigurableApplicationContext().isActive())
            System.exit((int) TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_STAMP));
    }

    private void copyAval() throws IOException {
        String avaPathStr = Paths.get(".").toFile().getCanonicalPath();
        Path logPath = Paths.get(avaPathStr + "\\modules\\networker\\src\\main\\resources\\static\\texts\\available.txt");
        File avalInRoot = new File("available.txt");
        File avalInTexts = new File(logPath.toString());
        if (avalInRoot.exists() && avalInRoot.canRead()) {
            String avalInTextsStr = avalInTexts.toString();
            avalInTextsStr = avalInTextsStr.replace("available", "available_last");
            Files.copy(avalInRoot.toPath(), Paths.get(avalInTextsStr));
        } else if (avalInTexts.exists() && avalInTexts.canRead()) {
            Files.copy(avalInTexts.toPath(), Paths.get(avalInTexts
                .getAbsolutePath().replace("available", "available_last")));
        } else {
            LOGGER.error("NO FILES AVAILABLE!");
        }
    }
}
