package ru.vachok.networker.componentsrepo;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    private Map<Long, String> visitsMap = new ConcurrentHashMap<>();

    public Map<Long, String> getVisitsMap() {
        return visitsMap;
    }

    public void setVisitsMap(Map<Long, String> visitsMap) {
        this.visitsMap = visitsMap;
    }

    @Override
    public String toString() {
        return "Visitor{" +
            "timeSt=" + timeSt +
            ", remAddr='" + remAddr + '\'' +
            '}';
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
}
