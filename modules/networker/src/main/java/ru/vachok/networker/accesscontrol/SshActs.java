package ru.vachok.networker.accesscontrol;


import org.springframework.stereotype.Service;

/**
 SSH-actions class

 @since 29.11.2018 (13:01) */
@Service // TODO: 29.11.2018 aditem.html
public class SshActs {

    private Boolean tempFull = false;

    public Boolean isTempFull() {
        return tempFull;
    }

    public void setTempFull(boolean tempFull) {
        this.tempFull = tempFull;
    }
}
