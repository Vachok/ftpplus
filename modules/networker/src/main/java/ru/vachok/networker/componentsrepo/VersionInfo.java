// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;


/**

 @since 24.09.2018 (9:44) */
@Component(ConstantsFor.STR_VERSIONINFO)
@Scope(ConstantsFor.SINGLETON)
public class VersionInfo {

    /**
     Ссылка на /doc/index.html
     */
    private static final String DOC_URL = "<a href=\"/doc/index.html\">DOC</a>";

    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();

    private static final String PR_APP_BUILD = "appBuild";

    /**
     {@link ConstantsFor#thisPC()}
     */
    private final String thisPCNameStr = ConstantsFor.thisPC();

    private static final MessageToUser messageToUser = new MessageLocal(VersionInfo.class.getSimpleName());
    /**
     Версия
     */
    private String appVersion = "No version";
    /**
     Билд
     */
    private String appBuild = String.valueOf(this.hashCode());
    /**
     Время сборки
     */
    private String buildTime = getBuildTime();
    
    private String propertiesFrom = ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES;
    
    private static final String ALERT_DNE = "Property does not exists";
    
    private static final String REPLACEPAT_VERSION = "version = '";
    
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
        if (ConstantsFor.thisPC().toLowerCase().contains("home") || ConstantsFor.thisPC().toLowerCase().contains("do0")) {
            AppComponents.getProps().setProperty(ConstantsFor.PR_APP_BUILDTIME, timeStr);
            return timeStr;
        }
        else {
            return "1";
        }
    }
    
    /**
     *
     */
    public void setParams() {
        String rootPathStr = Paths.get(".").toAbsolutePath().normalize().toString();
        File file = new File(rootPathStr + ConstantsFor.FILENAME_BUILDGRADLE);

        if (file.exists()) {
            setterVersionFromFiles(file);
        } else {
            try {
                getParams();
            }
            catch (Exception e) {
                messageToUser.error(e.getMessage() + " " + getClass().getSimpleName() + ".setParams");
            }
        }
    }
    
    /**
     Usages: {@link #setParams()} <br> Uses: - <br>

     @param file gradle.build
     */
    private void setterVersionFromFiles( File file ) {
        for (String s : FileSystemWorker.readFileToList(file.getAbsolutePath())) {
            if (s.toLowerCase().contains(REPLACEPAT_VERSION)) {
                this.appVersion = s.replace(REPLACEPAT_VERSION, "").trim();
            }
            this.appBuild = String.valueOf(ConstantsFor.DELAY);
            this.buildTime = new Date().toString();
        }
        AppComponents.getProps().setProperty(ConstantsFor.PR_APP_BUILDTIME, this.buildTime);
        AppComponents.getProps().setProperty(ConstantsFor.PR_APP_BUILD, this.appBuild);
        AppComponents.getProps().setProperty(ConstantsFor.PR_APP_VERSION, this.appVersion);
    }
    
    private void getParams() {
        Properties properties = AppComponents.getProps();
        this.appBuild = properties.getProperty(ConstantsFor.PR_APP_BUILD, ALERT_DNE);
        this.appVersion = properties.getProperty(ConstantsFor.PR_APP_VERSION, ALERT_DNE);
        this.buildTime = properties.getProperty(ConstantsFor.PR_APP_BUILDTIME, ALERT_DNE);
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", propertiesFrom='").append(propertiesFrom).append('\'');
        sb.append(", thisPCNameStr='").append(thisPCNameStr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
