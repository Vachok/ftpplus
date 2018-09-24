package ru.vachok.networker.componentsrepo;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by 14350 on 12.08.2018 1:19
 */
@Component
@Scope("prototype")
public class Visitor {

    /**
     * The Time st.
     */
    private long timeSt;

    /**
     * The Rem addr.
     */
    private String remAddr;

    private String visitPlace;

    private String dbInfo;

    private String userID;

    private HttpServletRequest request;

    private int clickCounter;

    private Collection<Cookie> cookieCollection = new ArrayList<>();

    public Visitor(HttpServletRequest request) {
        this.request = request;
    }

    public int getClickCounter() {
        return clickCounter;
    }

    public void setClickCounter(int clickCounter) {
        this.clickCounter = this.clickCounter + clickCounter;
    }

    public Collection<Cookie> getCookieCollection() {
        return cookieCollection;
    }

    public void setCookieCollection(Collection<Cookie> cookieCollection) {
        this.cookieCollection = cookieCollection;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getVisitPlace() {
        return visitPlace;
    }

    public void setVisitPlace(String visitPlace) {
        this.visitPlace = visitPlace;
    }

    public String getDbInfo() {
        return dbInfo;
    }

    public void setDbInfo(String dbInfo) {
        this.dbInfo = dbInfo;
    }

    private Map<Long, String> visitsMap = new ConcurrentHashMap<>();

    public Map<Long, String> getVisitsMap() {
        return visitsMap;
    }

    public void setVisitsMap(Map<Long, String> visitsMap) {
        this.visitsMap = visitsMap;
    }

    /**
     * Gets time st.
     *
     * @return the time st
     */
    public long getTimeSt() {
        return timeSt;
    }


    /**
     * Sets time st.
     *
     * @param timeSt the time st
     */
    public void setTimeSt(long timeSt) {
        this.timeSt = timeSt;
    }


    /**
     * Gets rem addr.
     *
     * @return the rem addr
     */
    public String getRemAddr() {
        return remAddr;
    }


    /**
     * Sets rem addr.
     *
     * @param remAddr the rem addr
     */
    public void setRemAddr(String remAddr) {
        this.remAddr = remAddr;
    }

    @Override
    public String toString() {
        return "Visitor{" +
            "remAddr='" + remAddr + '\'' +
            ", timeSt=" + timeSt +
            '}' +
            "\n<br>";
    }
}
