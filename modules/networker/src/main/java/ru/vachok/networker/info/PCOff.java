// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToTray;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
 @see ru.vachok.networker.info.DatabasePCSearcherTest
 @since 08.08.2019 (13:20) */
public class PCOff extends LocalPCInfo {
    
    
    private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    private static final Pattern COMPILE = Pattern.compile(": ");
    
    private List<String> userPCName = new ArrayList<>();
    
    private Map<Integer, String> freqName = new ConcurrentHashMap<>();
    
    private StringBuilder stringBuilder;
    
    private String pcName = PCInfo.getAboutWhat();
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    public PCOff() {
    
    }
    
    @Override
    public @NotNull String pcNameWithHTMLLink(String someMore, String pcName) {
        PCInfo.setAboutWhat(pcName);
        InetAddress byName = InetAddress.getLoopbackAddress();
        try {
            byName = InetAddress.getByName(pcName);
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("PCOff.pcNameWithHTMLLink: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return pcNameUnreachable(someMore, byName);
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        PCInfo.setAboutWhat(aboutWhat);
        return getCondition(NetScanService.isReach(aboutWhat));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabasePCSearcher{");
        sb.append('}');
        return sb.toString();
    }
    
    private @NotNull String pcNameUnreachable(String someMore, @NotNull InetAddress byName) {
        String onLines = new StringBuilder()
                .append("online ")
                .append(false)
                .append("<br>").toString();
        NetKeeper.getPcNamesSet().add(byName.getHostName() + ":" + byName.getHostAddress() + " " + onLines);
        NetKeeper.getNetworkPCs().put("<br>" + byName + " last name is " + someMore, false);
        messageToUser.warn(byName.toString(), onLines, someMore);
        return onLines + " " + someMore;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.pcName = (String) classOption;
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
        String mostFreqName = freqName.get(Collections.max(integers));
        InternetUse internetUse = new InetUserPCName();
        stringBuilder.append("<br>");
        stringBuilder.append(internetUse.getInfoAbout(mostFreqName));
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
    
}
