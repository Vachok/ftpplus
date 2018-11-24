package ru.vachok.money.services;


import com.google.maps.model.DistanceMatrixElement;
import ru.vachok.money.other.XmlNode;

import javax.mail.Address;
import javax.servlet.http.Cookie;
import java.util.*;
import java.util.concurrent.ConcurrentMap;


/**
 @since 28.08.2018 (0:28) */
public class TForms {

    /*Fields*/
    private static final String BR_S = "<br>";

    private static final String N_S = "\n";

    private StringBuilder brStrBuilder = new StringBuilder();

    private StringBuilder nStringBuilder = new StringBuilder();

    public String toStringFromArray(Collection<String> headerNames) {
        brStrBuilder.append("<p>");
        headerNames.forEach(x -> brStrBuilder.append(x).append("<br>"));
        brStrBuilder.append("</p>");
        return brStrBuilder.toString();
    }

    public String toStringFromArray(List<?> listSome, boolean br) {
        brStrBuilder.append("<p>");
        nStringBuilder.append("\n");
        listSome.forEach(x -> {
            brStrBuilder.append(x)
                .append("<br>");
            nStringBuilder.append(x)
                .append("\n");
        });
        if(br){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String mapLongString(Map<Long, String> cookiesToShow) {
        brStrBuilder.append("LongString:\n");
        cookiesToShow.forEach((x, y) -> {
            brStrBuilder.append(new Date(x)).append(": ").append(y).append("\n");
        });
        return brStrBuilder.toString();
    }

    public String toStringFromArray(Map<String, String> map) {
        brStrBuilder.append("<p>");
        map.forEach((x, y) -> {
            brStrBuilder.append(x).append(" ; ");
            brStrBuilder.append(y).append("<br>");
        });
        brStrBuilder.append("</p>");
        return brStrBuilder.toString();

    }

    public String toStringFromArray(Exception e, boolean br) {
        brStrBuilder.append("<p>");
        for(StackTraceElement stackTraceElement : e.getStackTrace()){
            brStrBuilder
                .append(stackTraceElement.getLineNumber())
                .append(" line, in ").append(".").append(stackTraceElement.getMethodName())
                .append(" " +
                    "exception: ").append(stackTraceElement.toString());
            brStrBuilder.append("<br>");

            nStringBuilder
                .append(stackTraceElement.getLineNumber())
                .append(" line, in ").append(stackTraceElement.getMethodName())
                .append(" " + "exception: ")
                .append(stackTraceElement.toString());
            nStringBuilder.append("\n");
        }
        if(br){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String toStringFromArray(Address[] from) {
        for(Address fr : from){
            brStrBuilder.append(fr.toString()).append("\n");
        }
        return brStrBuilder.toString();
    }

    public String toStringFromArray(List<String> stringList) {
        for(String s : stringList){
            nStringBuilder.append(s).append("\n");
        }
        return nStringBuilder.toString();
    }

    public String toStringFromArray(ConcurrentMap<?, ?> mapOfSome, boolean b) {
        brStrBuilder.append("<p>");
        nStringBuilder.append("\n");
        mapOfSome.forEach((x, y) -> {
            brStrBuilder.append(x.toString()).append(" x  |  ")
                .append(y.toString()).append(" y  |||<br>");
            nStringBuilder.append(x.toString()).append(" x  |  ")
                .append(y.toString()).append(" y  |||\n");
        });
        if(b){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String mapIntXmlNode(Map<Integer, XmlNode> integerXmlNodeMap) {
        brStrBuilder.append("<p>");
        integerXmlNodeMap.forEach((x, y) -> brStrBuilder.append(x)
            .append(") ")
            .append(y.toString())
            .append("<br>"));
        brStrBuilder.append("</p>");
        return brStrBuilder.toString();
    }

    public String toStringFromArray(String[] beanDefinitionNames) {
        brStrBuilder.append("<p>");
        for(String s : beanDefinitionNames){
            brStrBuilder.append(s).append("<br>");
        }
        brStrBuilder.append("</p>");
        return brStrBuilder.toString();
    }

    public String toStringFromArray(Cookie[] cookies, boolean brB) {
        for(Cookie c : cookies){
            String br = "<br>";
            String n = "\n";
            brStrBuilder.append(br);
            nStringBuilder.append(n);
            brStrBuilder
                .append(c.getName()).append(" name")
                .append(br)
                .append(c.getDomain()).append(" domain")
                .append(br)
                .append(c.getValue()).append(" value")
                .append(br)
                .append(c.getSecure()).append(" secure")
                .append(br)
                .append(c.getVersion()).append(" version");
            nStringBuilder
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
        if(brB){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String toStringFromArray(DistanceMatrixElement[] elements, boolean br) {
        for(DistanceMatrixElement element : elements){
            brStrBuilder.append("<p>");
            brStrBuilder.append(element.status).append(BR_S);
            brStrBuilder
                .append(element.duration).append(" рассчётное время.")
                .append(BR_S)
                .append(element.durationInTraffic).append(" рассчётное время, с учётом траффика.")
                .append(BR_S)
                .append(element.distance).append(" дистанция");
            nStringBuilder.append(element.status).append(N_S);
            nStringBuilder
                .append(element.duration).append(" рассчётное время.")
                .append(N_S)
                .append(element.durationInTraffic).append(" рассчётное время, с учётом траффика.")
                .append(N_S)
                .append(element.distance).append(" дистанция");
        }
        if(br){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String stackToString(Stack<?> stack, boolean br) {
        brStrBuilder.append("<p>");
        stack.forEach(x -> {
            String s = stack.pop().toString();
            brStrBuilder
                .append(s)
                .append("<br>");
            nStringBuilder
                .append(s)
                .append("\n");
        });
        if(br){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    String enumToString(Enumeration<String> yourEnum, boolean br) {
        brStrBuilder.append("<p>");
        nStringBuilder.append("<p>");
        while(yourEnum.hasMoreElements()){
            String str = yourEnum.nextElement();
            brStrBuilder
                .append(str)
                .append("<br>");
            nStringBuilder
                .append(str)
                .append("<br>");
        }
        brStrBuilder.append("</p>");
        nStringBuilder.append("</p>");
        if(br){
            return brStrBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }
}