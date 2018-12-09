package ru.vachok.networker;


import org.slf4j.Logger;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.mailserver.MailRule;

import javax.mail.Address;
import javax.servlet.http.Cookie;
import java.io.File;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentMap;


/**
 <h1>Помощник для {@link Arrays#toString(int[])}</h1>
 Делает похожие действия, но сразу так, как нужно для {@link ru.vachok.networker.IntoApplication}

 @since 06.09.2018 (9:33) */
public class TForms {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();

    private StringBuilder brStringBuilder = new StringBuilder();

    private StringBuilder nStringBuilder = new StringBuilder();

    private static final String N_S = "\n";

    private static final String BR_S = "<br>";

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
            if (f.getName().contains(".jar")) {
                return f.getName().replace(".jar", "");
            } else {
                return System.getProperties().getProperty("version");
            }
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

    public String mapStringBoolean(Map<String, Boolean> call) {
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

    public String fromArray(Exception e, boolean br) {
        brStringBuilder.append("<p>");
        nStringBuilder.append("\n");
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
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
            brStringBuilder
                .append("At ")
                .append(stackTraceElement
                    .getClassName())
                .append(" line, class: ")
                .append(stackTraceElement.getClassName())
                .append(" occurred disaster!<br>")
                .append(stackTraceElement.getMethodName())
                .append(" method.<br>File: ")
                .append(stackTraceElement.getFileName());
        }
        if (!br) return nStringBuilder.toString();
        else return brStringBuilder.toString();
    }

    public String fromArray(String[] stringsArray) {
        for (String s : stringsArray) {
            nStringBuilder.append(s).append("\n<br>");
        }
        return nStringBuilder.toString();
    }

    public String mapLongString(Map<Long, String> visitsMap) {
        visitsMap.forEach((x, y) -> nStringBuilder.append(x).append(" | ").append(y).append("\n"));
        return nStringBuilder.toString();
    }

    public String fromEnum(Enumeration<String> enumStrings, boolean br) {
        nStringBuilder.append("\n");
        brStringBuilder.append("<p>");
        while (enumStrings.hasMoreElements()) {
            String str = enumStrings.nextElement();
            nStringBuilder.append(str).append("\n");
            brStringBuilder.append(str).append("<br>");
        }
        nStringBuilder.append("\n");
        brStringBuilder.append("</p>");
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Queue<String> stringQueue) {
        brStringBuilder.append("<p>");
        while (stringQueue.iterator().hasNext()) {
            brStringBuilder.append(stringQueue.poll()).append("<br>");
        }
        brStringBuilder.append("</p>");
        return brStringBuilder.toString();
    }

    public String fromArray(Map<String, Boolean> stringBooleanMap, boolean br) {
        List<String> stringList = new ArrayList<>();
        stringBooleanMap.forEach((x, y) -> {
            stringList.add(x + " " + y);
        });
        Collections.sort(stringList);
        brStringBuilder.append("<p>");
        for (String s : stringList) {
            brStringBuilder.append(s).append("<br>");
            nStringBuilder.append(s).append("\n");
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String mapStrStrArr(Map<String, String[]> parameterMap, boolean br) {
        brStringBuilder.append("<p>");
        parameterMap.forEach((x, y) -> {
            brStringBuilder.append("<h4>").append(x).append("</h4><br>");
            int i = 1;
            for (String s : y) {
                brStringBuilder.append(i++).append(")").append(s).append("<br>");
                nStringBuilder.append(i++).append(")").append(s).append("\n");
            }
            nStringBuilder.append(x).append("\n");
            brStringBuilder.append("</p>");
        });
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Cookie[] cookies, boolean br) {
        brStringBuilder.append("<p>");
        for (Cookie c : cookies) {
            brStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append("<br>");
            nStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append("\n");
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromADUsersList(List<ADUser> adUsers, boolean br) {
        nStringBuilder.append("\n");
        for (ADUser ad : adUsers) {
            brStringBuilder
                .append(ad.toStringBR());
            nStringBuilder
                .append(ad.toString())
                .append("\n");
        }
        nStringBuilder.append("\n");
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String adPCMap(List<ADComputer> adComputers, boolean br) {
        brStringBuilder.append("<p>");
        nStringBuilder.append("\n");
        for (ADComputer ad : adComputers) {
            brStringBuilder
                .append(ad.toString())
                .append("<br>");
            nStringBuilder
                .append(ad.toString())
                .append("\n\n");
        }
        brStringBuilder.append("</p>");
        nStringBuilder.append("\n\n\n");
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String adMap(Map<ADComputer, ADUser> adComputerADUserMap) {
        adComputerADUserMap.forEach((x, y) -> {
            brStringBuilder.append("<p>");
            brStringBuilder
                .append(x.toString())
                .append("<br>")
                .append(y.toString())
                .append("</p>");
        });
        return brStringBuilder.toString();
    }

    public String fromArray(Address[] mailAddress, boolean br) {
        for (Address address : mailAddress) {
            brStringBuilder
                .append(address.toString())
                .append("br");
            nStringBuilder
                .append(address.toString())
                .append("\n");
        }
        if (br) return brStringBuilder.toString();
        else return nStringBuilder.toString();
    }

    public String fromArray(Throwable[] suppressed) {
        nStringBuilder.append("suppressed throwable!\n".toUpperCase());
        for (Throwable throwable : suppressed) {
            nStringBuilder.append(throwable.getMessage());
        }
        return nStringBuilder.toString();
    }

    public String fromArray(Set<?> cacheSet, boolean br) {
        brStringBuilder.append("<p>");
        nStringBuilder.append("\n");
        for (Object o : cacheSet) {
            brStringBuilder
                .append(o.toString())
                .append("<br>");
            nStringBuilder
                .append(o.toString())
                .append("\n");
        }
        if (br) return brStringBuilder.toString();
        else return brStringBuilder.toString();
    }

    public String fromArrayRules(ConcurrentMap<Integer, MailRule> mailRules, boolean br) {
        mailRules.forEach((x, y) -> {
            nStringBuilder
                .append(N_S)
                .append(x)
                .append(" MAP ID  RULE:")
                .append(N_S)
                .append(y.toString());
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(" MAP ID  RULE:</h4>")
                .append(BR_S)
                .append(y.toString())
                .append("</p>");
        });
        if (br) return brStringBuilder.toString();
        else return nStringBuilder.toString();
    }

    public String fromArrayUsers(ConcurrentMap<String, String> pcUsers, boolean br) {
        pcUsers.forEach((x, y) -> {
            nStringBuilder
                .append(N_S)
                .append(x)
                .append(N_S)
                .append(y);
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(BR_S)
                .append(y)
                .append("</p>");
        });
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(ConcurrentMap<?, ?> filesFailMap, boolean br) {
        brStringBuilder.append("<p>");
        filesFailMap.forEach((x, y) -> {
            brStringBuilder.append("<br>")
                .append(x.toString())
                .append(" ")
                .append(y.toString());
            nStringBuilder.append("\n")
                .append(x.toString())
                .append(" ")
                .append(y.toString());
        });
        if (br) return brStringBuilder.toString();
        else return nStringBuilder.toString();
    }

    public String fromArray(InetAddress[] allByName, boolean b) {
        brStringBuilder.append(BR_S);
        for (InetAddress inetAddress : allByName) {
            brStringBuilder
                .append(inetAddress.toString())
                .append(BR_S);
            nStringBuilder
                .append(inetAddress.toString())
                .append(N_S);
        }
        if (b) return brStringBuilder.toString();
        else return nStringBuilder.toString();
    }

    public String fromArray(List<?> rndList, boolean b) {
        brStringBuilder.append(BR_S);
        rndList.forEach(x -> {
            brStringBuilder
                .append(x.toString())
                .append(BR_S);
            nStringBuilder
                .append(x.toString())
                .append(N_S);
        });
        if (b) return brStringBuilder.toString();
        else return nStringBuilder.toString();
    }

    public String fromArray(StackTraceElement[] y, boolean b) {
        brStringBuilder.append(BR_S);
        brStringBuilder.append(y.length)
            .append(" stack length<br>");
        nStringBuilder.append(y.length)
            .append(" stack length\n");
        for(StackTraceElement st : y){
            nStringBuilder
                .append(st.toString())
                .append(N_S);
            brStringBuilder
                .append(st.toString())
                .append(BR_S);
        }
        if(b){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }
}
