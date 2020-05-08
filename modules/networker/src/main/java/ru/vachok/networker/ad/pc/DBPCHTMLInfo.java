// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.restapi.props.InitProperties;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 @see DBPCHTMLInfoTest
 @see PCOffTest
 @since 18.08.2019 (17:41) */
class DBPCHTMLInfo implements HTMLInfo {


    private static final Pattern COMPILE = Pattern.compile(": ");

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBPCHTMLInfo.class.getSimpleName());

    private static final String NOT_FOUND = " not found";

    private final List<String> userPCName = new ArrayList<>();

    private String pcName;

    private final Map<Integer, String> freqName = new ConcurrentHashMap<>();

    protected static final String DATABASES_NOT_REGISTRED = "Not registered in both databases...";

    private StringBuilder stringBuilder;

    protected String getUserNameFromNonAutoDB() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I)
            .getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
             PreparedStatement statementPCUser = connection.prepareStatement("select * from pcuser")) {
            stringBuilder.append(firstOnlineResultsParsing(statementPCUser));
        }
        catch (SQLException | ParseException | RuntimeException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }

    @NotNull
    private String getLast20UserPCs() {
        StringBuilder retBuilder = new StringBuilder();
        final String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        if (pcName.contains(":")) {
            try {
                pcName = COMPILE.split(pcName)[1].trim();
            }
            catch (ArrayIndexOutOfBoundsException e) {
                pcName = pcName.split(":")[1].trim();
            }
        }

        try (Connection c = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, "%" + pcName + "%");
            try (ResultSet r = p.executeQuery()) {
                String headER = "<h3><center>LAST 20 USER (" + pcName + ") PCs</center></h3>";
                this.stringBuilder = new StringBuilder();
                stringBuilder.append(headER);
                while (r.next()) {
                    rNext(r);
                }

                List<String> collectedNames = userPCName.stream().distinct().collect(Collectors.toList());

                for (String nameFromDB : collectedNames) {
                    collectFreq(nameFromDB);
                }
                if (r.last()) {
                    rLast(r);
                }
                countCollection(collectedNames);
                return stringBuilder.toString();
            }
        }
        catch (SQLException | NoSuchElementException e) {
            retBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return retBuilder.toString();
    }

    /**
     @return sample: {@code <a href="/ad?do0213"><font color="red">ikudryashov - do0213. Last online: 2019-10-25 18:00:17.0</font></a>}

     @see DBPCHTMLInfoTest#testFillWebModel()
     */
    @Override
    public String fillWebModel() {
        String lastOn = lastOnline();
        lastOn = HTMLGeneration.getInstance("").setColor("red", lastOn);

        return new PageGenerationHelper().getAsLink("/ad?" + pcName, lastOn);
    }

    DBPCHTMLInfo() {
        messageToUser.warn("SET THE PC NAME!");
    }

    DBPCHTMLInfo(@NotNull String pcName) {
        if (pcName.contains(ConstantsFor.EATMEAT)) {
            pcName = pcName.split("\\Q.eatmeat.ru\\E")[0];
        }
        this.pcName = pcName;
    }

    /**
     @param attributeName {@link #pcName}
     @return Count power on , count power off

     @see DBPCHTMLInfoTest#testFillAttribute()
     */
    @Override
    public String fillAttribute(String attributeName) {
        this.pcName = attributeName;
        return countOnOffNew();
    }

    @NotNull
    private String countOnOffNew() {
        final String sql = String.format("SELECT * FROM pcuser WHERE pcName LIKE '%s%%'", pcName);
        int on = 0;
        int off = 0;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I)
            .getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                on = resultSet.getInt("On");
                off = resultSet.getInt("Off");
            }
        }
        catch (SQLException | RuntimeException e) {
            messageToUser.warn(DBPCHTMLInfo.class.getSimpleName(), "countOnOffNew", e.getMessage() + Thread.currentThread().getState().name());
        }
        return htmlOnOffCreate(on, off);
    }

    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBPCInfo{");
        sb.append("pcName='").append(pcName).append('\'');
        String sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
        sb.append(", sql='").append(sql).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @NotNull
    protected String htmlOnOffCreate(int onSize, int offSize) {
        StringBuilder stringBuilder = new StringBuilder();
        String htmlFormatOnlineTimes = MessageFormat.format(" Online = {0} times.", onSize);
        stringBuilder.append(htmlFormatOnlineTimes);
        stringBuilder.append(" Offline = ");
        stringBuilder.append(offSize);
        stringBuilder.append(" times. TOTAL: ");
        stringBuilder.append(offSize + onSize);
        return stringBuilder.toString();
    }

    @NotNull
    private String firstOnlineResultsParsing(@NotNull PreparedStatement statementPCUser) throws SQLException, ParseException {
        StringBuilder stringBuilder = new StringBuilder();
        try (ResultSet resultUser = statementPCUser.executeQuery()) {
            while (resultUser.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
                if (resultUser.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName)) {
                    stringBuilder
                        .append(resultUser.getString(ConstantsFor.DBFIELD_USERNAME)).append(". Resolved: ")
                        .append(dateFormat.parse(resultUser.getString(ConstantsFor.DB_FIELD_WHENQUERIED))).append(" ");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     @param viewWhenQueriedRS {@link ResultSet} from {@link ##lastOnline()}
     @return sample: {@code ikudryashov - do0213. Last online: 2019-10-25 18:00:17.0}

     @throws SQLException поиск осуществляется по БД
     @throws NoSuchElementException когда нечего вернуть
     */
    @NotNull
    private String lastOnlinePCResultsParsing(@NotNull ResultSet viewWhenQueriedRS) throws SQLException {
        Deque<String> rsParsedDeque = new LinkedList<>();
        while (viewWhenQueriedRS.next()) {
            if (viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_PCNAME).toLowerCase().contains(pcName.toLowerCase())) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                    .append(viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_USERNAME)).append(" - ")
                    .append(viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_PCNAME))
                    .append(". Last online: ")
                    .append(viewWhenQueriedRS.getString(ConstantsFor.DBFIELD_LASTONLINE));
                rsParsedDeque.addFirst(stringBuilder.toString());
            }
        }
        if (rsParsedDeque.isEmpty()) {
            return pcName + NOT_FOUND;
        }
        else {
            return rsParsedDeque.getLast();
        }
    }

    private String lastOnline() {
        @NotNull String result;
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM velkom.pcuser WHERE pcName LIKE ?")) {
            preparedStatement.setString(1, String.format("%s%%", pcName));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                String lastOnLineStr = lastOnlinePCResultsParsing(resultSet);
                if (!lastOnLineStr.contains(NOT_FOUND)) {
                    result = lastOnLineStr;
                }
                else {
                    result = searchInBigDB();
                }
            }
        }
        catch (SQLException | RuntimeException e) {
            result = MessageFormat.format("DBPCHTMLInfo.lastOnline: {0}", e.getMessage());
        }
        return result;
    }

    private String searchInBigDB() {
        String result = DATABASES_NOT_REGISTRED;
        Thread.currentThread().setName(this.getClass().getSimpleName());
        final String sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP + " AND AddressPP LIKE '%true' ORDER BY idRec DESC LIMIT 1";
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.STR_VELKOM + "." + ConstantsFor.DB_PCUSERAUTO)) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, String.format("%s%%", pcName));
                preparedStatement.setQueryTimeout(18);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String userName = resultSet.getString(ConstantsFor.DBFIELD_USERNAME);
                        result = MessageFormat.format("{0} : {1}. last seen at {2}", pcName, userName, new Date(resultSet.getTimestamp(ConstantsFor.DBFIELD_TIMENOW).getTime()));
                    }
                }
            }
        }
        catch (SQLException | RuntimeException e) {
            result = MessageFormat.format("DBPCHTMLInfo.searchInBigDB: {0}", e.getMessage());

        }
        messageToUser.info(this.getClass().getSimpleName(), sql.replace("?", String.format("%s%%", pcName)), result);
        return result;
    }

    @NotNull
    private String sortList(List<String> timeNow) {
        Collections.sort(timeNow);

        String str = timeNow.get(timeNow.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(pcName);
        stringBuilder.append("Last online: ");
        stringBuilder.append(str);
        stringBuilder.append(" (");
        stringBuilder.append(")<br>Actual on: ");
        stringBuilder.append(new Date(Long.parseLong(InitProperties.getTheProps().getProperty(PropertiesNames.LASTSCAN))));
        stringBuilder.append("</center></font>");

        return stringBuilder.toString();
    }

    private void rNext(@NotNull ResultSet r) throws SQLException {
        String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
        userPCName.add(pcName);
        String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " + r
                .getString(ConstantsFor.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER_CLOSE;
        stringBuilder.append(returnER);
    }

    private void collectFreq(String nameFromDB) {
        int frequency = Collections.frequency(userPCName, nameFromDB);
        stringBuilder.append(frequency).append(") ").append(nameFromDB).append("<br>");
        freqName.putIfAbsent(frequency, nameFromDB);
    }

    private void rLast(@NotNull ResultSet r) throws SQLException {
        try {
            ru.vachok.messenger.MessageToUser messageToUser = new MessageToTray(this.getClass().getSimpleName());
            messageToUser.info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsFor.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DBFIELD_USERNAME));
        }
        catch (HeadlessException e) {
            MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName())
                    .info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsFor.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DBFIELD_USERNAME));
        }
    }

    private void countCollection(List<String> collectedNames) {
        Collections.sort(collectedNames);
        Set<Integer> integers = freqName.keySet();
        String mostFreqName;
        try {
            mostFreqName = freqName.get(Collections.max(integers));
        }
        catch (RuntimeException e) {
            mostFreqName = e.getMessage();
        }
        stringBuilder.append("<br>");
        if (mostFreqName != null && !mostFreqName.isEmpty()) {
            this.pcName = mostFreqName;
        }
        stringBuilder.append(this.getLast20UserPCs());
    }

}