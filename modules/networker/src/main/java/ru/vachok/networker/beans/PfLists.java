package ru.vachok.networker.beans;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * @since 10.09.2018 (11:35)
 */
@Component
@Scope ("singleton")
public class PfLists {

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

    private String pfRules;

    private String pfNat;

    private String uName;

    /*Get&Set*/
    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getPfRules() {
        return pfRules;
    }

    public void setPfRules(String pfRules) {
        this.pfRules = pfRules;
    }

    public String getUname() {
        return uName;
    }

    public String getPfNat() {
        return pfNat;
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

    public void setPfNat(String pfNat) {
        this.pfNat = pfNat;
    }

    @Override
    public String toString() {
        return "PfLists{" +
            "fullSquid='" + fullSquid + '\'' +
            ", limitSquid='" + limitSquid + '\'' +
            ", pfNat='" + pfNat + '\'' +
            ", pfRules='" + pfRules + '\'' +
            ", stdSquid='" + stdSquid + '\'' +
            ", uName='" + uName + '\'' +
            ", vipNet='" + vipNet + '\'' +
            '}';
    }
}
