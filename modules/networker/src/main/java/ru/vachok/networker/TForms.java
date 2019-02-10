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
 Помощник для {@link Arrays#toString()}
 <p>
 Делает похожие действия, но сразу так, как нужно для {@link ru.vachok.networker.IntoApplication}

 @since 06.09.2018 (9:33) */
public class TForms {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static final String LINE_CLASS = " line, class: ";

    private static final String STR_VALUE = ", value: ";

    private static final String N_S = "\n";

    private static final String BR_S = "<br>";

    private static final String N_STR_ENTER = "\n";

    private static final String BR_STR_HTML_ENTER = "<br>";

    private static final String P_STR_HTML_PARAGRAPH = "<p>";

    private static final String STR_DISASTER = " occurred disaster!<br>";

    private static final String STR_METHFILE = " method.<br>File: ";

    private StringBuilder brStringBuilder = new StringBuilder();

    private StringBuilder nStringBuilder = new StringBuilder();

    public static String from(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
            .append(new Date()).append(N_S)
            .append("Exception message: ").append(e.getMessage()).append(N_S)
            .append("Trace: \n");
        for (StackTraceElement elem : e.getStackTrace()) {
            appendNElement(stringBuilder, elem);
        }
        if (e.getSuppressed() != null) {
            for (Throwable throwable : e.getSuppressed()) {
                for (StackTraceElement element : throwable.getStackTrace()) {
                    appendNElement(stringBuilder, element);
                }
            }
        } else stringBuilder.append("Suppressed is null");
        return stringBuilder.toString();
    }

    private static void appendNElement(StringBuilder stringBuilder, StackTraceElement elem) {
        String strNative = "NATIVE***>>>  ";
        if (elem.isNativeMethod()) {
            stringBuilder.append(strNative);
        }
        stringBuilder
            .append("Line ")
            .append(elem.getLineNumber())
            .append(" in ")
            .append(elem.getClassName()).append(".").append(elem.getMethodName())
            .append(" (").append(elem.getFileName()).append(")").append(N_S);
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
        nStringBuilder.append(N_STR_ENTER);
        properties.forEach((x, y) -> {
            String msg = x + " : " + y;
            LOGGER.info(msg);
            nStringBuilder.append(x).append(" :: ").append(y).append(N_STR_ENTER);
        });
        return nStringBuilder.toString();
    }

    public String fromArray(Exception e, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            nStringBuilder
                .append("At *").append(stackTraceElement.getLineNumber()).append("* line, classname is *")
                .append(stackTraceElement.getClassName()).append("* occurred disaster!\n*Method is native: *")
                .append(stackTraceElement.isNativeMethod()).append("*\n")
                .append(stackTraceElement.getMethodName()).append("* method.\nFile: *")
                .append(stackTraceElement.getFileName()).append("*\n");
            brStringBuilder
                .append("At ")
                .append(stackTraceElement.getClassName()).append(" ")
                .append(stackTraceElement.getLineNumber()).append(" ").append(LINE_CLASS).append(STR_DISASTER)
                .append(stackTraceElement.getMethodName()).append(STR_METHFILE)
                .append(stackTraceElement.getFileName());
        }
        if (!br) {
            return nStringBuilder.toString();
        } else {
            return brStringBuilder.toString();
        }
    }

    public String fromEnum(Enumeration<String> enumStrings, boolean br) {
        nStringBuilder.append(N_STR_ENTER);
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        while (enumStrings.hasMoreElements()) {
            String str = enumStrings.nextElement();
            nStringBuilder.append(str).append(N_STR_ENTER);
            brStringBuilder.append(str).append(BR_STR_HTML_ENTER);
        }
        nStringBuilder.append(N_STR_ENTER);
        brStringBuilder.append("</p>");
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Queue<String> stringQueue, boolean br) {
        brStringBuilder = new StringBuilder();
        nStringBuilder = new StringBuilder();
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        while (stringQueue.iterator().hasNext()) {
            brStringBuilder.append(stringQueue.poll()).append(BR_STR_HTML_ENTER);
            nStringBuilder.append(stringQueue.poll()).append(N_S);
        }
        brStringBuilder.append("</p>");
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Cookie[] cookies, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        for (Cookie c : cookies) {
            brStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(BR_STR_HTML_ENTER);
            nStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(N_STR_ENTER);
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromADUsersList(List<ADUser> adUsers, boolean br) {
        nStringBuilder.append(N_STR_ENTER);
        for (ADUser ad : adUsers) {
            brStringBuilder
                .append(ad.toStringBR());
            nStringBuilder
                .append(ad.toString())
                .append(N_STR_ENTER);
        }
        nStringBuilder.append(N_STR_ENTER);
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String adPCMap(List<ADComputer> adComputers, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for (ADComputer ad : adComputers) {
            brStringBuilder
                .append(ad.toString())
                .append(BR_STR_HTML_ENTER);
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

    public String fromArray(Address[] mailAddress, boolean br) {
        for (Address address : mailAddress) {
            brStringBuilder
                .append(address.toString())
                .append("br");
            nStringBuilder
                .append(address.toString())
                .append(N_STR_ENTER);
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Throwable[] suppressed) {
        nStringBuilder.append("suppressed throwable!\n".toUpperCase());
        for (Throwable throwable : suppressed) {
            nStringBuilder.append(throwable.getMessage());
        }
        return nStringBuilder.toString();
    }

    public String fromArray(Set<?> cacheSet, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for (Object o : cacheSet) {
            brStringBuilder
                .append(o.toString())
                .append(BR_STR_HTML_ENTER);
            nStringBuilder
                .append(o.toString())
                .append(N_STR_ENTER);
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
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
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
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

    public String fromArray(Map<?, ?> mapDefObj, boolean br) {
        brStringBuilder = new StringBuilder();
        nStringBuilder = new StringBuilder();

        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        Set<?> keySet = mapDefObj.keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x -> list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = mapDefObj.get(keyMap).toString();
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
            nStringBuilder.append(keyMap).append(" ").append(valueMap).append("\n");
        }
        if (br) {
            brStringBuilder.append(P_STR_HTML_PARAGRAPH);
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
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
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
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
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(StackTraceElement[] y, boolean b) {
        brStringBuilder.append(BR_S);
        brStringBuilder.append(y.length)
            .append(" stack length<br>");
        nStringBuilder.append(y.length)
            .append(" stack length\n");
        for (StackTraceElement st : y) {
            nStringBuilder
                .append(st.toString())
                .append(N_S);
            brStringBuilder
                .append(st.toString())
                .append(BR_S);
        }
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Object[] objects, boolean b) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        for (Object o : objects) {
            brStringBuilder
                .append(o.toString())
                .append(BR_S);
            nStringBuilder
                .append(o.toString())
                .append(N_S);
        }
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Properties p, boolean b) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        p.forEach((x, y) -> {
            String str = "Property: ";
            String str1 = STR_VALUE;
            brStringBuilder
                .append(str).append(x.toString())
                .append(str1).append(y.toString()).append(BR_S);
            nStringBuilder
                .append(str).append(x.toString())
                .append(str1).append(y.toString()).append(N_S);
        });
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Throwable throwable, boolean b) {
        StackTraceElement[] throwableStackTrace = throwable.getStackTrace();
        if(b){
            return new TForms().fromArray(throwableStackTrace, true);
        }
        else{
            return new TForms().fromArray(throwableStackTrace, false);
        }
    }
}
