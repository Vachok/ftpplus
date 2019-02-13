package ru.vachok.networker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
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
@SuppressWarnings ("ClassWithTooManyMethods")
public class TForms {

    /**
     {@link LoggerFactory#getLogger(java.lang.String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TForms.class.getSimpleName());

    private static final String STR_LINE_CLASS = " line, class: ";

    private static final String STR_VALUE = ", value: ";

    private static final String N_STR = "\n";

    private static final String BR_STR = "<br>";

    private static final String P_STR = "<p>";

    private static final String STR_DISASTER = " occurred disaster!<br>";

    private static final String STR_METHFILE = " method.<br>File: ";

    private StringBuilder brStringBuilder = new StringBuilder();

    private StringBuilder nStringBuilder = new StringBuilder();

    public String fromArray(Properties properties) {
        InitProperties initProperties = new FileProps(ConstantsFor.APP_NAME);
        initProperties.setProps(properties);
        nStringBuilder.append(N_STR);
        properties.forEach((x, y) -> {
            String msg = x + " : " + y;
            LOGGER.info(msg);
            nStringBuilder.append(x).append(" :: ").append(y).append(N_STR);
        });
        return nStringBuilder.toString();
    }

    /**
     Преобразрвание исключений.

     @param e      {@link Exception}
     @param isHTML где показывается строка. На ВЕБ или нет.
     @return {@link #brStringBuilder} или {@link #nStringBuilder}
     */
    public String fromArray(Exception e, boolean isHTML) {
        brStringBuilder.append(P_STR);
        nStringBuilder.append(N_STR);
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            nStringBuilder
                .append("At ").append(stackTraceElement.getLineNumber()).append(" line, classname is ")
                .append(stackTraceElement.getClassName().toUpperCase()).append(" file: ").append(stackTraceElement.getFileName().toUpperCase())
                .append("\n*Method is native: ")
                .append(stackTraceElement.isNativeMethod()).append("\n")
                .append(stackTraceElement.getMethodName().toUpperCase()).append(" name of method.\n");
            brStringBuilder
                .append("At ")
                .append(stackTraceElement.getClassName()).append(" ")
                .append(stackTraceElement.getLineNumber()).append(" ").append(STR_LINE_CLASS).append(STR_DISASTER)
                .append(stackTraceElement.getMethodName()).append(STR_METHFILE)
                .append(stackTraceElement.getFileName());
        }
        if(isHTML){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromArray(File[] dirFiles) {
        for(File f : dirFiles){
            if(f.getName().contains(".jar")){
                return f.getName().replace(".jar", "");
            }
            else{
                return System.getProperties().getProperty("version");
            }
        }
        throw new UnsupportedOperationException("Хуя ты ХЕРург");
    }

    public String fromEnum(Enumeration<String> enumStrings, boolean br) {
        nStringBuilder.append(N_STR);
        brStringBuilder.append(P_STR);
        while (enumStrings.hasMoreElements()) {
            String str = enumStrings.nextElement();
            nStringBuilder.append(str).append(N_STR);
            brStringBuilder.append(str).append(BR_STR);
        }
        nStringBuilder.append(N_STR);
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
        brStringBuilder.append(P_STR);
        while (stringQueue.iterator().hasNext()) {
            brStringBuilder.append(stringQueue.poll()).append(BR_STR);
            nStringBuilder.append(stringQueue.poll()).append(N_STR);
        }
        brStringBuilder.append("</p>");
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Cookie[] cookies, boolean br) {
        brStringBuilder.append(P_STR);
        for (Cookie c : cookies) {
            brStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(BR_STR);
            nStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(N_STR);
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromADUsersList(List<ADUser> adUsers, boolean br) {
        nStringBuilder.append(N_STR);
        for (ADUser ad : adUsers) {
            brStringBuilder
                .append(ad.toStringBR());
            nStringBuilder
                .append(ad.toString())
                .append(N_STR);
        }
        nStringBuilder.append(N_STR);
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String adPCMap(List<ADComputer> adComputers, boolean br) {
        brStringBuilder.append(P_STR);
        nStringBuilder.append(N_STR);
        for (ADComputer ad : adComputers) {
            brStringBuilder
                .append(ad.toString())
                .append(BR_STR);
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
                .append(N_STR);
        }
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Set<?> cacheSet, boolean br) {
        brStringBuilder.append(P_STR);
        nStringBuilder.append(N_STR);
        for (Object o : cacheSet) {
            brStringBuilder
                .append(o.toString())
                .append(BR_STR);
            nStringBuilder
                .append(o.toString())
                .append(N_STR);
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
                .append(N_STR)
                .append(x)
                .append(" MAP ID  RULE:")
                .append(N_STR)
                .append(y.toString());
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(" MAP ID  RULE:</h4>")
                .append(BR_STR)
                .append(y.toString())
                .append("</p>");
        });
        if (br) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Throwable[] suppressed) {
        nStringBuilder.append("suppressed throwable!\n".toUpperCase());
        for(Throwable throwable : suppressed){
            nStringBuilder.append(throwable.getMessage());
        }
        return nStringBuilder.toString();
    }

    public String fromArrayUsers(ConcurrentMap<String, String> pcUsers, boolean br) {
        pcUsers.forEach((x, y) -> {
            nStringBuilder
                .append(N_STR)
                .append(x)
                .append(N_STR)
                .append(y);
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(BR_STR)
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

        brStringBuilder.append(P_STR);
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
            brStringBuilder.append(P_STR);
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(InetAddress[] allByName, boolean b) {
        brStringBuilder.append(BR_STR);
        for (InetAddress inetAddress : allByName) {
            brStringBuilder
                .append(inetAddress.toString())
                .append(BR_STR);
            nStringBuilder
                .append(inetAddress.toString())
                .append(N_STR);
        }
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(List<?> rndList, boolean b) {
        brStringBuilder.append(BR_STR);
        rndList.forEach(x -> {
            brStringBuilder
                .append(x.toString())
                .append(BR_STR);
            nStringBuilder
                .append(x.toString())
                .append(N_STR);
        });
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(StackTraceElement[] y, boolean b) {
        brStringBuilder.append(BR_STR);
        brStringBuilder.append(y.length)
            .append(" stack length<br>");
        nStringBuilder.append(y.length)
            .append(" stack length\n");
        for (StackTraceElement st : y) {
            nStringBuilder
                .append(st.toString())
                .append(N_STR);
            brStringBuilder
                .append(st.toString())
                .append(BR_STR);
        }
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Object[] objects, boolean b) {
        brStringBuilder.append(P_STR);
        for (Object o : objects) {
            brStringBuilder
                .append(o.toString())
                .append(BR_STR);
            nStringBuilder
                .append(o.toString())
                .append(N_STR);
        }
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Properties p, boolean b) {
        brStringBuilder.append(P_STR);
        p.forEach((x, y) -> {
            String str = "Property: ";
            String str1 = STR_VALUE;
            brStringBuilder
                .append(str).append(x.toString())
                .append(str1).append(y.toString()).append(BR_STR);
            nStringBuilder
                .append(str).append(x.toString())
                .append(str1).append(y.toString()).append(N_STR);
        });
        if (b) {
            return brStringBuilder.toString();
        } else {
            return nStringBuilder.toString();
        }
    }

    public static String from(Exception e) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
            .append(new Date()).append(N_STR)
            .append("Exception message: ").append(e.getMessage()).append(N_STR)
            .append("Trace: \n");
        for(StackTraceElement elem : e.getStackTrace()){
            appendNElement(stringBuilder, elem);
        }
        if(e.getSuppressed()!=null){
            for(Throwable throwable : e.getSuppressed()){
                for(StackTraceElement element : throwable.getStackTrace()){
                    appendNElement(stringBuilder, element);
                }
            }
        }
        else{
            stringBuilder.append("Suppressed is null");
        }
        return stringBuilder.toString();
    }

    private static void appendNElement(StringBuilder stringBuilder, StackTraceElement elem) {
        String strNative = "NATIVE***>>>  ";
        if(elem.isNativeMethod()){
            stringBuilder.append(strNative);
        }
        stringBuilder
            .append("Line ")
            .append(elem.getLineNumber())
            .append(" in ")
            .append(elem.getClassName()).append(".").append(elem.getMethodName())
            .append(" (").append(elem.getFileName()).append(")").append(N_STR);
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
