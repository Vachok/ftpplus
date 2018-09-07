package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.config.AppComponents;

import java.io.File;
import java.util.*;


/**
 * @since 06.09.2018 (9:33)
 */
public class TForms {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private StringBuilder brStringBuilder = new StringBuilder();

    private StringBuilder nStingBuilder = new StringBuilder();

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
        for (String s : list) {
            brStringBuilder.append(s);
            LOGGER.info(s);
        }
        return brStringBuilder.toString();
    }

    public String fromArray(File[] dirFiles) {
        for (File f : dirFiles) {
            if (f.getName().contains(".jar")) return f.getName().replace(".jar", "");
            else return System.getProperties().getProperty("version");
        }
        throw new UnsupportedOperationException("Хуя ты ХЕРург");
    }

    public String fromArray(Properties properties) {
        InitProperties initProperties = new FileProps(ConstantsFor.APP_NAME);
        initProperties.setProps(properties);
        nStingBuilder.append("\n");
        properties.forEach((x, y) -> {
            LOGGER.info(x + " : " + y);
            nStingBuilder.append(x).append(" :: ").append(y).append("\n");
        });
        return nStingBuilder.toString();
    }
}
