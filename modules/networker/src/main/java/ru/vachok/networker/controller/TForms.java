package ru.vachok.networker.controller;


import org.slf4j.Logger;
import ru.vachok.networker.config.AppComponents;

import java.util.*;


/**
 * @since 06.09.2018 (9:33)
 */
public class TForms {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.logger();

    private StringBuilder brStringBuilder = new StringBuilder();

    public String fromArray(List<String> stringListWithBR) {
        brStringBuilder.append("<p>");
        for (String s : stringListWithBR) {
            brStringBuilder.append(s).append("<br>");
        }
        brStringBuilder.append("</p>");
        return brStringBuilder.toString();
    }

    public String fromArray(Map<String, String> ru_vachok_ethosdistro) {
        List<String> list = new ArrayList<>();
        ru_vachok_ethosdistro.forEach((x, y) -> {
            list.add(x + "  ::  " + y + "<br>\n");
        });
        Collections.sort(list);
        for(String s : list){
            brStringBuilder.append(s);
            LOGGER.info(s);
        }
        return brStringBuilder.toString();
    }
}
