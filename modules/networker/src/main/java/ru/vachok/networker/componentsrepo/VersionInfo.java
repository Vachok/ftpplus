package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;

import java.io.*;
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

    private static final Properties PROPERTIES = ConstantsFor.getPROPS();

    private final String thisPCName = ConstantsFor.thisPC();
    /*Get&Set*/

    /**
     Usages: {@link #getParams()} <br> Uses: - <br>

     @param appBuild build (Random num)
     */
    private void setAppBuild(String appBuild) {
        this.appBuild = appBuild;
    }
    public VersionInfo() {
        Thread.currentThread().setName(getClass().getSimpleName());
        if(thisPCName.toLowerCase().contains("home") || thisPCName.toLowerCase().contains("no0")){
            setParams();
        } else getParams();
    }

    public String getAppBuild() {
        return appBuild;
    }

    public String getAppVersion() {
        return appVersion;
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
            ConstantsFor.saveProps(PROPERTIES);
        } else {
            file = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\build.gradle");
            if(file.exists()){
                setterVersionFromFiles(file);
                ConstantsFor.saveProps(PROPERTIES);
            }
            else {
                getParams();
                String msg = toString();
                LOGGER.warn(msg);
            }
        }
        this.appBuild = thisPCName + "." + new SecureRandom().nextInt(( int ) ConstantsFor.MY_AGE);
        ConstantsFor.getPROPS().setProperty("appBuild", appBuild);
        if(thisPCName.equalsIgnoreCase("home") ||
            thisPCName.toLowerCase().contains("no0027")){
            this.buildTime = new Date(ConstantsFor.START_STAMP).toString();
            ConstantsFor.getPROPS().setProperty("buildTime", buildTime);
        }
        PROPERTIES.setProperty("appVersion", getAppVersion());
        String msg = this.toString();
        LOGGER.info(msg);
    }

    private void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    private void getParams() {
        setAppBuild(PROPERTIES.getOrDefault("appBuild", "no database").toString());
        setBuildTime(PROPERTIES.getOrDefault("buildTime", System.currentTimeMillis()).toString());
        setAppVersion(PROPERTIES.getOrDefault("appVersion", "no database").toString());
    }

    /*Instances*/

    /**
     Usages: {@link #setParams()} <br> Uses: - <br>

     @param file gradle.build
     */
    private void setterVersionFromFiles(File file) {
        try(InputStream inputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader)){
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
        sb.append(", appName='").append(appName).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", thisPCName='").append(thisPCName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
