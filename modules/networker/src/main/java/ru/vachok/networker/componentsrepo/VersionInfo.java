package ru.vachok.networker.componentsrepo;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;

/**
 @since 24.09.2018 (9:44) */
@Component("versioninfo")
public class VersionInfo {

    @Value(value = "{application.name}")
    private String appName = ConstantsFor.APP_NAME.replace("-", "");

    @Value(value = "{build.version}")
    private String appVersion;

    @Value("{timestamp}")
    private String timeStamp;

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

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
            "appName='" + appName + '\'' +
            ", appVersion='" + appVersion + '\'' +
            ", timeStamp='" + timeStamp + '\'' +
            '}';
    }
}
