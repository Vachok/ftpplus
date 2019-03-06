package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
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
    
    private boolean isBUGged;
    
    /**
     Версия
     */
    private String appVersion = null;
    
    /**
     Билд
     */
    private String appBuild = null;
    
    /**
     Время сборки
     */
    private String buildTime = null;
    
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
            AppComponents.threadConfig().executeAsThread(() -> setParams());
        }
        
        getParams();
        
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
     Usages: {@link #getParams()} <br> Uses: - <br>
     
     @param appBuild build (Random num)
     */
    private void setAppBuild(String appBuild) {
        this.appBuild = appBuild;
    }
    
    /**
     @return {@link #appVersion}
     */
    public String getAppVersion() {
        return appVersion;
    }
    
    /**
     @param appVersion {@link #setterVersionFromFiles(File)}
     */
    private void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
    
    /**
     @return {@link #buildTime}
     */
    public String getBuildTime() {
        return buildTime;
    }
    
    /**
     Usages: {@link #getParams()} <br> Uses: - <br>
     
     @param buildTime build timestamp
     */
    private void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
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
                getParams();
                String msg = toString();
                LOGGER.warn(msg);
            }
        }
        this.appBuild = thisPCNameStr + "." + LocalDate.now().getDayOfWeek().getValue();
        PROPERTIES.setProperty(PR_APP_BUILD, appBuild);
        if (thisPCNameStr.equalsIgnoreCase("home") ||
            thisPCNameStr.toLowerCase().contains(ConstantsFor.HOSTNAME_NO0027)) {
            this.buildTime = new Date(ConstantsFor.START_STAMP).toString();
            PROPERTIES.setProperty(PR_BUILD_TIME, buildTime);
        }
        try {
            PROPERTIES.setProperty(ConstantsFor.PR_APP_VERSION, getAppVersion());
            AppComponents.getOrSetProps(PROPERTIES);
        } catch (NullPointerException e) {
            setAppVersion("Unknown ver");
            FileSystemWorker.writeFile(getClass().getSimpleName() + ConstantsFor.FILEEXT_LOG, Collections.singletonList(new TForms().fromArray(e, false)));
        }
        String msg = this.toString();
        LOGGER.info(msg);
    }
    
    /**
     Usages: {@link #setParams()} <br> Uses: - <br>
     
     @param file gradle.build
     */
    private void setterVersionFromFiles(File file) {
        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(x -> {
                if (x.contains("version = '8.")) {
                    setAppVersion(x.split("'")[1]);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        setBuildTime(System.currentTimeMillis() + "");
    }
    
    /**
     Загружает из {@link #PROPERTIES} информацию о версии и билде
     */
    private void getParams() {
        setAppBuild(PROPERTIES.getOrDefault(PR_APP_BUILD, "no database").toString());
        setBuildTime(PROPERTIES.getOrDefault(PR_BUILD_TIME, System.currentTimeMillis()).toString());
        setAppVersion(PROPERTIES.getOrDefault(ConstantsFor.PR_APP_VERSION, "no database").toString());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", isBUGged=").append(isBUGged);
        sb.append('}');
        return sb.toString();
    }
}
