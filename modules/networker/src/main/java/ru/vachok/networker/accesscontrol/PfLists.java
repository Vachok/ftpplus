package ru.vachok.networker.accesscontrol;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;


/**
 @since 10.09.2018 (11:35) */
@Component(ConstantsFor.PFLISTS)
@Scope(ConstantsFor.SINGLETON)
public class PfLists {

    private String vipNet;

    private String stdSquid;

    private String limitSquid;

    private String fullSquid;

    private String pfRules;

    private String pfNat;

    private long gitStats;

    private long timeUpd;

    private String uName;

    public void setuName(String uName) {
        this.uName = uName;
    }

    public void setTimeUpd(long timeUpd) {
        this.timeUpd = timeUpd;
    }

    public String getVipNet() {
        return vipNet;
    }

    public void setVipNet(String vipNet) {
        this.vipNet = vipNet;
    }

    public String getStdSquid() {
        return stdSquid;
    }

    public void setStdSquid(String stdSquid) {
        this.stdSquid = stdSquid;
    }

    public String getLimitSquid() {
        return limitSquid;
    }

    public void setLimitSquid(String limitSquid) {
        this.limitSquid = limitSquid;
    }

    public String getFullSquid() {
        return fullSquid;
    }

    public long getTimeUpd() {
        return timeUpd;
    }

    public void setFullSquid(String fullSquid) {
        this.fullSquid = fullSquid;
    }

    public long getGitStats() {
        return gitStats;
    }

    public void setGitStats(long gitStats) {
        this.gitStats = gitStats;
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

    public void setPfNat(String pfNat) {
        this.pfNat = pfNat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfLists{");
        sb.append("vipNet='").append(vipNet).append('\'');
        sb.append(", stdSquid='").append(stdSquid).append('\'');
        sb.append(", limitSquid='").append(limitSquid).append('\'');
        sb.append(", fullSquid='").append(fullSquid).append('\'');
        sb.append(", pfRules='").append(pfRules).append('\'');
        sb.append(", pfNat='").append(pfNat).append('\'');
        sb.append(", gitStats=").append(gitStats);
        sb.append(", timeUpd=").append(timeUpd);
        sb.append(", uName='").append(uName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
