package ru.vachok.networker.componentsrepo;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private Map<Integer, ADComputer> adComputers = new ConcurrentHashMap<>();

    public Map<Integer, ADComputer> getAdComputers() {
        return adComputers;
    }

    public void setAdComputers(Map<Integer, ADComputer> adComputers) {
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

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    @Override
    public String toString() {
        return "ADComputer{" +
            ", distinguishedName='" + distinguishedName + '\'' +
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
