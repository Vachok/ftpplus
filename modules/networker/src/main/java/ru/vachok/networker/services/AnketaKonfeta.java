// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.restapi.message.MessageLocal;


/**
 @since 17.01.2019 (9:52) */
@Service("anketaKonfeta")
public class AnketaKonfeta {

    private String userMail;
    
    private String userIp;
    
    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }
    
    private String q1Ans;

    private String q2Ans;

    private String additionalString;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public String getQ2Ans() {
        return q2Ans;
    }

    public void setQ2Ans(String q2Ans) {
        this.q2Ans = q2Ans;
    }

    public String getAdditionalString() {
        return additionalString;
    }

    public void setAdditionalString(String additionalString) {
        this.additionalString = additionalString;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public String getQ1Ans() {
        return q1Ans;
    }

    public void setQ1Ans(String q1Ans) {
        this.q1Ans = q1Ans;
    }

    public void sendKonfeta(String addStr) {
        setAdditionalString(addStr);
        boolean writeKonfeta = new ExitApp("anketa.", this).writeOwnObject();
        final String classMeth = "AnketaKonfeta.sendKonfeta";
        messageToUser.info(classMeth, "writeKonfeta", " = " + writeKonfeta);
        messageToUser.info(classMeth, "toString()", " = " + this);
    }
    
    public void setAllAsEmptyString() {
        setQ1Ans("");
        setUserMail("");
        setAdditionalString("");
        setQ2Ans("");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnketaKonfeta{");
        sb.append("additionalString='").append(additionalString).append('\'');
        sb.append(", q1Ans='").append(q1Ans).append('\'');
        sb.append(", q2Ans='").append(q2Ans).append('\'');
        sb.append(", userIp='").append(userIp).append('\'');
        sb.append(", userMail='").append(userMail).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
