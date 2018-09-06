package ru.vachok.networker.controller;


import java.util.List;

/**
 * @since 06.09.2018 (9:33)
 */
public class TForms {

    private StringBuilder brStringBuilder = new StringBuilder();

    public String fromArray(List<String> stringListWithBR) {
        brStringBuilder.append("<p>");
        for (String s : stringListWithBR) {
            brStringBuilder.append(s).append("<br>");
        }
        brStringBuilder.append("</p>");
        return brStringBuilder.toString();
    }
}
