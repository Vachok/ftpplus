package ru.vachok.networker.services;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

/**
 Локальная имплементация {@link MessageToUser}
 <p>

 @since 30.01.2019 (17:05) */
public class MessageLocal implements MessageToUser {

    private String bodyMsg = "NO BODY";

    private String titleMsg = "NO TITLE";

    private String headerMsg = "NO HEADER";

    public void warning(String bodyMsg) {
        this.bodyMsg = bodyMsg;
        errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    public void warn(String s) {
        this.bodyMsg = s;
        warning(bodyMsg);
    }

    @Override
    public void errorAlert(String headerMsg, String titleMsg, String bodyMsg) {
        this.headerMsg = headerMsg;
        this.titleMsg = titleMsg;
        this.bodyMsg = bodyMsg;
        new MessageCons().errorAlert(headerMsg, titleMsg, bodyMsg);
    }

    @Override
    public void info(String headerMsg, String titleMsg, String bodyMsg) {
        new MessageCons().info(headerMsg, titleMsg, bodyMsg);
    }

    @Override
    public void infoNoTitles(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageLocal{");
        sb.append("bodyMsg='").append(bodyMsg).append('\'');
        sb.append(", headerMsg='").append(headerMsg).append('\'');
        sb.append(", titleMsg='").append(titleMsg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
