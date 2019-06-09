package ru.vachok.networker.ad.user;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


/**
 <b>Хранилище информации о пользователе AD</b>

 @since 30.08.2018 (10:11) */
@Component (ConstantsFor.ATT_ADUSER)
@Scope("prototype")
public class ADUser {

    private static final char CHAR_SLASH = '\'';

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
    
    private MultipartFile usersAD;

    public MultipartFile getUsersAD() {
        return usersAD;
    }

    public void setUsersAD(MultipartFile usersAD) {
        this.usersAD = usersAD;
    }

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

    @Override
    public int hashCode() {
        int result = getDefaultDomainName().hashCode();
        result = 31 * result + (getUsersAD()!=null? getUsersAD().hashCode(): 0);
        result = 31 * result + (getSamAccountName()!=null? getSamAccountName().hashCode(): 0);
        result = 31 * result + (getInputName()!=null? getInputName().hashCode(): 0);
        return result;
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
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null || getClass()!=o.getClass()){
            return false;
        }

        ADUser adUser = ( ADUser ) o;

        return getDefaultDomainName().equals(adUser.getDefaultDomainName()) && (getUsersAD()!=null? getUsersAD().equals(adUser.getUsersAD()):
                                                                                    adUser.getUsersAD()==null) && (getSamAccountName()!=null?
                                                                                                                       getSamAccountName().equals(adUser.getSamAccountName()): adUser.getSamAccountName()==null) && (getInputName()!=null? getInputName().equals(adUser.getInputName()): adUser.getInputName()==null);
    }

    private String getDefaultDomainName() {
        return defaultDomainName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADUser{");
        sb.append("enabled='").append(enabled).append(CHAR_SLASH);
        sb.append(", inputName='").append(inputName).append(CHAR_SLASH);
        sb.append(", ownerRights=").append(new TForms().fromArray(ownerRights, false));
        sb.append(ConstantsFor.TOSTRING_SAMACCOUNTNAME).append(samAccountName).append(CHAR_SLASH);
        sb.append(", sid='").append(sid).append(CHAR_SLASH);
        sb.append(", usersAD=").append(usersAD.getSize());
        sb.append('}');
        return sb.toString();
    }
}
