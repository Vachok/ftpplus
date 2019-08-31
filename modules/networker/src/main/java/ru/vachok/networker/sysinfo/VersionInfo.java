// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.FileNames;
import ru.vachok.networker.componentsrepo.data.enums.OtherKnownDevices;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.prefs.Preferences;


/**
 @since 24.09.2018 (9:44) */
public class VersionInfo {
    
    
    /**
     {@link AppComponents#getProps()}
     */
    private final Properties PROPERTIES;
    
    /**
     Билд
     */
    private String appBuild = PREF_USER.get(PropertiesNames.PR_APP_BUILD, ALERT_DNE);
    
    /**
     Версия
     */
    private String appVersion = PREF_USER.get(PropertiesNames.PR_APP_VERSION, ALERT_DNE);
    
    /**
     Время сборки
     */
    private String buildTime = PREF_USER.get(PropertiesNames.PR_APP_BUILDTIME, ALERT_DNE);
    
    private String propertiesFrom = ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES;
    
    /**
     Ссылка на /doc/index.html
     */
    private static final String DOC_URL = "<a href=\"/doc/index.html\">DOC</a>";
    
    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.MessageToUser
        .getInstance(ru.vachok.networker.restapi.MessageToUser.LOCAL_CONSOLE, VersionInfo.class.getSimpleName());
    
    private static final String PR_APP_BUILD = "appBuild";
    
    private static final Preferences PREF_USER = AppComponents.getUserPref();
    
    private static final String ALERT_DNE = "Property does not exists";
    
    private static final String REPLACEPATTERN_VERSION = "version = '";
    
    public VersionInfo(Properties properties, String thisPC) {
        PROPERTIES = (Properties) properties.clone();
        if (thisPC.toLowerCase().contains("home") || thisPC.toLowerCase().contains(OtherKnownDevices.DO0213_KUDR)) {
            setParams();
        }
        else {
            getParams();
        }
    }
    
    public String getPropertiesFrom() {
        return propertiesFrom;
    }
    
    public void setPropertiesFrom(String propertiesFrom) {
        this.propertiesFrom = propertiesFrom;
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
        String timeStr = String.valueOf(ConstantsFor.START_STAMP);
        if (UsefulUtilities.thisPC().toLowerCase().contains("home") || UsefulUtilities.thisPC().toLowerCase().contains("do0")) {
            PROPERTIES.setProperty(PropertiesNames.PR_APP_BUILDTIME, timeStr);
            return timeStr;
        }
        else {
            return PROPERTIES.getProperty(PropertiesNames.PR_APP_BUILDTIME, "1");
        }
    }
    
    public String setParams() {
        String rootPathStr = Paths.get(".").toAbsolutePath().normalize().toString();
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(rootPathStr + ConstantsFor.FILESYSTEM_SEPARATOR + FileNames.FILENAME_BUILDGRADLE);
        if (file.exists()) {
            try {
                stringBuilder.append(setterVersionFromFiles(file)).append(" is SET");
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        }
        else {
            stringBuilder.append(getParams()).append(" is GET");
        }
        return stringBuilder.toString();
    }
    
    public String getParams() {
        try {
            DateFormat format = new SimpleDateFormat("yyw");
            this.appVersion = PROPERTIES.getProperty(PropertiesNames.PR_APP_VERSION, "8.0." + format.format(new Date()));
            this.buildTime = PROPERTIES.getProperty(PropertiesNames.PR_APP_BUILDTIME, String.valueOf(ConstantsFor.START_STAMP));
            format = new SimpleDateFormat("E");
            this.appBuild = PROPERTIES.getProperty(PropertiesNames.PR_APP_BUILD, format.format(new Date()));
    
            PREF_USER.put(PropertiesNames.PR_APP_VERSION, appVersion);
            PREF_USER.put(PropertiesNames.PR_APP_BUILDTIME, buildTime);
            PREF_USER.put(PropertiesNames.PR_APP_BUILD, appBuild);
            PREF_USER.sync();
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getParams", e));
        }
        return this.appVersion + " version from props, " + this.buildTime + " " + this.appBuild + " is GET";
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", propertiesFrom='").append(propertiesFrom).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Usages: {@link #setParams()} <br> Uses: - <br>
 
     @param file gradle.build
     */
    private String setterVersionFromFiles(File file) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("E", Locale.ENGLISH);
        for (String stringFromBuildGradle : FileSystemWorker.readFileToList(file.getAbsolutePath())) {
            if (stringFromBuildGradle.toLowerCase().contains(REPLACEPATTERN_VERSION)) {
                this.appVersion = stringFromBuildGradle.replace(REPLACEPATTERN_VERSION, "").replace("\'", "").trim();
            }
            this.buildTime = new Date().toString();
        }
        this.appBuild = UsefulUtilities.thisPC() + dateFormat.format(new Date());
        PROPERTIES.setProperty(PropertiesNames.PR_APP_BUILDTIME, this.buildTime);
        PROPERTIES.setProperty(PropertiesNames.PR_APP_BUILD, appBuild);
        PROPERTIES.setProperty(PropertiesNames.PR_APP_VERSION, this.appVersion);
        return this.appBuild + " build, " + this.buildTime + " time, " + this.appVersion + " version, props saved: " + false;
    }
}
