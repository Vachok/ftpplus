package ru.vachok.networker.services;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 @since 17.01.2019 (9:52) */
@Service("anketaKonfeta")
public class AnketaKonfeta {

    private String userMail;

    private String q1Ans;

    private String q2Ans;

    private String additionalString;

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
        new Thread(this::sendKonfeta).start();
    }

    public void sendKonfeta() {
        List<String> emailsList = new ArrayList<>();
        MessageToUser messageToUser = new ESender(emailsList);
        emailsList.add("143500@gmail.com");
        try {
            if (this.userMail.isEmpty()) {
                emailsList.add("ikudryashov@velokmfood.ru");
            } else {
                emailsList.add(userMail);
            }
            AppComponents.getLogger().info(toString());
        } catch (NullPointerException e) {
            messageToUser.errorAlert(this.getClass().getSimpleName(), "sendKonfeta", e.getMessage() + " in 64 ");
        }
        messageToUser.info(getClass().getSimpleName(), new Date().toString(), toString());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AnketaKonfeta{");
        sb.append("additionalString='").append(additionalString).append('\'');
        sb.append(", q1Ans='").append(q1Ans).append('\'');
        sb.append(", q2Ans='").append(q2Ans).append('\'');
        sb.append(", userMail='").append(userMail).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public void setAll() {
        setQ1Ans("");
        setUserMail("");
        setAdditionalString("");
        setQ2Ans("");
    }
}
