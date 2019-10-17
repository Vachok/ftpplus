package ru.vachok.networker.ad.inet;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 @see AccessLogHTMLMakerTest
 @since 27.08.2019 (11:28) */
public class AccessLogHTMLMaker extends InternetUse implements HTMLInfo {
    
    
    private String aboutWhat;
    
    private Map<String, String> siteResponseMap = new ConcurrentHashMap<>();
    
    private List<String> toWriteDenied = new ArrayList<>();
    
    private List<String> toWriteAllowed = new ArrayList<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        return fillAttribute(aboutWhat);
    }
    
    @Override
    public String getInfo() {
        return aboutWhat != null ? fillWebModel() : "Set classOption! " + this.toString();
    }
    
    @Override
    public String fillWebModel() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            aboutWhat = new NameOrIPChecker(aboutWhat).resolveInetAddress().getHostAddress();
        }
        catch (RuntimeException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        stringBuilder.append("<details><summary>Посмотреть сайты (BETA)</summary>");
        stringBuilder.append("Показаны только <b>уникальные</b> сайты<br>");
    
        try (Connection connection = DataConnectTo.getDefaultI().getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(ConstantsFor.SQL_SELECT_DIST)) {
                preparedStatement.setString(1, aboutWhat);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        resultSetWhileNext(resultSet);
                    }
                    if (resultSet.last()) {
                        LocalDate localDate = LocalDateTime.ofEpochSecond((resultSet.getLong("Date") / 1000), 0, ZoneOffset.ofHours(3)).toLocalDate();
                        int compareTo = LocalDate.now().compareTo(localDate);
                        stringBuilder.append("<b>Статистика за ").append(compareTo).append(" дней</b><br>");
                    }
                    if (resultSet.wasNull()) {
                        stringBuilder.append("No usage detected");
                    }
                    stringBuilder.append(makeReadableResults());
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private void resultSetWhileNext(@NotNull ResultSet r) throws SQLException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss z, E");
        String date = dateFormat.format(new Date(r.getLong("Date")));
        
        String siteString = r.getString("site");
        try {
            String[] splittedSiteNoHTTP = siteString.split("//");
            siteString = splittedSiteNoHTTP[1].split("/")[0];
            siteString = splittedSiteNoHTTP[0] + "//" + siteString;
        }
        catch (ArrayIndexOutOfBoundsException ignored) {
            //
        }
        String responseString = r.getString(ConstantsFor.DBCOL_RESPONSE) + " " + r.getString(ConstantsFor.DBFIELD_METHOD);
        siteResponseMap.putIfAbsent(siteString,
            MessageFormat.format("{0} when: {1} ({2} bytes, {3} seconds)", responseString, date, r.getInt(ConstantsFor.DBCOL_BYTES), r.getInt("inte")));
    }
    
    private @NotNull String makeReadableResults() {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keySet = siteResponseMap.keySet();
        keySet.stream().distinct().forEachOrdered(this::parseResultSetMap);
        stringBuilder.append("DENIED SITES: <br>");
        
        Collections.sort(toWriteAllowed);
        Collections.sort(toWriteDenied);
        
        Collections.reverse(toWriteDenied);
        Collections.reverse(toWriteAllowed);
        
        toWriteDenied.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append("<p>ALLOWED SITES: <br>");
        toWriteAllowed.forEach(x->stringBuilder.append(x).append("<br>"));
        stringBuilder.append(ConstantsFor.HTMLTAG_DETAILSCLOSE);
        return stringBuilder.toString();
    }
    
    private void parseResultSetMap(String distinctKey) {
        HTMLGeneration htmlGeneration = new PageGenerationHelper();
        String valueX = siteResponseMap.get(distinctKey);
        if (!distinctKey.startsWith("http") && distinctKey.contains(":")) {
            distinctKey = distinctKey.split(":")[0];
        }
        try {
            valueX = valueX.split("when: ")[1] + ") " + valueX.split("when: ")[0];
        }
        catch (ArrayIndexOutOfBoundsException ignore) {
            //
        }
        if (!distinctKey.startsWith("http")) {
            distinctKey = ConstantsFor.HTTPS + distinctKey;
        }
        
        if (valueX.contains("/5") | valueX.contains("/6")) {
            String errorSite = htmlGeneration.setColor("#fca503", valueX + " ||| " + htmlGeneration.getAsLink(distinctKey.trim(), distinctKey));
            toWriteAllowed.add(errorSite + " error!");
        }
        else if (valueX.contains("/4")) {
            String strDeny = htmlGeneration.setColor("red", valueX + " ||| " + htmlGeneration.getAsLink(distinctKey.trim(), distinctKey));
            toWriteDenied.add(strDeny);
        }
        else {
            String strAllow = htmlGeneration.setColor(ConstantsFor.GREEN, valueX + " ||| " + htmlGeneration.getAsLink(distinctKey.trim(), distinctKey));
            if (valueX.toLowerCase().contains("post") | valueX.toLowerCase().contains("connect") | valueX.toLowerCase().contains("tunnel")) {
                strAllow = strAllow.replace(ConstantsFor.GREEN, ConstantsFor.YELLOW);
            }
            toWriteAllowed.add(strAllow);
        }
        FileSystemWorker.writeFile("denied.log", toWriteDenied.stream().sorted());
        FileSystemWorker.writeFile("allowed.log", toWriteAllowed.stream().sorted());
    }
    
    @Override
    public String fillAttribute(String attributeName) {
        this.aboutWhat = attributeName;
        InformationFactory logUSER = new AccessLogUSER();
        logUSER.setClassOption(attributeName);
        return logUSER.getInfoAbout(attributeName);
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.aboutWhat = (String) classOption;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AccessLogHTMLMaker{");
        sb.append("toWriteDenied=").append(toWriteDenied);
        sb.append(", toWriteAllowed=").append(toWriteAllowed);
        sb.append(", siteResponseMap=").append(siteResponseMap);
        sb.append(", aboutWhat='").append(aboutWhat).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public int hashCode() {
        int result = aboutWhat != null ? aboutWhat.hashCode() : 0;
        result = 31 * result + siteResponseMap.hashCode();
        result = 31 * result + toWriteDenied.hashCode();
        result = 31 * result + toWriteAllowed.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        AccessLogHTMLMaker maker = (AccessLogHTMLMaker) o;
        
        if (aboutWhat != null ? !aboutWhat.equals(maker.aboutWhat) : maker.aboutWhat != null) {
            return false;
        }
        if (!siteResponseMap.equals(maker.siteResponseMap)) {
            return false;
        }
        if (!toWriteDenied.equals(maker.toWriteDenied)) {
            return false;
        }
        return toWriteAllowed.equals(maker.toWriteAllowed);
    }
}
