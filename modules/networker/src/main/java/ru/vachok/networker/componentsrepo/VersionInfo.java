package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.io.*;
import java.security.SecureRandom;
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

    /**
     {@link ConstantsFor#thisPC()}
     */
    private final String thisPCName = ConstantsFor.thisPC();

    /**
     Версия
     */
    private String appVersion;

    /**
     * Билд
     */
    private String appBuild;

    /**
     * Время сборки
     */
    private String buildTime;

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
     * Загружает из {@link #PROPERTIES} информацию о версии и билде
     */
    private void getParams() {
        setAppBuild(PROPERTIES.getOrDefault("appBuild", "no database").toString());
        setBuildTime(PROPERTIES.getOrDefault("buildTime", System.currentTimeMillis()).toString());
        setAppVersion(PROPERTIES.getOrDefault("appVersion", "no database").toString());
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
     Usages: {@link AppComponents#versionInfo()} <br> Uses: {@link #setterVersionFromFiles(File)} , {@link #getParams()} , {@link #toString()} , {@link ConstantsFor#saveProps(Properties)}<br>
     */
    void setParams() {
        File file = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\build.gradle");
        if (file.exists()) {
            setterVersionFromFiles(file);
            ConstantsFor.saveProps(PROPERTIES);
        } else {
            file = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\build.gradle");
            if (file.exists()) {
                setterVersionFromFiles(file);
                ConstantsFor.saveProps(PROPERTIES);
            } else {
                getParams();
                String msg = toString();
                LOGGER.warn(msg);
            }
        }
        this.appBuild = thisPCName + "." + new SecureRandom().nextInt((int) ConstantsFor.MY_AGE);
        ConstantsFor.getProps().setProperty("appBuild", appBuild);
        if (thisPCName.equalsIgnoreCase("home") ||
            thisPCName.toLowerCase().contains(ConstantsFor.NO0027)) {
            this.buildTime = new Date(ConstantsFor.START_STAMP).toString();
            ConstantsFor.getProps().setProperty("buildTime", buildTime);
        }
        PROPERTIES.setProperty("appVersion", getAppVersion());
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
                if (x.contains("version = '0.")) {
                    setAppVersion(x.split("'")[1]);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        setBuildTime(System.currentTimeMillis() + "");
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
        sb.append(", PROPERTIES=").append(new TForms().fromArray(PROPERTIES));
        sb.append(", thisPCName='").append(thisPCName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
