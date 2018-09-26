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

    @Value ("build.version")
    private String appVersion;

    @Value ("build.timestamp")
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

    /*Get&Set*/
    public void setTimeStamp(final String timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return new StringJoiner("~~", VersionInfo.class.getSimpleName() + "\n", "\n")
            .add("appName='" + appName + "'")
            .add("appVersion='" + appVersion + "'")
            .add("timeStamp='" + timeStamp + "'")
            .toString();
    }
}
