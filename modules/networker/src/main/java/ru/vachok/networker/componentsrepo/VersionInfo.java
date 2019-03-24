package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.TimeChecker;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Properties;


/**
 @since 24.09.2018 (9:44) */
@Component("versioninfo")
public class VersionInfo {
    
    
    /**
     {@link LoggerFactory#getLogger(String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfo.class.getSimpleName());
    
    /**
     Ссылка на /doc/index.html
     */
    private static final String DOC_URL = "<a href=\"/doc/index.html\">DOC</a>";
    
    /**
     {@link AppComponents#getOrSetProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getOrSetProps();
    
    private static final String PR_BUILD_TIME = "buildTime";
    
    private static final String PR_APP_BUILD = "appBuild";
    
    /**
     {@link ConstantsFor#thisPC()}
     */
    private final String thisPCNameStr = ConstantsFor.thisPC();
    
    /**
     Версия
     */
    private final String appVersion;
    
    /**
     Билд
     */
    private final String appBuild;
    
    /**
     Время сборки
     */
    private final String buildTime;
    
    private boolean isBUGged;
    
    private long pingTVStartStamp = ConstantsFor.START_STAMP;
    
    /**
     Конструктор по-умолчанию.
     <p>
     Если имя ПК содержит "home" или "no0" {@link #setParams()} , иначе {@link #getParams()}
     */
    public VersionInfo() {
        if (new File("bugged").exists()) {
            this.isBUGged = true;
        }
        if (thisPCNameStr.toLowerCase().contains("home") || thisPCNameStr.toLowerCase().contains("no0")) {
            AppComponents.threadConfig().executeAsThread(this::setParams);
        }
        appBuild = PROPERTIES.getProperty(PR_APP_BUILD);
        appVersion = PROPERTIES.getProperty(ConstantsFor.PR_APP_VERSION);
        buildTime = PROPERTIES.getProperty(PR_BUILD_TIME, String.valueOf(new TimeChecker().call().getReturnTime()));
    }
    
    public long getPingTVStartStamp() {
        return pingTVStartStamp;
    }
    
    public void setPingTVStartStamp(long pingTVStartStamp) {
        this.pingTVStartStamp = pingTVStartStamp;
    }
    
    public boolean isBUGged() {
        return isBUGged;
    }
    
    public void setBUGged(boolean BUGged) {
        isBUGged = BUGged;
    }
    
    /**
     @return {@link #appBuild}
     */
    public String getAppBuild() {
        return appBuild;
    }
    
    /**
     @return {@link #appVersion}
     */
    public String getAppVersion() {
        return appVersion;
    }
    
    /**
     @return {@link #buildTime}
     */
    public String getBuildTime() {
        return buildTime;
    }
    
    /**
     *
     */
    void setParams() {
        File file = new File("G:\\My_Proj\\FtpClientPlus\\modules\\networker\\build.gradle");
        if (file.exists()) {
            setterVersionFromFiles(file);
        } else {
            file = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\build.gradle");
            if (file.exists()) {
                setterVersionFromFiles(file);
            } else {
                String msg = toString();
                LOGGER.warn(msg);
            }
        }
        PROPERTIES.setProperty(PR_APP_BUILD, thisPCNameStr + "." + LocalDate.now().getDayOfWeek().getValue());
        if (thisPCNameStr.equalsIgnoreCase("home") ||
            thisPCNameStr.toLowerCase().contains(ConstantsFor.HOSTNAME_NO0027)) {
            PROPERTIES.setProperty(PR_BUILD_TIME, String.valueOf(System.currentTimeMillis()));
        }
        try {
            PROPERTIES.setProperty(ConstantsFor.PR_APP_VERSION, getAppVersion());
        } catch (NullPointerException e) {
            FileSystemWorker.writeFile(getClass().getSimpleName() + ConstantsFor.FILEEXT_LOG, Collections.singletonList(new TForms().fromArray(e, false)));
        }
        AppComponents.getOrSetProps(PROPERTIES);
    }
    
    /**
     Usages: {@link #setParams()} <br> Uses: - <br>
     
     @param file gradle.build
     */
    private void setterVersionFromFiles(File file) {
        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(x->{
                if (x.contains("version = '8.")) {
                    PROPERTIES.setProperty(ConstantsFor.PR_APP_VERSION, x.split("'")[1]);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(LocalDateTime.ofEpochSecond(Long.parseLong(buildTime) / 1000, 0, ZoneOffset.ofHours(3))).append('\'');
        sb.append(", isBUGged=").append(isBUGged);
        sb.append('}');
        return sb.toString();
    }
}
