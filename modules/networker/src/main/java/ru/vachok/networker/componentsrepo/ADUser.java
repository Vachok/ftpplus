package ru.vachok.networker.componentsrepo;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


/**
 <h>Хранилище информации о пользователе AD</h1>

 @since 30.08.2018 (10:11) */
@Component("aduser")
@Scope("prototype")
public class ADUser {

    private static ADUser adUser = new ADUser();

    private ADUser() {

    }

    public static ADUser getAdUser() {
        return adUser;
    }

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

    @Override
    public int hashCode() {
        int result = defaultDomainName != null ? defaultDomainName.hashCode() : 0;
        result = 31 * result + (userDomain != null ? userDomain.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + (userRealName != null ? userRealName.hashCode() : 0);
        result = 31 * result + (userSurname != null ? userSurname.hashCode() : 0);
        result = 31 * result + (distinguishedName != null ? distinguishedName.hashCode() : 0);
        result = 31 * result + (userPrincipalName != null ? userPrincipalName.hashCode() : 0);
        result = 31 * result + (surname != null ? surname.hashCode() : 0);
        result = 31 * result + (SID != null ? SID.hashCode() : 0);
        result = 31 * result + (samAccountName != null ? samAccountName.hashCode() : 0);
        result = 31 * result + (objectClass != null ? objectClass.hashCode() : 0);
        result = 31 * result + (objectGUID != null ? objectGUID.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);
        result = 31 * result + (givenName != null ? givenName.hashCode() : 0);
        result = 31 * result + (adUsers != null ? adUsers.hashCode() : 0);
        result = 31 * result + (userPhoto != null ? userPhoto.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ADUser adUser = (ADUser) o;

        if (defaultDomainName != null ? !defaultDomainName.equals(adUser.defaultDomainName) : adUser.defaultDomainName != null) return false;
        if (userDomain != null ? !userDomain.equals(adUser.userDomain) : adUser.userDomain != null) return false;
        if (userName != null ? !userName.equals(adUser.userName) : adUser.userName != null) return false;
        if (userRealName != null ? !userRealName.equals(adUser.userRealName) : adUser.userRealName != null) return false;
        if (userSurname != null ? !userSurname.equals(adUser.userSurname) : adUser.userSurname != null) return false;
        if (distinguishedName != null ? !distinguishedName.equals(adUser.distinguishedName) : adUser.distinguishedName != null) return false;
        if (userPrincipalName != null ? !userPrincipalName.equals(adUser.userPrincipalName) : adUser.userPrincipalName != null) return false;
        if (surname != null ? !surname.equals(adUser.surname) : adUser.surname != null) return false;
        if (SID != null ? !SID.equals(adUser.SID) : adUser.SID != null) return false;
        if (samAccountName != null ? !samAccountName.equals(adUser.samAccountName) : adUser.samAccountName != null) return false;
        if (objectClass != null ? !objectClass.equals(adUser.objectClass) : adUser.objectClass != null) return false;
        if (objectGUID != null ? !objectGUID.equals(adUser.objectGUID) : adUser.objectGUID != null) return false;
        if (name != null ? !name.equals(adUser.name) : adUser.name != null) return false;
        if (enabled != null ? !enabled.equals(adUser.enabled) : adUser.enabled != null) return false;
        if (givenName != null ? !givenName.equals(adUser.givenName) : adUser.givenName != null) return false;
        if (adUsers != null ? !adUsers.equals(adUser.adUsers) : adUser.adUsers != null) return false;
        return userPhoto != null ? userPhoto.equals(adUser.userPhoto) : adUser.userPhoto == null;
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
            .add("userName='" + userName + "'\n")
            .toString();
    }
}
