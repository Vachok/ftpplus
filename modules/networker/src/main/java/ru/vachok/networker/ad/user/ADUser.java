package ru.vachok.networker.ad.user;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


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
    private String userDomain = "";

    private String userName = "";

    private String userRealName = "";

    private String userSurname = "";

    private String distinguishedName = "";

    private String userPrincipalName = "";

    private String surname = "";

    private String sid = "";

    private String samAccountName = "";

    private String objectClass = "";

    private String objectGUID = "";

    private String name = "";

    private String enabled = "";

    private String givenName = "";

    /**
     Ввод пользователя.
     <p>
     Из user.html .

     @see UserWebCTRL
     */
    private String inputName = "";

    /**
     Лист прав на common, где пользователь является владельцем.
     */
    private List<String> ownerRights = new ArrayList<>();

    private BufferedImage userPhoto;

    /**
     @return {@link #inputName}
     */
    public String getInputName() {
        return inputName;
    }

    /**
     @param inputName {@link #inputName}
     */
    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public List<String> getOwnerRights() {
        return ownerRights;
    }

    public void setOwnerRights(List<String> ownerRights) {
        this.ownerRights = ownerRights;
    }

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

    public String toStringBR() {
        final StringBuilder sb = new StringBuilder("ADUser{");
        sb.append("defaultDomainName='").append(defaultDomainName).append("<br>");
        sb.append(", userDomain='").append(userDomain).append("<br>");
        sb.append(", userName='").append(userName).append("<br>");
        sb.append(", userRealName='").append(userRealName).append("<br>");
        sb.append(", userSurname='").append(userSurname).append("<br>");
        sb.append(", distinguishedName='").append(distinguishedName).append("<br>");
        sb.append(", userPrincipalName='").append(userPrincipalName).append("<br>");
        sb.append(", surname='").append(surname).append("<br>");
        sb.append(", sid='").append(sid).append("<br>");
        sb.append(", samAccountName='").append(samAccountName).append("<br>");
        sb.append(", objectClass='").append(objectClass).append("<br>");
        sb.append(", objectGUID='").append(objectGUID).append("<br>");
        sb.append(", name='").append(name).append("<br>");
        sb.append(", enabled='").append(enabled).append("<br>");
        sb.append(", givenName='").append(givenName).append("<br>");
        sb.append(", inputName='").append(inputName).append("<br>");
        sb.append(", ownerRights=").append(ownerRights.size());
        sb.append('}');
        return sb.toString();
    }

    @SuppressWarnings({"MethodWithMoreThanThreeNegations", "OverlyComplexMethod"})
    @Override
    public int hashCode() {
        int result = getDefaultDomainName().hashCode();
        result = 31 * result + (getUserDomain() != null ? getUserDomain().hashCode() : 0);
        result = 31 * result + (getUserName() != null ? getUserName().hashCode() : 0);
        result = 31 * result + (getUserRealName() != null ? getUserRealName().hashCode() : 0);
        result = 31 * result + (getUserSurname() != null ? getUserSurname().hashCode() : 0);
        result = 31 * result + (getDistinguishedName() != null ? getDistinguishedName().hashCode() : 0);
        result = 31 * result + (getUserPrincipalName() != null ? getUserPrincipalName().hashCode() : 0);
        result = 31 * result + (getSurname() != null ? getSurname().hashCode() : 0);
        result = 31 * result + (getSid() != null ? getSid().hashCode() : 0);
        result = 31 * result + (getSamAccountName() != null ? getSamAccountName().hashCode() : 0);
        result = 31 * result + (getObjectClass() != null ? getObjectClass().hashCode() : 0);
        result = 31 * result + (getObjectGUID() != null ? getObjectGUID().hashCode() : 0);
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getEnabled() != null ? getEnabled().hashCode() : 0);
        result = 31 * result + (getGivenName() != null ? getGivenName().hashCode() : 0);
        result = 31 * result + (getInputName() != null ? getInputName().hashCode() : 0);
        result = 31 * result + getOwnerRights().hashCode();
        result = 31 * result + (getUserPhoto() != null ? getUserPhoto().hashCode() : 0);
        return result;
    }


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

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ADUser)) return false;

        ADUser adUser = (ADUser) o;

        if (!getDefaultDomainName().equals(adUser.getDefaultDomainName())) return false;
        if (getUserDomain() != null ? !getUserDomain().equals(adUser.getUserDomain()) : adUser.getUserDomain() != null) return false;
        if (getUserName() != null ? !getUserName().equals(adUser.getUserName()) : adUser.getUserName() != null) return false;
        if (getUserRealName() != null ? !getUserRealName().equals(adUser.getUserRealName()) : adUser.getUserRealName() != null) return false;
        if (getUserSurname() != null ? !getUserSurname().equals(adUser.getUserSurname()) : adUser.getUserSurname() != null) return false;
        if (getDistinguishedName() != null ? !getDistinguishedName().equals(adUser.getDistinguishedName()) : adUser.getDistinguishedName() != null) return false;
        if (getUserPrincipalName() != null ? !getUserPrincipalName().equals(adUser.getUserPrincipalName()) : adUser.getUserPrincipalName() != null) return false;
        if (getSurname() != null ? !getSurname().equals(adUser.getSurname()) : adUser.getSurname() != null) return false;
        if (getSid() != null ? !getSid().equals(adUser.getSid()) : adUser.getSid() != null) return false;
        if (getSamAccountName() != null ? !getSamAccountName().equals(adUser.getSamAccountName()) : adUser.getSamAccountName() != null) return false;
        if (getObjectClass() != null ? !getObjectClass().equals(adUser.getObjectClass()) : adUser.getObjectClass() != null) return false;
        if (getObjectGUID() != null ? !getObjectGUID().equals(adUser.getObjectGUID()) : adUser.getObjectGUID() != null) return false;
        if (getName() != null ? !getName().equals(adUser.getName()) : adUser.getName() != null) return false;
        if (getEnabled() != null ? !getEnabled().equals(adUser.getEnabled()) : adUser.getEnabled() != null) return false;
        if (getGivenName() != null ? !getGivenName().equals(adUser.getGivenName()) : adUser.getGivenName() != null) return false;
        if (getInputName() != null ? !getInputName().equals(adUser.getInputName()) : adUser.getInputName() != null) return false;
        if (!getOwnerRights().equals(adUser.getOwnerRights())) return false;
        return getUserPhoto() != null ? getUserPhoto().equals(adUser.getUserPhoto()) : adUser.getUserPhoto() == null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADUser{");
        sb.append("defaultDomainName='").append(defaultDomainName).append('\'');
        sb.append(", userDomain='").append(userDomain).append('\'');
        sb.append(", userName='").append(userName).append('\'');
        sb.append(", userRealName='").append(userRealName).append('\'');
        sb.append(", userSurname='").append(userSurname).append('\'');
        sb.append(", distinguishedName='").append(distinguishedName).append('\'');
        sb.append(", userPrincipalName='").append(userPrincipalName).append('\'');
        sb.append(", surname='").append(surname).append('\'');
        sb.append(", sid='").append(sid).append('\'');
        sb.append(", samAccountName='").append(samAccountName).append('\'');
        sb.append(", objectClass='").append(objectClass).append('\'');
        sb.append(", objectGUID='").append(objectGUID).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", enabled='").append(enabled).append('\'');
        sb.append(", givenName='").append(givenName).append('\'');
        sb.append(", inputName='").append(inputName).append('\'');
        sb.append(", ownerRights=").append(ownerRights.size());
        sb.append(", userPhoto=").append(userPhoto.hashCode());
        sb.append('}');
        return sb.toString();
    }
}
