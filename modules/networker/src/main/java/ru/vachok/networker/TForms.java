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
import java.io.*;
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

    private StringBuilder brStringBuilder = new StringBuilder();

    private StringBuilder nStringBuilder = new StringBuilder();

    private static final String N_S = "\n";

    private static final String BR_S = "<br>";

    private final String STR_DISASTER = " occurred disaster!<br>";

    private final String STR_METHFILE = " method.<br>File: ";

    private static final String N_STR_ENTER = "\n";

    private static final String BR_STR_HTML_ENTER = "<br>";

    private static final String P_STR_HTML_PARAGRAPH = "<p>";

    public String fromArray(Map<String, String> stringStringMap) {
        List<String> list = new ArrayList<>();
        stringStringMap.forEach((x, y) -> list.add(x + "    " + y + "<br>\n"));
        Collections.sort(list);
        for(String s : list){
            brStringBuilder.append(s);
            LOGGER.info(s);
        }
        return brStringBuilder.toString();
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

    public String mapStringBoolean(Map<String, Boolean> call) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        call.forEach((x, y) -> {
            String msg = x + y;
            LOGGER.info(msg);
            brStringBuilder
                .append(x)
                .append(" - ")
                .append(y)
                .append(BR_STR_HTML_ENTER);
        });
        brStringBuilder.append("</p>");
        return brStringBuilder.toString();
    }

    public String stringObjectMapParser(Map<String, Object> stringObjectMap) {
        stringObjectMap.forEach((x, y) -> {
            nStringBuilder.append(x).append("  ").append(y.toString()).append(N_STR_ENTER);
        });
        return nStringBuilder.toString();
    }

    public String fromArray(Exception e, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for(StackTraceElement stackTraceElement : e.getStackTrace()){
            nStringBuilder
                .append("At ")
                .append(stackTraceElement
                    .getClassName())
                .append(LINE_CLASS)
                .append(stackTraceElement.getClassName())
                .append(" occurred disaster!\n")
                .append(stackTraceElement.getMethodName())
                .append(" method.\nFile: ")
                .append(stackTraceElement.getFileName());
            brStringBuilder
                .append("At ")
                .append(stackTraceElement
                    .getClassName())
                .append(LINE_CLASS)
                .append(stackTraceElement.getClassName())
                .append(STR_DISASTER)
                .append(stackTraceElement.getMethodName())
                .append(STR_METHFILE)
                .append(stackTraceElement.getFileName());
        }
        if(!br){
            return nStringBuilder.toString();
        }
        else{
            return brStringBuilder.toString();
        }
    }

    public String mapLongString(Map<Long, String> visitsMap) {
        visitsMap.forEach((x, y) -> nStringBuilder.append(x).append(" | ").append(y).append(N_STR_ENTER));
        return nStringBuilder.toString();
    }

    public String fromEnum(Enumeration<String> enumStrings, boolean br) {
        nStringBuilder.append(N_STR_ENTER);
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        while(enumStrings.hasMoreElements()){
            String str = enumStrings.nextElement();
            nStringBuilder.append(str).append(N_STR_ENTER);
            brStringBuilder.append(str).append(BR_STR_HTML_ENTER);
        }
        nStringBuilder.append(N_STR_ENTER);
        brStringBuilder.append("</p>");
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Queue<String> stringQueue) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        while(stringQueue.iterator().hasNext()){
            brStringBuilder.append(stringQueue.poll()).append(BR_STR_HTML_ENTER);
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
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        for(String s : stringList){
            brStringBuilder.append(s).append(BR_STR_HTML_ENTER);
            nStringBuilder.append(s).append(N_STR_ENTER);
        }
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String mapStrStrArr(Map<String, String[]> parameterMap, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        parameterMap.forEach((x, y) -> {
            brStringBuilder.append("<h4>").append(x).append("</h4><br>");
            int i = 1;
            for(String s : y){
                brStringBuilder.append(i++).append(")").append(s).append(BR_STR_HTML_ENTER);
                nStringBuilder.append(i++).append(")").append(s).append(N_STR_ENTER);
            }
            nStringBuilder.append(x).append(N_STR_ENTER);
            brStringBuilder.append("</p>");
        });
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Cookie[] cookies, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        for(Cookie c : cookies){
            brStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(BR_STR_HTML_ENTER);
            nStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(N_STR_ENTER);
        }
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromADUsersList(List<ADUser> adUsers, boolean br) {
        nStringBuilder.append(N_STR_ENTER);
        for(ADUser ad : adUsers){
            brStringBuilder
                .append(ad.toStringBR());
            nStringBuilder
                .append(ad.toString())
                .append(N_STR_ENTER);
        }
        nStringBuilder.append(N_STR_ENTER);
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String adPCMap(List<ADComputer> adComputers, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for(ADComputer ad : adComputers){
            brStringBuilder
                .append(ad.toString())
                .append(BR_STR_HTML_ENTER);
            nStringBuilder
                .append(ad.toString())
                .append("\n\n");
        }
        brStringBuilder.append("</p>");
        nStringBuilder.append("\n\n\n");
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String adMap(Map<ADComputer, ADUser> adComputerADUserMap) {
        adComputerADUserMap.forEach((x, y) -> {
            brStringBuilder.append(P_STR_HTML_PARAGRAPH);
            brStringBuilder
                .append(x.toString())
                .append(BR_STR_HTML_ENTER)
                .append(y.toString())
                .append("</p>");
        });
        return brStringBuilder.toString();
    }

    public String fromArray(Address[] mailAddress, boolean br) {
        for(Address address : mailAddress){
            brStringBuilder
                .append(address.toString())
                .append("br");
            nStringBuilder
                .append(address.toString())
                .append(N_STR_ENTER);
        }
        if(br){
            return brStringBuilder.toString();
        }
        else{
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

    public String fromArray(Set<?> cacheSet, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for(Object o : cacheSet){
            brStringBuilder
                .append(o.toString())
                .append(BR_STR_HTML_ENTER);
            nStringBuilder
                .append(o.toString())
                .append(N_STR_ENTER);
        }
        if(br){
            return brStringBuilder.toString();
        }
        else{
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
        if(br){
            return brStringBuilder.toString();
        }
        else{
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
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromArray(ConcurrentMap<?, ?> mapDefObj, boolean br) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        mapDefObj.forEach((x, y) -> {
            brStringBuilder.append(BR_STR_HTML_ENTER)
                .append("Key: ")
                .append(x.toString())
                .append(STR_VALUE)
                .append(y.toString());
            nStringBuilder.append(N_STR_ENTER)
                .append("Key: ")
                .append(x.toString())
                .append(STR_VALUE)
                .append(y.toString());
        });
        if(br){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromArray(InetAddress[] allByName, boolean b) {
        brStringBuilder.append(BR_S);
        for(InetAddress inetAddress : allByName){
            brStringBuilder
                .append(inetAddress.toString())
                .append(BR_S);
            nStringBuilder
                .append(inetAddress.toString())
                .append(N_S);
        }
        if(b){
            return brStringBuilder.toString();
        }
        else{
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
        if(b){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
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

    public String fromArray(Object[] objects, boolean b) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        for(Object o : objects){
            brStringBuilder
                .append(o.toString())
                .append(BR_S);
            nStringBuilder
                .append(o.toString())
                .append(N_S);
        }
        if(b){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public boolean writeArray(Set<?> set, String name) {
        try(OutputStream outputStream = new FileOutputStream(System.currentTimeMillis() + " " + name + ".set");
            PrintWriter printWriter = new PrintWriter(outputStream, true)){
            set.forEach(x -> printWriter.println(x.toString()));
            return true;
        }
        catch(IOException e){
            return false;
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
        if(b){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }

    public String fromArray(Throwable throwable, boolean b) {
        brStringBuilder.append(P_STR_HTML_PARAGRAPH);
        nStringBuilder.append(N_STR_ENTER);
        for(StackTraceElement stackTraceElement : throwable.getStackTrace()){
            nStringBuilder
                .append("At ")
                .append(stackTraceElement
                    .getClassName())
                .append(LINE_CLASS)
                .append(stackTraceElement.getClassName())
                .append(" occurred disaster!\n")
                .append(stackTraceElement.getMethodName())
                .append(" method.\nFile: ")
                .append(stackTraceElement.getFileName());
            brStringBuilder
                .append("At ")
                .append(stackTraceElement
                    .getClassName())
                .append(LINE_CLASS)
                .append(stackTraceElement.getClassName())
                .append(STR_DISASTER)
                .append(stackTraceElement.getMethodName())
                .append(STR_METHFILE)
                .append(stackTraceElement.getFileName());
        }
        if(b){
            return brStringBuilder.toString();
        }
        else{
            return nStringBuilder.toString();
        }
    }
}
