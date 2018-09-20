package ru.vachok.money.components;


import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import ru.vachok.money.ConstantsFor;


/**
 @since 15.09.2018 (20:34) */
@Component ("inboxmessage")
public class MessageFromInbox {

    /*Fields*/

    /**
     {@link }
     */
    private static final Logger LOGGER = ConstantsFor.getLogger();

    private String from;

    private String to;

    private long when;

    private String subject;

    private String content;

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
        this.when = when;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MessageFromInbox{" +
            "content='" + content + '\'' +
            ", from='" + from + '\'' +
            ", LOGGER=" + LOGGER +
            ", subject='" + subject + '\'' +
            ", to='" + to + '\'' +
            ", when=" + when +
            '}';
    }
}