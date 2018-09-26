package ru.vachok.money.components;


import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.StringJoiner;


/**
 @since 27.09.2018 (1:02) */
@Component ("appversion")
public class AppVersion implements Serializable {

    public static final long serialVersionUID = 1984L;

    public static final int GENERIC_ID = new SecureRandom().nextInt(1984);

    private String appMajorVersion;

    private String appMinorVersion;

    private String appDisplayVwrsion;

    private String appBuild;

    /*Instances*/
    public AppVersion() {
        String msg = GENERIC_ID + " final";
        LoggerFactory.getLogger(AppVersion.class).info(msg);
    }

    @Override
    public int hashCode() {
        int result = getAppMajorVersion()!=null? getAppMajorVersion().hashCode(): 0;
        result = 31 * result + (getAppMinorVersion()!=null? getAppMinorVersion().hashCode(): 0);
        result = 31 * result + (getAppDisplayVwrsion()!=null? getAppDisplayVwrsion().hashCode(): 0);
        result = 31 * result + (getAppBuild()!=null? getAppBuild().hashCode(): 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null || getClass()!=o.getClass()){
            return false;
        }

        AppVersion that = ( AppVersion ) o;

        if(getAppMajorVersion()!=null? !getAppMajorVersion().equals(that.getAppMajorVersion()): that.getAppMajorVersion()!=null){
            return false;
        }
        if(getAppMinorVersion()!=null? !getAppMinorVersion().equals(that.getAppMinorVersion()): that.getAppMinorVersion()!=null){
            return false;
        }
        if(getAppDisplayVwrsion()!=null? !getAppDisplayVwrsion().equals(that.getAppDisplayVwrsion()): that.getAppDisplayVwrsion()!=null){
            return false;
        }
        return getAppBuild()!=null? getAppBuild().equals(that.getAppBuild()): that.getAppBuild()==null;
    }

    public String getAppMajorVersion() {
        return appMajorVersion;
    }

    public void setAppMajorVersion(String appMajorVersion) {
        this.appMajorVersion = appMajorVersion;
    }

    public String getAppMinorVersion() {
        return appMinorVersion;
    }

    public void setAppMinorVersion(String appMinorVersion) {
        this.appMinorVersion = appMinorVersion;
    }

    public String getAppDisplayVwrsion() {
        return appDisplayVwrsion;
    }

    public void setAppDisplayVwrsion(String appDisplayVwrsion) {
        this.appDisplayVwrsion = appDisplayVwrsion;
    }

    public String getAppBuild() {
        return appBuild;
    }

    public void setAppBuild(String appBuild) {
        this.appBuild = appBuild;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AppVersion.class.getSimpleName() + "<br>", "<br>")
            .add("appBuild='" + appBuild + "'")
            .add("appDisplayVwrsion='" + appDisplayVwrsion + "'")
            .add("appMajorVersion='" + appMajorVersion + "'")
            .add("appMinorVersion='" + appMinorVersion + "'")
            .toString();
    }
}