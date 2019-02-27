package ru.vachok.networker.accesscontrol;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.networker.ConstantsFor;


/**
 @since 10.09.2018 (11:35) */
@Component(ConstantsFor.BEANNAME_PFLISTS)
@Scope(ConstantsFor.SINGLETON)
public class PfLists {

    private String vipNet;

    private String stdSquid;

    private String limitSquid;

    private String fullSquid;

    private String pfRules;

    private String pfNat;

    private String inetLog;

    public String getInetLog() {
        return inetLog;
    }

    public void setInetLog(String inetLog) {
        this.inetLog = inetLog;
    }

    private long gitStatsUpdatedStampLong;

    private long timeStampToNextUpdLong = System.currentTimeMillis();

    private String uName;

    public void setuName(String uName) {
        this.uName = uName;
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

    public void setFullSquid(String fullSquid) {
        this.fullSquid = fullSquid;
    }

    public long getTimeStampToNextUpdLong() {
        return timeStampToNextUpdLong;
    }

    public void setTimeStampToNextUpdLong(long timeToNextUpd) {
        this.timeStampToNextUpdLong = timeToNextUpd;
    }

    public long getGitStatsUpdatedStampLong() {
        return gitStatsUpdatedStampLong;
    }

    public void setGitStatsUpdatedStampLong(long gitStatsUpdatedStampLong) {
        this.gitStatsUpdatedStampLong = gitStatsUpdatedStampLong;
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
    public int hashCode() {
        int result = getVipNet()!=null? getVipNet().hashCode(): 0;
        result = 31 * result + (getStdSquid()!=null? getStdSquid().hashCode(): 0);
        result = 31 * result + (getLimitSquid()!=null? getLimitSquid().hashCode(): 0);
        result = 31 * result + (getFullSquid()!=null? getFullSquid().hashCode(): 0);
        result = 31 * result + ( int ) (getTimeStampToNextUpdLong() ^ (getTimeStampToNextUpdLong() >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null || getClass()!=o.getClass()){
            return false;
        }

        PfLists pfLists = ( PfLists ) o;

        if(getTimeStampToNextUpdLong()!=pfLists.getTimeStampToNextUpdLong()){
            return false;
        }
        if(getVipNet()!=null? !getVipNet().equals(pfLists.getVipNet()): pfLists.getVipNet()!=null){
            return false;
        }
        if(getStdSquid()!=null? !getStdSquid().equals(pfLists.getStdSquid()): pfLists.getStdSquid()!=null){
            return false;
        }
        if(getLimitSquid()!=null? !getLimitSquid().equals(pfLists.getLimitSquid()): pfLists.getLimitSquid()!=null){
            return false;
        }
        return getFullSquid() != null ? getFullSquid().equals(pfLists.getFullSquid()) : pfLists.getFullSquid() == null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfLists{");
        sb.append("fullSquid='").append(fullSquid).append('\'');
        sb.append(", gitStatsUpdatedStampLong=").append(gitStatsUpdatedStampLong);
        sb.append(", inetLog='").append(inetLog).append('\'');
        sb.append(", limitSquid='").append(limitSquid).append('\'');
        sb.append(", pfNat='").append(pfNat).append('\'');
        sb.append(", pfRules='").append(pfRules).append('\'');
        sb.append(", stdSquid='").append(stdSquid).append('\'');
        sb.append(", timeStampToNextUpdLong=").append(timeStampToNextUpdLong);
        sb.append(", uName='").append(uName).append('\'');
        sb.append(", vipNet='").append(vipNet).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
