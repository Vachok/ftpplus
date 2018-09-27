package ru.vachok.networker.componentsrepo;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;

import java.util.StringJoiner;


/**
 @since 24.09.2018 (9:44) */
@Component ("versioninfo")
public class VersionInfo {

    @Value ("application.name")
    private String appName = ConstantsFor.APP_NAME.replace("-", "");

    @Value("{build.version}")
    private String appVersion;

    private String timeStamp;

    @Value("${build.time}")
    private String buildTime;

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

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(final String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VersionInfo.class.getSimpleName() + "[", "]")
            .add("appName='" + appName + "'")
            .add("appVersion='" + appVersion + "'")
            .add("buildTime='" + buildTime + "'")
            .add("timeStamp='" + timeStamp + "'")
            .toString();
    }
}
