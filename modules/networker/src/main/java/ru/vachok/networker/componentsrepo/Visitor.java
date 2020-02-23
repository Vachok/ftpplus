// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;


import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 @see ru.vachok.networker.componentsrepo.VisitorTest
 @since 12.08.2018 1:19 */
public class Visitor implements Serializable {


    /**
     Время в мс инициализации класса
     */
    private static final long ST_ART = System.currentTimeMillis();

    private String userId;


    /**
     The Time st.
     */
    private String timeSpend = new StringBuilder()
        .append((float) (System.currentTimeMillis() - ST_ART) / 1000)
        .append(" сек. идёт сессия.")
        .append("\n")
        .toString();

    private int clickCounter;

    /**
     The Rem addr.
     */
    private String remAddr;

    private HttpSession session;

    private HttpServletRequest request;

    public Visitor(@NotNull HttpServletRequest request) throws RuntimeException {
        List<String> visitList = new ArrayList<>();
        this.request = request;
        this.session = request.getSession();
        this.remAddr = request.getRemoteAddr();
        this.userId = session.getId();
        visitList.add(new Date(System.currentTimeMillis()).toString());
    }


    public HttpSession getSession() {
        return session;
    }


    public HttpServletRequest getRequest() throws NullPointerException {
        return request;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Visitor{");
        sb.append("clickCounter=").append(clickCounter);
        sb.append(", remAddr='").append(remAddr).append('\'');
        sb.append(", request=").append(request.getPathInfo());
        sb.append(", session=").append(session.getServletContext().getServerInfo());
        sb.append(", ST_ART=").append(ST_ART);
        sb.append(", timeSpend='").append(timeSpend).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
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
}