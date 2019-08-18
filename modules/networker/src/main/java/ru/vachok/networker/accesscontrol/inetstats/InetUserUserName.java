// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;


/**
 @see ru.vachok.networker.accesscontrol.inetstats.InetUserUserNameTest
 @since 17.08.2019 (15:19) */
public class InetUserUserName extends InternetUse {
    
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
    
    private String aboutWhat;
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.SEARCH_PC_IN_DB);
    
    private Map<Long, String> inetDateStampSite = new TreeMap<>();
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return getFromDB();
    }
    
    private @NotNull String getFromDB() {
        String userPC = resolveUserPC();
        informationFactory.setClassOption(userPC);
        String usage0 = InternetUse.getInetUse().getUsage0(userPC);
        String conStat = getConnectStatistics();
        
        userPC = MessageFormat.format("{2}\n<p>{0}:\n{1}", userPC, usage0, conStat);
        return userPC;
    }
    
    private void dbConnection(String userPC) {
        try (Connection connection = dataConnectTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `inetstats` WHERE `ip` LIKE ?")) {
                preparedStatement.setString(1, userPC);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        inetDateStampSite.put(resultSet.getLong("Date"), resultSet.getString("site"));
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                .format("InetUserUserName.getFromDB {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
        }
    }
    
    @Contract(pure = true)
    private @NotNull String resolveUserPC() {
        if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            try {
                return InetAddress.getByAddress(InetAddress.getByName(aboutWhat).getAddress()).toString().replaceAll("\\Q/\\E", "");
            }
            catch (UnknownHostException e) {
                return e.getMessage();
            }
        }
        else {
            return aboutWhat.trim();
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", InetUserUserName.class.getSimpleName() + "[\n", "\n]")
            .add("dataConnectTo = " + dataConnectTo)
            .add("aboutWhat = '" + aboutWhat + "'")
            .add("inetDateStampSite = " + inetDateStampSite)
            .toString();
    }
}