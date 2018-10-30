package ru.vachok.money.services;


import com.google.maps.model.DistanceMatrixElement;
import ru.vachok.money.other.XmlNode;

import javax.mail.Address;
import javax.servlet.http.Cookie;
import java.util.*;


/**
 @since 28.08.2018 (0:28) */
public class TForms {

    private static StringBuilder stringBuilder = new StringBuilder();

    private static StringBuilder nstringBuilder = new StringBuilder();

    private static final String BR_S = "<br>";

    private static final String N_S = "\n";

    public String toStringFromArray(Collection<String> headerNames) {
        stringBuilder.append("<p>");
        headerNames.forEach(x -> stringBuilder.append(x).append("<br>"));
        stringBuilder.append("</p>");
        return stringBuilder.toString();
    }

    /*PS Methods*/
    public static String toStringFromArray(StackTraceElement[] e) {
        for (StackTraceElement element : e) {
            stringBuilder.append(element.toString()).append("\n");
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

    public String toStringFromArray(Map<String, String> map) {
        stringBuilder.append("<p>");
        map.forEach((x, y) -> {
            stringBuilder.append(x).append(" ; ");
            stringBuilder.append(y).append("<br>");
        });
        stringBuilder.append("</p>");
        return stringBuilder.toString();

    }

    public String toStringFromArray(Exception e) {
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            stringBuilder
                .append(stackTraceElement.getLineNumber())
                .append(" line, in ").append(".").append(stackTraceElement.getMethodName())
                .append(" " +
                    "exception: ").append(stackTraceElement.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStringFromArray(Address[] from) {
        for (Address fr : from) {
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

    public String enumToString(Enumeration<String> yourEnum, boolean br) {
        stringBuilder.append("<p>");
        nstringBuilder.append("<p>");
        while (yourEnum.hasMoreElements()) {
            String str = yourEnum.nextElement();
            stringBuilder
                .append(str)
                .append("<br>");
            nstringBuilder
                .append(str)
                .append("<br>");
        }
        stringBuilder.append("</p>");
        nstringBuilder.append("</p>");
        if (br) {
            return stringBuilder.toString();
        } else {
            return nstringBuilder.toString();
        }
    }

    public String mapIntXmlNode(Map<Integer, XmlNode> integerXmlNodeMap) {
        stringBuilder.append("<p>");
        integerXmlNodeMap.forEach((x, y) -> stringBuilder.append(x)
            .append(") ")
            .append(y.toString())
            .append("<br>"));
        stringBuilder.append("</p>");
        return stringBuilder.toString();
    }

    public String toStringFromArray(String[] beanDefinitionNames) {
        stringBuilder.append("<p>");
        for (String s : beanDefinitionNames) {
            stringBuilder.append(s).append("<br>");
        }
        stringBuilder.append("</p>");
        return stringBuilder.toString();
    }

    public String toStringFromArray(Cookie[] cookies, boolean brB) {
        for (Cookie c : cookies) {
            String br = "<br>";
            String n = "\n";
            stringBuilder.append(br);
            nstringBuilder.append(n);
            stringBuilder
                .append(c.getName()).append(" name")
                .append(br)
                .append(c.getDomain()).append(" domain")
                .append(br)
                .append(c.getValue()).append(" value")
                .append(br)
                .append(c.getSecure()).append(" secure")
                .append(br)
                .append(c.getVersion()).append(" version");
            nstringBuilder
                .append(c.getName()).append(" name")
                .append(n)
                .append(c.getDomain()).append(" domain")
                .append(n)
                .append(c.getValue()).append(" value")
                .append(n)
                .append(c.getSecure()).append(" secure")
                .append(n)
                .append(c.getVersion()).append(" version");
        }
        if (brB) return stringBuilder.toString();
        else return nstringBuilder.toString();
    }

    public String toStringFromArray(DistanceMatrixElement[] elements, boolean br) {
        for (DistanceMatrixElement element : elements) {
            stringBuilder.append("<p>");
            stringBuilder.append(element.status).append(BR_S);
            stringBuilder
                .append(element.duration).append(" рассчётное время.")
                .append(BR_S)
                .append(element.durationInTraffic).append(" рассчётное время, с учётом траффика.")
                .append(BR_S)
                .append(element.distance).append(" дистанция");
            nstringBuilder.append(element.status).append(N_S);
            nstringBuilder
                .append(element.duration).append(" рассчётное время.")
                .append(N_S)
                .append(element.durationInTraffic).append(" рассчётное время, с учётом траффика.")
                .append(N_S)
                .append(element.distance).append(" дистанция");
        }
        if (br) return stringBuilder.toString();
        else return nstringBuilder.toString();
    }
}