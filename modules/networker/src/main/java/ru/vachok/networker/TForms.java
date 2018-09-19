package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.File;
import java.util.*;


/**
 * @since 06.09.2018 (9:33)
 */
public class TForms {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private StringBuilder brStringBuilder = new StringBuilder();

    private StringBuilder nStringBuilder = new StringBuilder();

    public String fromArray(List<String> stringListWithBR) {
        brStringBuilder.append("<p>");
        for (String s : stringListWithBR) {
            brStringBuilder.append(s).append("<br>");
        }
        brStringBuilder.append("</p>");
        return brStringBuilder.toString();
    }

    public String fromArray(Map<String, String> stringStringMap) {
        List<String> list = new ArrayList<>();
        stringStringMap.forEach((x, y) -> list.add(x + "    " + y + "<br>\n"));
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
        nStringBuilder.append("\n");
        properties.forEach((x, y) -> {
            String msg = x + " : " + y;
            LOGGER.info(msg);
            nStringBuilder.append(x).append(" :: ").append(y).append("\n");
        });
        return nStringBuilder.toString();
    }

    public String fromStringBoolean(Map<String, Boolean> call) {
        brStringBuilder.append("<p>");
        call.forEach((x, y) -> {
            String msg = x + y;
            LOGGER.info(msg);
            brStringBuilder
                .append(x)
                .append(" - ")
                .append(y)
                .append("<br>");
        });
        brStringBuilder.append("</p>");
        return brStringBuilder.toString();
    }

    public String stringObjectMapParser(Map<String, Object> stringObjectMap) {
        stringObjectMap.forEach((x, y) -> {
            nStringBuilder.append(x).append("  ").append(y.toString()).append("\n");
        });
        return nStringBuilder.toString();
    }

    public String fromArray(StackTraceElement[] stackTrace) {
        for (StackTraceElement stackTraceElement : stackTrace) {
            nStringBuilder
                .append("At ")
                .append(stackTraceElement
                    .getClassName())
                .append(" line, class: ")
                .append(stackTraceElement.getClassName())
                .append(" occurred disaster!\n")
                .append(stackTraceElement.getMethodName())
                .append(" method.\nFile: ")
                .append(stackTraceElement.getFileName());
        }
        return nStringBuilder.toString();
    }

    public String fromArray(String[] stringsArray) {
        for(String s : stringsArray){
            nStringBuilder.append(s).append("\n");
        }
        return nStringBuilder.toString();
    }

    public String mapLongString(Map<Long, String> visitsMap) {
        visitsMap.forEach((x, y) -> nStringBuilder.append(x).append(" | ").append(y).append("\n"));
        return nStringBuilder.toString();
    }

    public String fromEnum(Enumeration<String> enumStrings) {
        while (enumStrings.hasMoreElements()) {
            nStringBuilder.append(enumStrings.nextElement()).append("\n");
        }
        return nStringBuilder.toString();
    }
}
