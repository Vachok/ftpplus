package ru.vachok.networker.ad;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


/**
 <b>Хранилище информации о пользователе AD</b>

 @since 30.08.2018 (10:11) */
@Component("aduser")
@Scope("prototype")
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

    private String sid;

    private String samAccountName;

    private String objectClass;

    private String objectGUID;

    private String name;

    private String enabled;

    private String givenName;

    /**
     Лист прав на common, где пользователь является владельцем.
     */
    private List<String> ownerRights = new ArrayList<>();

    public List<String> getOwnerRights() {
        return ownerRights;
    }

    public void setOwnerRights(List<String> ownerRights) {
        this.ownerRights = ownerRights;
    }

    private List<ADUser> adUsers = new ArrayList<>();

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

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getSamAccountName() {
        if (samAccountName == null) return "No Account";
        else return samAccountName;
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

    public List<ADUser> getAdUsers() {
        return adUsers;
    }

    public void setAdUsers(List<ADUser> adUsers) {
        this.adUsers = adUsers;
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

    public String toStringBR() {
        String brString = "'<br>";
        if (enabled != null && enabled.equalsIgnoreCase("true")) enabled = "<font=\"green\"><b>" + enabled + "</font></b>";
        else enabled = "<font=\"gray\">" + enabled + "</font>";
        return new String(new StringJoiner("   ", "<p><font color=\"yellow\">" + samAccountName + " = " + "</font><br>", hashCode() + " hashCode.   ")
            .add("defaultDomainName='" + defaultDomainName + brString)
            .add("distinguishedName='" + distinguishedName + brString)
            .add("enabled='" + enabled + brString)
            .add("givenName='" + givenName + brString)
            .add("name='" + name + brString)
            .add("objectClass='" + objectClass + brString)
            .add("objectGUID='" + objectGUID + brString)
            .add("samAccountName='" + samAccountName + brString)
            .add("sid='" + sid + brString)
            .add("surname='" + surname + brString)
            .add("userDomain='" + userDomain + brString)
            .add("userName='" + userName + brString)
            .add("userPrincipalName='" + userPrincipalName + brString)
            .add("userRealName='" + userRealName + brString)
            .add("userSurname='" + userSurname + brString)
            .toString().getBytes(), StandardCharsets.UTF_8);
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

    @Override
    public String toString() {
        return new StringJoiner("\n", ADUser.class.getSimpleName() + "\n", "\n")
            .add("defaultDomainName='" + defaultDomainName + "'\n")
            .add("distinguishedName='" + distinguishedName + "'\n")
            .add("enabled='" + enabled + "'\n")
            .add("givenName='" + givenName + "'\n")
            .add("name='" + name + "'\n")
            .add("objectClass='" + objectClass + "'\n")
            .add("objectGUID='" + objectGUID + "'\n")
            .add("samAccountName='" + samAccountName + "'\n")
            .add("sid='" + sid + "'\n")
            .add("surname='" + surname + "'\n")
            .add("userDomain='" + userDomain + "'\n")
            .add("userName='" + userName + "'\n")
            .add("userPrincipalName='" + userPrincipalName + "'\n")
            .add("userRealName='" + userRealName + "'\n")
            .add("userSurname='" + userSurname + "'\n")
            .toString();
    }
}
