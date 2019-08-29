// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;


/**
 @see UserInfoTest */
public abstract class UserInfo implements InformationFactory {
    
    
    public static final String ADUSER = ModelAttributeNames.ADUSER;
    
    @Contract("_ -> new")
    public static @NotNull UserInfo getInstance(String type) {
        if (type == null) {
            return new UnknownUser(UserInfo.class.getSimpleName());
        }
        else if (ADUSER.equals(type)) {
            return new LocalUserResolver();
        }
        else {
            return new ResolveUserInDataBase(type);
        }
    }
    
    public static String writeToDB() {
        try {
            return new UserInfo.DatabaseWriter().writeAllPrefixToDB();
        }
        catch (SQLException e) {
            return e.getMessage();
        }
    }
    
    public static String autoResolvedUsersRecord(String pcName, @NotNull String lastFileUse) {
        if (!lastFileUse.contains("Unknown user")) {
            return new UserInfo.DatabaseWriter().writeAutoresolvedUserToDB(pcName, lastFileUse);
        }
        else {
            return MessageFormat.format("{0}. Unknown user. DB NOT WRITTEN", pcName);
        }
    }
    
    public abstract List<String> getPCLogins(String pcName, int resultsLimit);
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setOption(Object option);
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UserInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    private static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
        
        private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserInfo.DatabaseWriter.class.getSimpleName());
        
        private final Connection connection;
        
        private String pcName;
        
        private String userName;
        
        private DatabaseWriter() {
            Connection connection1;
            DataConnectTo dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_VELKOM);
            try {
                connection1 = dataConnectTo.getDataSource().getConnection();
            }
            catch (SQLException e) {
                messageToUser.error(e.getMessage());
                connection1 = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
            }
            connection = connection1;
        }
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", UserInfo.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
                    .toString();
        }
    
        private @NotNull String writeAutoresolvedUserToDB(String pcName, @NotNull String lastFileUse) {
            this.pcName = pcName;
            this.userName = lastFileUse;
            final String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
            StringBuilder stringBuilder = new StringBuilder();
            final String sqlReplaced = COMPILE.matcher(sql).replaceAll(ConstantsFor.DBFIELD_PCUSERAUTO);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlReplaced)) {
                stringBuilder.append(execAutoResolvedUser(preparedStatement));
            }
            catch (SQLException | ArrayIndexOutOfBoundsException | NullPointerException | InvalidPathException e) {
                stringBuilder.append(MessageFormat.format("{4}: insert into pcuser (pcName, userName, lastmod, stamp) values({0},{1},{2},{3})",
                        pcName, lastFileUse, UsefulUtilities.thisPC(), "split[0]", e.getMessage()));
            }
            return stringBuilder.toString();
        }
        
        private @NotNull String execAutoResolvedUser(@NotNull PreparedStatement preparedStatement) throws SQLException, IndexOutOfBoundsException {
            String[] split = userName.split(" ");
            preparedStatement.setString(1, pcName);
            if (userName.toLowerCase().contains(ModelAttributeNames.USERS)) {
                userName = userName.split(" ")[1];
                userName = Paths.get(userName).getFileName().toString();
            }
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, UsefulUtilities.thisPC());
            preparedStatement.setString(4, split[0]);
            String retStr = MessageFormat.format("{0}: insert into pcuser (pcName, userName, lastmod, stamp) values({1},{2},{3},{4})", preparedStatement
                    .executeUpdate(), pcName, userName, UsefulUtilities.thisPC(), split[0]);
            ((MessageLocal) messageToUser).loggerFine(retStr);
            return retStr;
        }
        
        private @NotNull String manualUsersDatabaseRecord(String pcName, String userName) {
            String sql = "insert into pcuser (pcName, userName) values(?,?)";
            String msg = userName + " on pc " + pcName + " is set.";
            int retIntExec = 0;
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
                 PreparedStatement p = connection.prepareStatement(sql)) {
                p.setString(1, userName);
                p.setString(2, pcName);
                retIntExec = p.executeUpdate();
                NetKeeper.getPcUser().put(pcName, msg);
            }
            catch (SQLException ignore) {
                //nah
            }
            return MessageFormat.format("{0} executeUpdate {1}", userName, retIntExec);
        }
        
        private @NotNull String writeAllPrefixToDB() throws SQLException {
            int exUpInt = 0;
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
                 PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
                List<String> toSort = new ArrayList<>(NetKeeper.getPcNamesForSendToDatabase());
                toSort.sort(null);
                for (String resolvedStrFromSet : toSort) {
                    exUpInt = makeVLANSegmentation(resolvedStrFromSet, p);
                }
            }
            return MessageFormat.format("Update = {0} . (insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow))", exUpInt);
        }
        
        private int makeVLANSegmentation(@NotNull String resolvedStrFromSet, PreparedStatement prStatement) throws SQLException {
            String pcSegment = "Я не знаю...";
            if (resolvedStrFromSet.contains("200.200")) {
                pcSegment = "Торговый дом";
            }
            if (resolvedStrFromSet.contains("200.201")) {
                pcSegment = "IP телефоны";
            }
            if (resolvedStrFromSet.contains("200.202")) {
                pcSegment = "Техслужба";
            }
            if (resolvedStrFromSet.contains("200.203")) {
                pcSegment = "СКУД";
            }
            if (resolvedStrFromSet.contains("200.204")) {
                pcSegment = "Упаковка";
            }
            if (resolvedStrFromSet.contains("200.205")) {
                pcSegment = "МХВ";
            }
            if (resolvedStrFromSet.contains("200.206")) {
                pcSegment = "Здание склада 5";
            }
            if (resolvedStrFromSet.contains("200.207")) {
                pcSegment = "Сырокопоть";
            }
            if (resolvedStrFromSet.contains("200.208")) {
                pcSegment = "Участок убоя";
            }
            if (resolvedStrFromSet.contains("200.209")) {
                pcSegment = "Да ладно?";
            }
            if (resolvedStrFromSet.contains("200.210")) {
                pcSegment = "Мастера колб";
            }
            if (resolvedStrFromSet.contains("200.212")) {
                pcSegment = "Мастера деликатесов";
            }
            if (resolvedStrFromSet.contains("200.213")) {
                pcSegment = "2й этаж. АДМ.";
            }
            if (resolvedStrFromSet.contains("200.214")) {
                pcSegment = "WiFiCorp";
            }
            if (resolvedStrFromSet.contains("200.215")) {
                pcSegment = "WiFiFree";
            }
            if (resolvedStrFromSet.contains("200.217")) {
                pcSegment = "1й этаж АДМ";
            }
            if (resolvedStrFromSet.contains("200.218")) {
                pcSegment = "ОКК";
            }
            if (resolvedStrFromSet.contains("192.168")) {
                pcSegment = "Может быть в разных местах...";
            }
            if (resolvedStrFromSet.contains("172.16.200")) {
                pcSegment = "Open VPN авторизация - сертификат";
            }
            boolean onLine = false;
            if (resolvedStrFromSet.contains("true")) {
                onLine = true;
            }
            String namePP = resolvedStrFromSet.split(":")[0];
            prStatement.setString(1, namePP);
            String addressPP = resolvedStrFromSet.split(":")[1];
            prStatement.setString(2, addressPP.split("<")[0]);
            prStatement.setString(3, pcSegment);
            prStatement.setBoolean(4, onLine);
            return prStatement.executeUpdate();
        }
    }
    
    static String resolveOverDB(String userName) {
        try {
            userName = userName.split("\\Q.eatmeat.\\E")[0].split("PC: ")[1];
        }
        catch (IndexOutOfBoundsException e) {
            userName = "UnknownFormatConversionException: Conversion = " + userName;
        }
        List<String> userLogins = new ResolveUserInDataBase().getUserLogins(userName, 1);
        try {
            String pcAndUser = userLogins.get(0);
            return pcAndUser.split(" : ")[0];
        }
        catch (IndexOutOfBoundsException e) {
            return new UnknownUser(userName).getInfo();
        }
    }
    
    static @NotNull String manualUsersTableRecord(String pcName, String lastFileUse) {
        return new UserInfo.DatabaseWriter().manualUsersDatabaseRecord(pcName, lastFileUse);
    }
}
