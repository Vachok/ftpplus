// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo;



import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 @since 12.08.2018 1:19 */
public class Visitor {

    /**
     Время в мс инициализации класса
     */
    private static final long ST_ART = System.currentTimeMillis();

    private static final String STR_VISIT = ConstantsFor.getStringsVisit()[0];

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

    private String visitPlace;

    /**
     The Rem addr.
     */
    private String remAddr;

    private HttpSession session;
    
    private HttpServletRequest request;


    public Visitor( HttpServletRequest request ) throws NullPointerException, IllegalStateException {
        List<String> visitList = new ArrayList<>();
        this.request = request;
        this.session = request.getSession();
        this.visitPlace = request.getHeader(ConstantsFor.ATT_REFERER.toLowerCase());
        this.remAddr = request.getRemoteAddr();
        this.userId = session.getId();
        visitList.add(new Date(System.currentTimeMillis()).toString());
        visitList.add(this.toString());
        visitList.add(AppComponents.versionInfo().toString());
        FileSystemWorker.writeFile(STR_VISIT + userId , visitList);
    }


    public HttpSession getSession() {
        return session;
    }


    public HttpServletRequest getRequest() throws NullPointerException {
        return request;
    }


    public String getUserId() {
        return userId;
    }


    public int getClickCounter() {
        return clickCounter;
    }


    public void setClickCounter( int clickCounter ) {
        this.clickCounter = clickCounter;
    }


    public String getVisitPlace() {
        return visitPlace;
    }


    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + getTimeSpend().hashCode();
        result = 31 * result + (getVisitPlace() != null ? getVisitPlace().hashCode() : 0);
        result = 31 * result + (getRemAddr() != null ? getRemAddr().hashCode() : 0);
        result = 31 * result + (getSession() != null ? getSession().hashCode() : 0);
        result = 31 * result + getRequest().hashCode();
        result = 31 * result + getClickCounter();
        return result;
    }


    @Override
    public boolean equals( Object o ) {
        if (this == o) return true;
        if (!(o instanceof Visitor)) return false;

        Visitor visitor = (Visitor) o;

        if (getClickCounter() != visitor.getClickCounter()) return false;
        if (userId != null ? !userId.equals(visitor.userId) : visitor.userId != null) return false;
        if (!getTimeSpend().equals(visitor.getTimeSpend())) return false;
        if (getVisitPlace() != null ? !getVisitPlace().equals(visitor.getVisitPlace()) : visitor.getVisitPlace() != null) return false;
        if (getRemAddr() != null ? !getRemAddr().equals(visitor.getRemAddr()) : visitor.getRemAddr() != null) return false;
        if (getSession() != null ? !getSession().equals(visitor.getSession()) : visitor.getSession() != null) return false;
        return getRequest().equals(visitor.getRequest());
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
        sb.append(", visitPlace='").append(visitPlace).append('\'');
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