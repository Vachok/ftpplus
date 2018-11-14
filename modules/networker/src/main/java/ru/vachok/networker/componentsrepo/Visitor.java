package ru.vachok.networker.componentsrepo;


import ru.vachok.networker.ConstantsFor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

/**
 @since 12.08.2018 1:19
 */
public class Visitor {

    private static final long USER_ID = TimeUnit.MILLISECONDS.toSeconds(ConstantsFor.START_STAMP - System.currentTimeMillis());

    /**
     The Time st.
     */
    private String timeSpend = new StringBuilder()
        .append(System.currentTimeMillis() - ConstantsFor.START_STAMP)
        .append(" сек. идёт сессия.")
        .append("\n")
        .toString();

    private String visitPlace;

    private String dbInfo;

    /**
     The Rem addr.
     */
    private String remAddr;

    private HttpSession session;

    public Visitor(HttpServletRequest request) {
        this.request = request;
    }

    public HttpSession getSession() {
        return session;
    }

    public HttpServletRequest getRequest() throws NullPointerException {
        return request;
    }


    /**
     @param request {@link HttpServletRequest}
     @deprecated 07.11.2018 (13:58)
     */
    @Deprecated
    public void setRequest(HttpServletRequest request) {
        this.request = request;
        this.remAddr = request.getRemoteAddr();
        this.visitPlace = request.getPathInfo();
        this.session = request.getSession();
        getVisitsMap().put(USER_ID, request);
    }

    /**
     <i>{@link #setRequest(HttpServletRequest)}</i>

     @return {@link ConstantsFor#VISITS_MAP}
     */
    public Map<Long, HttpServletRequest> getVisitsMap() {
        return ConstantsFor.VISITS_MAP;
    }

    private HttpServletRequest request;

    private int clickCounter;

    private Collection<Cookie> cookieCollection = new ArrayList<>();

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

    public String getVisitPlace() {
        return visitPlace;
    }

    public String getDbInfo() {
        return dbInfo;
    }

    public void setDbInfo(String dbInfo) {
        this.dbInfo = dbInfo;
    }

    /**
     Gets time st.

     @return the time st
     */
    public String getTimeSpend() {
        return timeSpend;
    }

    /**
     Gets rem addr.

     @return the rem addr
     */
    public String getRemAddr() {
        return remAddr;
    }

    /**
     Sets rem addr.

     @param remAddr the rem addr
     */
    public void setRemAddr(String remAddr) {
        this.remAddr = remAddr;
    }

    @Override
    public String toString() {
        return new StringJoiner("\n", Visitor.class.getSimpleName() + "\n", "\n")
            .add("remAddr='" + remAddr + "'\n")
            .add("timeSpend=" + timeSpend)
            .add("userID=" + getUserID())
            .toString();
    }

    public long getUserID() {
        return USER_ID;
    }
}
