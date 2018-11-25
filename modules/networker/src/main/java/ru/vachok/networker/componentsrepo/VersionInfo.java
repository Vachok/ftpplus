package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.config.AppCtx;

import java.io.*;
import java.security.SecureRandom;
import java.util.Date;


/**
 @since 24.09.2018 (9:44) */
@Component("versioninfo")
public class VersionInfo {

    /*Fields*/
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfo.class.getSimpleName());

    private String appName = new AppCtx().getDisplayName().split("@")[0];

    private String appVersion;

    private String appBuild;

    private String buildTime;

    private final String thisPCName = ConstantsFor.thisPC();

    /*Instances*/
    public VersionInfo() {
        if(thisPCName.toLowerCase().contains("home") || thisPCName.toLowerCase().contains("no0")){
            setParams();
        } else getParams();
    }

    void setParams() {
        File file = new File("g:\\My_Proj\\FtpClientPlus\\modules\\networker\\build.gradle");
        if (file.exists()) {
            setterVersionFromFiles(file);
        } else {
            file = new File("c:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\build.gradle");
            setterVersionFromFiles(file);
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
    }

    public String getAppBuild() {
        return appBuild;
    }

    public void setAppBuild(String appBuild) {
        this.appBuild = appBuild;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppVersion() {
        return appVersion;
    }

    private void getParams() {
        setAppBuild(ConstantsFor.PROPS.getOrDefault("appBuild", "no database").toString());
        setBuildTime(ConstantsFor.PROPS.getOrDefault("buildTime", System.currentTimeMillis()).toString());
        setAppVersion(ConstantsFor.PROPS.getOrDefault("appVersion", "no database").toString());
    }

    private void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    private void setterVersionFromFiles(File file) {
        try (
            FileReader fileReader = new FileReader(file)) {
            BufferedReader reader = new BufferedReader(fileReader);
            reader.lines().forEach(x -> {
                if (x.contains("version = '0.")) {
                    setAppVersion(x.split("'")[1]);
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
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", pfLists timeout seconds='").append(ConstantsFor.DELAY).append('\'');
        sb.append(", netscan delay='").append(ConstantsFor.NETSCAN_DELAY).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
