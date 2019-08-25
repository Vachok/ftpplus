// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsNet;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;

import java.awt.*;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 @see ru.vachok.networker.info.PCOffTest
 @since 08.08.2019 (13:20) */
public class PCOff extends PCInfo {
    
    
    private static final Pattern COMPILE = Pattern.compile(": ");
    
    private List<String> userPCName = new ArrayList<>();
    
    private Map<Integer, String> freqName = new ConcurrentHashMap<>();
    
    private StringBuilder stringBuilder;
    
    private String pcName;
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public PCOff(String aboutWhat) {
        this.pcName = aboutWhat;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        PCOff off = (PCOff) o;
        
        if (!userPCName.equals(off.userPCName)) {
            return false;
        }
        if (!freqName.equals(off.freqName)) {
            return false;
        }
        if (pcName != null ? !pcName.equals(off.pcName) : off.pcName != null) {
            return false;
        }
        return dataConnectTo.equals(off.dataConnectTo);
    }
    
    @Override
    public int hashCode() {
        int result = userPCName.hashCode();
        result = 31 * result + freqName.hashCode();
        result = 31 * result + (pcName != null ? pcName.hashCode() : 0);
        result = 31 * result + dataConnectTo.hashCode();
        return result;
    }
    
    @Override
    public String fillWebModel() {
        throw new TODOException("ru.vachok.networker.ad.pc.PCOff.fillWebModel created 23.08.2019 (10:53)");
    }
    
    @Override
    public String fillAttribute(String attributeName) {
        throw new TODOException("ru.vachok.networker.ad.pc.PCOff.fillAttribute created 23.08.2019 (10:53)");
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        return new DBPCInfo(aboutWhat).userNameFromDBWhenPCIsOff();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "Please - set the pcName!\n" + this.toString();
        }
        this.pcName = PCInfo.checkValidName(pcName);
        String fromDBWhenOff = new DBPCInfo(pcName).userNameFromDBWhenPCIsOff();
        return MessageFormat.format("USER: {0}, {1}", fromDBWhenOff, pcNameWithHTMLLink(fromDBWhenOff, pcName));
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCOff.class.getSimpleName() + "[\n", "\n]")
            .add("userPCName = " + userPCName)
            .add("freqName = " + freqName)
            .add("stringBuilder = " + stringBuilder)
            .add("pcName = '" + pcName + "'")
            .add("dataConnectTo = " + dataConnectTo)
            .toString();
    }
    
    private @NotNull String pcNameWithHTMLLink(String someMore, String pcName) {
        NameOrIPChecker nameIpChecker = new NameOrIPChecker(pcName);
        if (nameIpChecker.isLocalAddress()) {
            return pcNameUnreachable(someMore, nameIpChecker.resolveInetAddress());
        }
        else {
            return "Not valid address";
        }
    }
    
    private @NotNull String pcNameUnreachable(String onOffCounter, @NotNull InetAddress byName) {
        String onLines = new StringBuilder()
            .append("online ")
            .append(false)
            .append("<br>").toString();
        NetKeeper.getPcNamesForSendToDatabase().add(byName.getHostName() + ":" + byName.getHostAddress() + " " + onLines);
        NetKeeper.getScannedUsersPC().put("<br>" + byName + " last name is " + onOffCounter, false);
        messageToUser.warn(byName.toString(), onLines, onOffCounter);
        return onLines + " " + onOffCounter;
    }
    
    private @NotNull String getLast20UserPCs() {
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
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
    
    private void rNext(@NotNull ResultSet r) throws SQLException {
        String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
        userPCName.add(pcName);
        String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " + r
            .getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER_CLOSE;
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
            messageToUser.info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
        }
        catch (HeadlessException e) {
            new MessageLocal(this.getClass().getSimpleName())
                .info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
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
        stringBuilder.append(getLast20UserPCs());
    }
}