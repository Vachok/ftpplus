// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.PCUserNameHTMLResolver;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.net.NetKeeper;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.Executors;


/**
 Проверки из классов.
 <p>
 Пинги, и тп
 
 @since 31.01.2019 (0:20) */
public class PCOn extends PCInfo {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(PCOn.class.getSimpleName());
    
    private static DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private boolean isOnline = true;
    
    private @NotNull String sql;
    
    private String pcName;
    
    public PCOn(@NotNull String pcName) {
        this.pcName = pcName;
        this.sql = "select * from velkompc where NamePP like ?";
    }
    
    @Override
    public String getUserByPC(String pcName) {
        throw new TODOException("20.08.2019 (16:11)");
    }
    
    @Override
    public String getInfo() {
        return getInfoAbout(pcName);
    }
    
    @Override
    public String getPCbyUser(String userName) {
        throw new TODOException("20.08.2019 (16:12)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCOn.class.getSimpleName() + "[\n", "\n]")
                .add("isOnline = " + isOnline)
                .add("sql = '" + sql + "'")
                .add("pcName = '" + pcName + "'")
                .toString();
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        ThreadConfig.thrNameSet(pcName.substring(0, 4));
        StringBuilder stringBuilder = new StringBuilder();
        String strHTMLLink = pcNameWithHTMLLink(DBPCInfo.getLocalInfo(pcName).getInfo(), pcName);
        stringBuilder.append(strHTMLLink);
        stringBuilder.append(lastUserResolved());
        return stringBuilder.toString();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.isOnline = (boolean) classOption;
    }
    
    private @NotNull String pcNameWithHTMLLink(String someMore, @NotNull String pcName) {
        String lastUser = lastUserResolved();
    
        StringBuilder builder = new StringBuilder();
        builder.append("<br><b>");
        builder.append(new PageGenerationHelper().getAsLink("/ad?" + pcName.split(".eatm")[0], pcName));
        builder.append(lastUser);
        builder.append("</b>    ");
        builder.append(someMore);
        builder.append(". ");
        
        String printStr = builder.toString();
        String pcOnline = "online is true<br>";
        
        NetKeeper.getNetworkPCs().put(printStr, true);
        NetKeeper.getPcNamesSet().add(pcName + ":" + pcName + pcOnline);
        
        messageToUser.info(pcName, pcOnline, someMore);
        
        int onlinePC = Integer.parseInt((LOCAL_PROPS.getProperty(PropertiesNames.PR_ONLINEPC, "0")));
        onlinePC += 1;
        
        LOCAL_PROPS.setProperty(PropertiesNames.PR_ONLINEPC, String.valueOf(onlinePC));
        return builder.toString();
    }
    
    private @NotNull String lastUserResolved() {
        StringBuilder stringBuilder = new StringBuilder();
        
        final String sqlLoc = "SELECT * FROM `pcuser` WHERE `pcName` LIKE ?";
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement p = connection.prepareStatement(sqlLoc)) {
            p.setString(1, new StringBuilder().append("%").append(pcName).append("%").toString());
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    if (r.last()) {
                        stringBuilder.append(r.getString(ConstantsFor.DB_FIELD_USER));
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage());
        }
        return new PageGenerationHelper().setColor("white", stringBuilder.toString());
    }
    
    private @NotNull String countOnOff() {
        HTMLInfo htmlInfo = new PCUserNameHTMLResolver(pcName);
        Runnable rPCResolver = ()->htmlInfo.fillAttribute(pcName);
        
        Collection<Integer> onLine = new ArrayList<>();
        Collection<Integer> offLine = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).execute(rPCResolver);
    
        try (Connection connection = dataConnectTo.getDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pcName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int onlineNow = resultSet.getInt(ConstantsNet.ONLINE_NOW);
                    if (onlineNow == 1) {
                        onLine.add(onlineNow);
                    }
                    if (onlineNow == 0) {
                        offLine.add(onlineNow);
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.errorAlert(this.getClass().getSimpleName(), "countOnOff", e.getMessage());
            stringBuilder.append(e.getMessage());
        }
        catch (NullPointerException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder
                .append(offLine.size())
                .append(" offline times and ")
                .append(onLine.size())
                .append(" online times.").toString();
    }
}