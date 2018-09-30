package ru.vachok.money.components;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.vachok.money.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 @since 14.09.2018 (19:45) */
@Component
public class Visitor {

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    private String sessionID;

    /*Fields*/
    private VisitorSrv visitorSrv;

    public VisitorSrv getVisitorSrv() {
        return visitorSrv;
    }

    /*Instances*/
    @Autowired
    public Visitor(VisitorSrv visitorSrv) {
        this.visitorSrv = visitorSrv;
    }

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = Visitor.class.getSimpleName();

    /**
     {@link }
     */

    private HttpServletRequest request;

    private HttpServletResponse response;

    private String ipAdd;

    private long timeStart;

    private long lastActivity;

    public static String getSourceClass() {
        return SOURCE_CLASS;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public String getIpAdd() {
        return ipAdd;
    }

    public void setIpAdd(String ipAdd) {
        this.ipAdd = ipAdd;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(long lastActivity) {
        this.lastActivity = lastActivity;
    }
}