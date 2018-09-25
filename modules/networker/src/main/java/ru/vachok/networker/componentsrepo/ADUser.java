package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 <h>Хранилище информации о пользователе AD</h1>

 @since 30.08.2018 (10:11) */
@Component("aduser")
public class ADUser {

    /**
     <b>eatmeat.ru</b>
     */
    private String defaultDomainName = "EATMEAT.RU";

    /**
     <b>SamAccountName</b>
     */
    private String userDomain;

    private String userName;

    private String userRealName;

    private String userSurname;

    private String distinguishedName;

    private String userPrincipalName;

    private String surname;

    private String SID;

    private String samAccountName;

    private String objectClass;

    private String objectGUID;

    private String name;

    private String enabled;

    private String givenName;

    private Map<Integer, ADUser> adUsers = new ConcurrentHashMap<>();

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public String getSamAccountName() {
        return samAccountName;
    }

    public void setSamAccountName(String samAccountName) {
        this.samAccountName = samAccountName;
    }

    public String getObjectGUID() {
        return objectGUID;
    }

    public void setObjectGUID(String objectGUID) {
        this.objectGUID = objectGUID;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public Map<Integer, ADUser> getAdUsers() {
        return adUsers;
    }

    public void setAdUsers(Map<Integer, ADUser> adUsers) {
        this.adUsers = adUsers;
    }

    @Override
    public String toString() {
        return "ADUser{" +
            "defaultDomainName='" + defaultDomainName + '\'' +
            ", userDomain='" + userDomain + '\'' +
            ", userName='" + userName + '\'' +
            ", userRealName='" + userRealName + '\'' +
            ", userSurname='" + userSurname + '\'' +
            ", adUsers=" + adUsers.size() +
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
