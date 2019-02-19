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
     {@link ConstantsFor#getProps()}
     */
    private static final Properties PROPERTIES = ConstantsFor.getProps();

    private static final String PROP_BUILD_TIME = "buildTime";

    private static final String PROP_APP_BUILD = "appBuild";

    /**
     {@link ConstantsFor#thisPC()}
     */
    private final String thisPCName = ConstantsFor.thisPC();

    /**
     Версия
     */
    private String appVersion = null;

    /**
     * Билд
     */
    private String appBuild = null;

    /**
     * Время сборки
     */
    private String buildTime = null;

    /**
     Usages: {@link #getParams()} <br> Uses: - <br>

     @param appBuild build (Random num)
     */
    private void setAppBuild(String appBuild) {
        this.appBuild = appBuild;
    }

    /**
     Конструктор по-умолчанию.
     <p>
     * Если имя ПК содержит "home" или "no0" {@link #setParams()} , иначе {@link #getParams()}
     */
    public VersionInfo() {
        Thread.currentThread().setName(getClass().getSimpleName());
        if (thisPCName.toLowerCase().contains("home") || thisPCName.toLowerCase().contains("no0")) {
            setParams();
        } else getParams();
    }

    /**
     Usages: {@link AppComponents#versionInfo()} <br> Uses: {@link #setterVersionFromFiles(File)} , {@link #getParams()} , {@link #toString()} , {@link ConstantsFor#saveProps(Properties)}<br>
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
        this.appBuild = thisPCName + "." + LocalDate.now().getDayOfWeek().getValue();
        PROPERTIES.setProperty(PROP_APP_BUILD, appBuild);
        if (thisPCName.equalsIgnoreCase("home") ||
            thisPCName.toLowerCase().contains(ConstantsFor.NO0027)) {
            this.buildTime = new Date(ConstantsFor.START_STAMP).toString();
            ConstantsFor.getProps().setProperty(PROP_BUILD_TIME, buildTime);
        }
        try{
            PROPERTIES.setProperty(ConstantsFor.PR_APP_VERSION, getAppVersion());
        }
        catch(NullPointerException e){
            setAppVersion("Unknown ver");
            FileSystemWorker.recFile(getClass().getSimpleName() + ConstantsFor.LOG, Collections.singletonList(new TForms().fromArray(e, false)));
        }
        String msg = this.toString();
        LOGGER.info(msg);
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
     Usages: {@link #setParams()} <br> Uses: - <br>

     @param file gradle.build
     */
    private void setterVersionFromFiles(File file) {
        try (InputStream inputStream = new FileInputStream(file);
             InputStreamReader reader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            bufferedReader.lines().forEach(x -> {
                if(x.contains("version = '8.")){
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
        setAppBuild(PROPERTIES.getOrDefault(PROP_APP_BUILD, "no database").toString());
        setBuildTime(PROPERTIES.getOrDefault(PROP_BUILD_TIME, System.currentTimeMillis()).toString());
        setAppVersion(PROPERTIES.getOrDefault(ConstantsFor.PR_APP_VERSION, "no database").toString());
    }

    /**
     Usages: {@link #getParams()} <br> Uses: - <br>

     @param buildTime build timestamp
     */
    private void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        String appName = ConstantsFor.APP_NAME;
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", DOC_URL='").append(DOC_URL).append('\'');
        sb.append(", thisPCName='").append(thisPCName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
