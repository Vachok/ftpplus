package ru.vachok.money.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.money.components.AppVersion;

import java.util.Scanner;


/**
 @since 27.09.2018 (1:05) */
@Service ("appverser")
public class AppVerSrv {

    /*Fields*/
    private static final String SOURCE_CLASS = AppVerSrv.class.getSimpleName();

    private static final Logger LOGGER = LoggerFactory.getLogger(SOURCE_CLASS);

    private AppVersion appVersion;

    public int getVerID() {
        return AppVersion.GENERIC_ID;
    }

    /*Instances*/
    @Autowired
    public AppVerSrv(AppVersion appVersion) {
        this.appVersion = appVersion;
    }

    private double readGradleCnf() {
        double theVersion = 0.001;
        try(Scanner scanner = new Scanner("G:\\My_Proj\\FtpClientPlus\\modules\\money\\build.gradle")){
            while(scanner.hasNext()){
                String version = scanner.findInLine("version");
                String msg = SOURCE_CLASS + " " + version;
                LOGGER.info(msg);
            }
        }
        return theVersion;
    }
}