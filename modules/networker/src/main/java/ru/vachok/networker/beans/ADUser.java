package ru.vachok.networker.beans;


import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 @since 30.08.2018 (10:11) */
public class ADUser {

    private Logger logger = AppComponents.getLogger();

    private String defaultDomainName = "EATMEAT";

    private String userDomain;

    private String userName;

    private String userRealName;

    private String userSurname;

    private File userPhoto;

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

    public File getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(File userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUserDomain() {
        return userDomain;
    }

    public void setUserDomain(String userDomain) {
        this.userDomain = userDomain;
    }

    public void initUser() {
        File fileFromActDirectory = new File("adusers.txt");
        try(Scanner scanner = new Scanner(fileFromActDirectory)){
            throw new UnsupportedOperationException("Not ready 05.09.2018 (21:56)"); //todo 05.09.2018 (21:56)
        } catch(IOException e){
            logger.error(e.getMessage(), e);
        }
    }
}
