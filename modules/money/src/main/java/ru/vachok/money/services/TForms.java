package ru.vachok.money.services;


import ru.vachok.messenger.MessageToUser;

import javax.mail.Address;
import java.util.*;


/**
 * @since 28.08.2018 (0:28)
 */
public class TForms {



    /*Fields*/
    /**
     * Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = TForms.class.getSimpleName();

    private static StringBuilder stringBuilder = new StringBuilder();
    /**
     * {@link }
     */
    private static MessageToUser messageToUser = new DBMessage();

    private List<String> fromArray = new ArrayList<>();

    public String toStringFromArray(Collection<String> headerNames) {
        stringBuilder.append("<p>");
        headerNames.forEach(x -> stringBuilder.append(x).append("<br>"));
        stringBuilder.append("</p>");
        return stringBuilder.toString();
    }

    public String toStringFromArray(Exception e) {
        stringBuilder.append("<p>");
        for(StackTraceElement stackTraceElement : e.getStackTrace()){
            stringBuilder.append(stackTraceElement.getLineNumber() +
                " line, in " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + " exception: " +
                stackTraceElement.toString());
        }
        return stringBuilder.toString();
    }

    public String mapLongString(Map<Long, String> cookiesToShow) {
        stringBuilder.append("LongString:\n");
        cookiesToShow.forEach((x, y) -> {
            stringBuilder.append(new Date(x)).append(": ").append(y).append("\n");
        });
        return stringBuilder.toString();
    }

    /*PS Methods*/
    public static String toStringFromArray(StackTraceElement[] e) {
        for(StackTraceElement element : e){
            stringBuilder.append(element.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStringFromArray(Map<String, String> map) {
        stringBuilder.append("<p>");
        map.forEach((x, y) -> {
            stringBuilder.append(x).append(" ; ");
            stringBuilder.append(y).append("<br>");
        });
        stringBuilder.append("</p>");
        return stringBuilder.toString();

    }

    public String toStringFromArray(Address[] from) {
        for(Address fr : from){
            stringBuilder.append(fr.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStringFromArray(List<String> stringList) {
        for (String s : stringList) {
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }
}