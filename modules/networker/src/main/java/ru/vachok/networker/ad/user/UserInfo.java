// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 @see UserInfoTest$$ABS */
public abstract class UserInfo implements InformationFactory {
    
    
    public static final String ADUSER = ModelAttributeNames.ADUSER;
    
    @Contract("_ -> new")
    public static @NotNull UserInfo getInstance(String type) {
        if (type == null) {
            new UnknownUser(type);
        }
        if (ADUSER.equals(type)) {
            return new LocalUserResolverDBSender();
        }
        return new ResolveUserInDataBase(type);
    }
    
    public static String writeToDB() {
        try {
            return new UserInfo.DatabaseWriter().writeAllPrefixToDB();
        }
        catch (SQLException e) {
            return e.getMessage();
        }
    }
    
    public abstract List<String> getPCLogins(String pcName, int resultsLimit);
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UserInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    static void autoResolvedUsersRecord(String pcName, String lastFileUse) {
        AppComponents.threadConfig().execByThreadConfig(()->new UserInfo.DatabaseWriter().writeAutoresolvedUserToDB(pcName, lastFileUse));
    }
    
    static void manualUsersTableRecord(String pcName, String lastFileUse) {
        new UserInfo.DatabaseWriter().manualUsersDatabaseRecord(pcName, lastFileUse);
    }
    
    private static class DatabaseWriter {
        
        
        private static final Pattern COMPILE = Pattern.compile(ConstantsFor.DBFIELD_PCUSER);
        
        private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserInfo.DatabaseWriter.class.getSimpleName());
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", UserInfo.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
                    .toString();
        }
        
        private void writeAutoresolvedUserToDB(String pcName, String lastFileUse) {
            final String sql = "insert into pcuser (pcName, userName, lastmod, stamp) values(?,?,?,?)";
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);) {
                final String sqlReplaced = COMPILE.matcher(sql).replaceAll(ConstantsFor.DBFIELD_PCUSERAUTO);
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlReplaced)) {
                    String[] split = lastFileUse.split(" ");
                    preparedStatement.setString(1, pcName);
                    if (lastFileUse.toLowerCase().contains(ModelAttributeNames.USERS)) {
                        lastFileUse = lastFileUse.split(" ")[1];
                        lastFileUse = Paths.get(lastFileUse).getFileName().toString();
                    }
                    preparedStatement.setString(2, lastFileUse);
                    preparedStatement.setString(3, UsefulUtilities.thisPC());
                    preparedStatement.setString(4, split[0]);
                    messageToUser.info(MessageFormat.format("{0}: insert into pcuser (pcName, userName, lastmod, stamp) values({1},{2},{3},{4})", preparedStatement
                            .executeUpdate(), pcName, lastFileUse, UsefulUtilities.thisPC(), split[0]));
                }
                catch (SQLException e) {
                    messageToUser.error(e.getMessage() + " see line: 109");
                }
            }
            catch (SQLException | ArrayIndexOutOfBoundsException | NullPointerException | InvalidPathException e) {
                messageToUser.error(MessageFormat.format("DatabaseWriter.writeAutoresolvedUserToDB {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
            }
        }
        
        private void manualUsersDatabaseRecord(String pcName, String userName) {
            String sql = "insert into pcuser (pcName, userName) values(?,?)";
            String msg = userName + " on pc " + pcName + " is set.";
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
                 PreparedStatement p = connection.prepareStatement(sql)) {
                p.setString(1, userName);
                p.setString(2, pcName);
                int executeUpdate = p.executeUpdate();
                NetKeeper.getPcUser().put(pcName, msg);
                messageToUser.info(msg, pcName, userName + " executeUpdate " + executeUpdate);
            }
            catch (SQLException ignore) {
                //nah
            }
        }
        
        private String writeAllPrefixToDB() throws SQLException {
            int exUpInt = 0;
            List<String> list = new ArrayList<>();
            
            try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM);
                 PreparedStatement p = connection.prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow) values (?,?,?,?)")) {
                List<String> toSort = new ArrayList<>(NetKeeper.getPcNamesForSendToDatabase());
                toSort.sort(null);
                for (String x : toSort) {
                    String pcSegment = "Я не знаю...";
                    if (x.contains("200.200")) {
                        pcSegment = "Торговый дом";
                    }
                    if (x.contains("200.201")) {
                        pcSegment = "IP телефоны";
                    }
                    if (x.contains("200.202")) {
                        pcSegment = "Техслужба";
                    }
                    if (x.contains("200.203")) {
                        pcSegment = "СКУД";
                    }
                    if (x.contains("200.204")) {
                        pcSegment = "Упаковка";
                    }
                    if (x.contains("200.205")) {
                        pcSegment = "МХВ";
                    }
                    if (x.contains("200.206")) {
                        pcSegment = "Здание склада 5";
                    }
                    if (x.contains("200.207")) {
                        pcSegment = "Сырокопоть";
                    }
                    if (x.contains("200.208")) {
                        pcSegment = "Участок убоя";
                    }
                    if (x.contains("200.209")) {
                        pcSegment = "Да ладно?";
                    }
                    if (x.contains("200.210")) {
                        pcSegment = "Мастера колб";
                    }
                    if (x.contains("200.212")) {
                        pcSegment = "Мастера деликатесов";
                    }
                    if (x.contains("200.213")) {
                        pcSegment = "2й этаж. АДМ.";
                    }
                    if (x.contains("200.214")) {
                        pcSegment = "WiFiCorp";
                    }
                    if (x.contains("200.215")) {
                        pcSegment = "WiFiFree";
                    }
                    if (x.contains("200.217")) {
                        pcSegment = "1й этаж АДМ";
                    }
                    if (x.contains("200.218")) {
                        pcSegment = "ОКК";
                    }
                    if (x.contains("192.168")) {
                        pcSegment = "Может быть в разных местах...";
                    }
                    if (x.contains("172.16.200")) {
                        pcSegment = "Open VPN авторизация - сертификат";
                    }
                    boolean onLine = false;
                    if (x.contains("true")) {
                        onLine = true;
                    }
                    String x1 = x.split(":")[0];
                    p.setString(1, x1);
                    String x2 = x.split(":")[1];
                    p.setString(2, x2.split("<")[0]);
                    p.setString(3, pcSegment);
                    p.setBoolean(4, onLine);
                    exUpInt += p.executeUpdate();
                    list.add(x1 + " " + x2 + " " + pcSegment + " " + onLine);
                }
            }
            messageToUser.warn(UserInfo.DatabaseWriter.class.getSimpleName() + ".writeDB", "executeUpdate: ", " = " + exUpInt);
            return new TForms().fromArray(list, true);
        }
    }
}
