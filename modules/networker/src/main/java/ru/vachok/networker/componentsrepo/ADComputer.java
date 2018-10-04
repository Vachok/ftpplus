package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 <h1>get-adcomputer</h1>

 @since 25.09.2018 (15:00) */
@Component("adcomputer")
public class ADComputer {

    private String distinguishedName;

    private String dnsHostName;

    private String enabled;

    private String name;

    private String objectClass;

    private String objectGUID;

    private String samAccountName;

    private String SID;

    private String userPrincipalName;

    private List<ADComputer> adComputers = new ArrayList<>();

    public List<ADComputer> getAdComputers() {
        return adComputers;
    }

    public void setAdComputers(List<ADComputer> adComputers) {
        this.adComputers = adComputers;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getDnsHostName() {
        return dnsHostName;
    }

    public void setDnsHostName(String dnsHostName) {
        this.dnsHostName = dnsHostName;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getObjectGUID() {
        return objectGUID;
    }

    public void setObjectGUID(String objectGUID) {
        this.objectGUID = objectGUID;
    }

    public String getSamAccountName() {
        return samAccountName;
    }

    public void setSamAccountName(String samAccountName) {
        this.samAccountName = samAccountName;
    }

    public String getSID() {
        return SID;
    }

    public void setSID(String SID) {
        this.SID = SID;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    @Override
    public int hashCode() {
        int result = distinguishedName != null ? distinguishedName.hashCode() : 0;
        result = 31 * result + (dnsHostName != null ? dnsHostName.hashCode() : 0);
        result = 31 * result + (enabled != null ? enabled.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (objectClass != null ? objectClass.hashCode() : 0);
        result = 31 * result + (objectGUID != null ? objectGUID.hashCode() : 0);
        result = 31 * result + (samAccountName != null ? samAccountName.hashCode() : 0);
        result = 31 * result + (SID != null ? SID.hashCode() : 0);
        result = 31 * result + (userPrincipalName != null ? userPrincipalName.hashCode() : 0);
        result = 31 * result + (adComputers != null ? adComputers.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ADComputer that = (ADComputer) o;

        if (distinguishedName != null ? !distinguishedName.equals(that.distinguishedName) : that.distinguishedName != null) return false;
        if (dnsHostName != null ? !dnsHostName.equals(that.dnsHostName) : that.dnsHostName != null) return false;
        if (enabled != null ? !enabled.equals(that.enabled) : that.enabled != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (objectClass != null ? !objectClass.equals(that.objectClass) : that.objectClass != null) return false;
        if (objectGUID != null ? !objectGUID.equals(that.objectGUID) : that.objectGUID != null) return false;
        if (samAccountName != null ? !samAccountName.equals(that.samAccountName) : that.samAccountName != null) return false;
        if (SID != null ? !SID.equals(that.SID) : that.SID != null) return false;
        if (userPrincipalName != null ? !userPrincipalName.equals(that.userPrincipalName) : that.userPrincipalName != null) return false;
        return adComputers != null ? adComputers.equals(that.adComputers) : that.adComputers == null;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    @Override
    public String toString() {
        return "ADComputer{" +
            "distinguishedName='" + distinguishedName + '\'' +
            ", dnsHostName='" + dnsHostName + '\'' +
            ", enabled='" + enabled + '\'' +
            ", name='" + name + '\'' +
            ", objectClass='" + objectClass + '\'' +
            ", objectGUID='" + objectGUID + '\'' +
            ", samAccountName='" + samAccountName + '\'' +
            ", SID='" + SID + '\'' +
            ", userPrincipalName='" + userPrincipalName + '\'' +
            '}';
    }
}
