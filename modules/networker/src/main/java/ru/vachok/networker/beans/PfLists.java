package ru.vachok.networker.beans;


import org.slf4j.Logger;
import ru.vachok.networker.config.AppComponents;
import ru.vachok.networker.logic.ssh.ListInternetUsers;

import java.util.Map;

/**
 * @since 10.09.2018 (11:35)
 */
public class PfLists extends Thread {

    private static Logger logger = AppComponents.getLogger();

    private String vipNet;

    private String stdSquid;

    private String limitSquid;

    private String fullSquid;

    private String allowDomain;

    private Map<String, String> listUsers = new ListInternetUsers().call();

    private String allowURL;

    public String getVipNet() {
        return vipNet;
    }

    public void setVipNet() {
        this.vipNet = listUsers.get("cat /etc/pf/vipnet");
        logger.info(vipNet);
    }

    public String getStdSquid() {
        return stdSquid;
    }

    public void setStdSquid() {
        this.stdSquid = listUsers.get("cat /etc/pf/squid");
        logger.info(stdSquid);
    }

    public String getLimitSquid() {
        return limitSquid;
    }

    public void setLimitSquid() {
        this.limitSquid = listUsers.get("cat /etc/pf/squidlimited");
        logger.info(limitSquid);
    }

    public String getFullSquid() {
        return fullSquid;
    }

    public void setFullSquid() {
        this.fullSquid = listUsers.get("cat /etc/pf/tempfull");
        logger.info(fullSquid);
    }

    public String getAllowDomain() {
        return allowDomain;
    }

    public void setAllowDomain() {
        this.allowURL = listUsers.get("cat /etc/pf/allowurl");
    }

    public String getAllowURL() {
        return allowURL;
    }

    public void setAllowURL() {
        this.allowDomain = listUsers.get("cat /etc/pf/allowdomain");
        logger.info(allowDomain);
    }

    @Override
    public String toString() {
        return "PfLists{" +
            "vipNet='" + vipNet + '\'' +
            ", stdSquid='" + stdSquid + '\'' +
            ", limitSquid='" + limitSquid + '\'' +
            ", fullSquid='" + fullSquid + '\'' +
            ", allowDomain='" + allowDomain + '\'' +
            ", allowURL='" + allowURL + '\'' +
            '}';
    }

    @Override
    public void run() {
        setLimitSquid();
        setFullSquid();
        setStdSquid();
        setAllowURL();
        setVipNet();
    }
}
