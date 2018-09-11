package ru.vachok.networker.beans;


import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import ru.vachok.networker.logic.ssh.ListInternetUsers;

import java.util.Map;

/**
 * @since 10.09.2018 (11:35)
 */
@Component
public class PfLists {

    private static Logger logger = AppComponents.getLogger();

    public String getVipNet() {
        return vipNet;
    }

    private String vipNet;

    public String getStdSquid() {
        return stdSquid;
    }

    private String stdSquid;

    public String getLimitSquid() {
        return limitSquid;
    }

    private String limitSquid;

    public String getFullSquid() {
        return fullSquid;
    }

    private String fullSquid;

    public String getAllowDomain() {
        return allowDomain;
    }

    private String allowDomain;

    private Map<String, String> listUsers = new ListInternetUsers().call();

    private String allowURL;

    public String getAllowURL() {
        return allowURL;
    }

    public void setVipNet(String vipNet) {
        this.vipNet = vipNet;
    }

    public void setStdSquid(String stdSquid) {
        this.stdSquid = stdSquid;
    }

    public void setLimitSquid(String limitSquid) {
        this.limitSquid = limitSquid;
    }

    public void setFullSquid(String fullSquid) {
        this.fullSquid = fullSquid;
    }

    public void setAllowDomain(String allowDomain) {
        this.allowDomain = allowDomain;
    }

    public Map<String, String> getListUsers() {
        return listUsers;
    }

    public void setListUsers(Map<String, String> listUsers) {
        this.listUsers = listUsers;
    }

    public void setAllowURL(String allowURL) {
        this.allowURL = allowURL;
    }

}
