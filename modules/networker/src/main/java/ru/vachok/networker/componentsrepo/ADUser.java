package ru.vachok.networker.componentsrepo;


import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;


/**
 @since 30.08.2018 (10:11) */
@Component("aduser")
public class ADUser {


    private static final Logger LOGGER = AppComponents.getLogger();

    private String defaultDomainName = "EATMEAT";

    private String userDomain;

    private String userName;

    private String userRealName;

    private String userSurname;

    @Override
    public String toString() {
        return "ADUser{" +
            "defaultDomainName='" + defaultDomainName + '\'' +
            ", userDomain='" + userDomain + '\'' +
            ", userName='" + userName + '\'' +
            ", userRealName='" + userRealName + '\'' +
            ", userSurname='" + userSurname + '\'' +
            ", userPhoto=" + userPhoto +
            '}';
    }

    private BufferedImage userPhoto;

    public String getDefaultDomainName() {
        return defaultDomainName;
    }

    public void setDefaultDomainName(String defaultDomainName) {
        this.defaultDomainName = defaultDomainName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(String userSurname) {
        this.userSurname = userSurname;
    }

    public BufferedImage getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(BufferedImage userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public void setUserDomain(String userDomain) {
        this.userDomain = userDomain;
    }
}
