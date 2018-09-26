package ru.vachok.money.services;


import ru.vachok.money.other.XmlNode;

import javax.mail.Address;
import java.util.*;


/**
 @since 28.08.2018 (0:28) */
public class TForms {

    private static StringBuilder stringBuilder = new StringBuilder();

    private static StringBuilder nstringBuilder = new StringBuilder();

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
        for(String s : stringList){
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }

    public String enumToString(Enumeration<String> yourEnum, boolean br) {
        stringBuilder.append("<p>");
        nstringBuilder.append("<p>");
        while(yourEnum.hasMoreElements()){
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
        if(br){
            return stringBuilder.toString();
        }
        else{
            return nstringBuilder.toString();
        }
    }

    public String toStringFromArray(String[] beanDefinitionNames) {
        stringBuilder.append("<p>");
        for(String s : beanDefinitionNames){
            stringBuilder.append(s).append("<br>");
        }
        stringBuilder.append("</p>");
        return stringBuilder.toString();
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

    /*PS Methods*/
    public static String toStringFromArray(StackTraceElement[] e) {
        for(StackTraceElement element : e){
            stringBuilder.append(element.toString()).append("\n");
        }
        return stringBuilder.toString();
    }
}