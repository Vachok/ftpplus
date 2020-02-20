// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.springframework.stereotype.Component;
import ru.vachok.networker.data.enums.ConstantsFor;


/**
 @since 10.09.2018 (11:35) */
@SuppressWarnings({"InstanceVariableMayNotBeInitialized"})
@Component(ConstantsFor.BEANNAME_PFLISTS)
public class PfLists {


    private String vipNet;

    private String stdSquid;

    private String limitSquid;

    private String fullSquid;

    private String pfRules;

    private String pfNat;

    private String inetLog;

    private long gitStatsUpdatedStampLong;

    private long timeStampToNextUpdLong = System.currentTimeMillis();

    private String uName;

    public String getInetLog() {
        return inetLog;
    }

    public void setInetLog(String inetLog) {
        this.inetLog = inetLog;
    }

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

    @SuppressWarnings("SpellCheckingInspection")
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
        JsonArray jsonArray = new JsonArray();
        JsonObject[] jsonObjects = {new JsonObject().add("vipNet", this.vipNet), new JsonObject().add("stdSquid", stdSquid), new JsonObject().add("limitSquid", limitSquid),
                new JsonObject().add("fullSquid", fullSquid), new JsonObject().add("pfRules", pfRules), new JsonObject().add("pfNat", pfNat), new JsonObject().add("inetLog", inetLog)};
        for (JsonObject jsonObject : jsonObjects) {
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }
}
