package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Properties;


/**
 @since 24.09.2018 (9:44) */
@Component("versioninfo")
public class VersionInfo {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfo.class.getSimpleName());

    private String appName = ConstantsFor.APP_NAME;

    private String appVersion;

    private String appBuild;

    private String buildTime;

    private final String thisPCName = ConstantsFor.thisPC();

    /*Instances*/
    public VersionInfo() {
        Thread.currentThread().setName(getClass().getSimpleName());
        if(thisPCName.toLowerCase().contains("home") || thisPCName.toLowerCase().contains("no0")){
            setParams();
        } else getParams();
    }

    public String getAppBuild() {
        return appBuild;
    }

    /**
     Usages: {@link #getParams()} <br> Uses: - <br>

     @param appBuild build (Random num)
     */
    public void setAppBuild(String appBuild) {
        this.appBuild = appBuild;
    }

    public String getAppName() {
        return appName;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     Usages: {@link AppComponents#versionInfo()} <br> Uses: {@link #setterVersionFromFiles(File)} , {@link #getParams()} , {@link #toString()} , {@link ConstantsFor#saveProps(Properties)}<br>
     */
    void setParams() {
        File file = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\build.gradle");
        if (file.exists()) {
            setterVersionFromFiles(file);
        } else {
            file = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\build.gradle");
            if (file.exists()) setterVersionFromFiles(file);
            else {
                getParams();
                String msg = toString();
                LOGGER.warn(msg);
            }
        }
        this.appBuild = thisPCName + "." + new SecureRandom().nextInt(( int ) ConstantsFor.MY_AGE);
        ConstantsFor.PROPS.setProperty("appBuild", appBuild);
        if(thisPCName.equalsIgnoreCase("home") ||
            thisPCName.toLowerCase().contains("no0027")){
            this.buildTime = new Date(ConstantsFor.START_STAMP).toString();
            ConstantsFor.PROPS.setProperty("buildTime", buildTime);
        }
        ConstantsFor.PROPS.setProperty("appVersion", getAppVersion());
        String msg = this.toString();
        LOGGER.info(msg);
        ConstantsFor.saveProps(ConstantsFor.PROPS);
    }

    /**
     Usages: {@link #setParams()} <br> Uses: - <br>

     @param file gradle.build
     */
    private void setterVersionFromFiles(File file) {
        try (
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader)) {
            reader.lines().forEach(x -> {
                if (x.contains("version = '0.")) {
                    setAppVersion(x.split("'")[1]);
                }
            });
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        setBuildTime(System.currentTimeMillis() + "");
    }

    private void getParams() {
        setAppBuild(ConstantsFor.PROPS.getOrDefault("appBuild", "no database").toString());
        setBuildTime(ConstantsFor.PROPS.getOrDefault("buildTime", System.currentTimeMillis()).toString());
        setAppVersion(ConstantsFor.PROPS.getOrDefault("appVersion", "no database").toString());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", thisPCName='").append(thisPCName).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getAppVersion() {
        return appVersion;
    }

    private void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    /**
     Usages: {@link #getParams()} <br> Uses: - <br>

     @param buildTime build timestamp
     */
    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }
}
