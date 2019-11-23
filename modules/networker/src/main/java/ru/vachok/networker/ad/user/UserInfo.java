// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.MyISAMRepair;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.NetSegments;
import ru.vachok.networker.data.synchronizer.TimeOnActualizer;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 @see UserInfoTest */
public abstract class UserInfo implements InformationFactory {
    
    
    private static final String DATABASE_DEFAULT_NAME = ConstantsFor.DB_PCUSERAUTO_FULL;
    
    @SuppressWarnings("StaticVariableOfConcreteClass") private static final UserInfo.DatabaseWriter DATABASE_WRITER = new UserInfo.DatabaseWriter();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserInfo.class.getSimpleName());
    
    @Override
    public abstract String getInfo();
    
    @Override
    public abstract void setClassOption(Object option);
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Contract("null -> new")
    public static @NotNull UserInfo getInstance(String type) {
        return type == null ? new UnknownUser(UserInfo.class.getSimpleName()) : checkType(type);
    }
    
    @Contract("null -> new")
    private static @NotNull UserInfo checkType(String type) {
        PCInfo.checkValidNameWithoutEatmeat(type);
        return ModelAttributeNames.ADUSER.equals(type) ? new LocalUserResolver() : new ResolveUserInDataBase(type);
        
    }
    
    /**
     @return written or not
     
     @see UserInfoTest#testWriteUsersToDBFromSET()
     */
    public static boolean writeUsersToDBFromSET() {
        return DATABASE_WRITER.writeAllPrefixToDB();
    }
    
    public static void autoResolvedUsersRecord(String pcName, @NotNull String lastFileUse) {
        if (!lastFileUse.contains(ConstantsFor.UNKNOWN_USER) | !lastFileUse.contains("not found")) {
            AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor().execute(()->DATABASE_WRITER.writeAutoResolveUserToDB(pcName, lastFileUse));
        }
        else {
            System.err.println(MessageFormat.format("{0}. Unknown user. DB NOT WRITTEN", pcName));
        }
    }
    
    /**
     @param pcName имя ПК
     @param isOffline состояние
     @see UserInfoTest#testRenewOffCounter()
     */
    public static void renewOffCounter(String pcName, boolean isOffline) {
        String methName = "UserInfo.renewOffCounter";
        String updateResults = DATABASE_WRITER.updTime(pcName, isOffline);
        messageToUser.warn(UserInfo.class.getSimpleName(), methName, updateResults);
    }
    
    public static @NotNull String uniqueUsersTableRecord(String pcName, String lastFileUse) {
        return DATABASE_WRITER.uniqueUserAddToDB(pcName, lastFileUse);
    }
    
    public abstract List<String> getLogins(String pcName, int resultsLimit);
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UserInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    private static class DatabaseWriter {
    
    
        private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, UserInfo.DatabaseWriter.class.getSimpleName());
        
        private String pcName;
        
        private String userName;
    
        @Contract(pure = true)
        private DatabaseWriter() {
        }
    
        @Override
        public int hashCode() {
            int result = pcName != null ? pcName.hashCode() : 0;
            result = 31 * result + (userName != null ? userName.hashCode() : 0);
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
        
            UserInfo.DatabaseWriter writer = (UserInfo.DatabaseWriter) o;
    
            return (pcName != null ? pcName.equals(writer.pcName) : writer.pcName == null) && (userName != null ? userName
                .equals(writer.userName) : writer.userName == null);
        }
        
        @Override
        public String toString() {
            return new StringJoiner(",\n", UserInfo.DatabaseWriter.class.getSimpleName() + "[\n", "\n]")
                    .toString();
        }
    
        /**
         @param pcName do0001
         @param lastFileUse 1561612688516 \\do0001.eatmeat.ru\c$\Users\estrelyaeva Thu Jun 27 08:18:08 MSK 2019 1561612688516
         */
        private void writeAutoResolveUserToDB(String pcName, @NotNull String lastFileUse) {
            this.pcName = pcName;
            this.userName = lastFileUse;
            final String sql = "insert into pcuserauto (pcName, userName, lastmod, stamp) values(?,?,?,?)";
            StringBuilder stringBuilder = new StringBuilder();
    
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(DATABASE_DEFAULT_NAME)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    stringBuilder.append(execAutoResolvedUser(preparedStatement));
                }
                messageToUser.info(this.getClass().getSimpleName(), "writeAutoresolvedUserToDB", stringBuilder.toString());
            }
            catch (SQLException | RuntimeException e) {
                stringBuilder.append(MessageFormat.format("{4}: insert into pcuserauto (pcName, userName, lastmod, stamp) values({0},{1},{2},{3})",
                        pcName, lastFileUse, UsefulUtilities.thisPC(), "split[0]", e.getMessage()));
                messageToUser.error("DatabaseWriter.writeAutoResolveUserToDB", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
            }
        }
    
        /**
         @param preparedStatement statement from {@link #writeAutoResolveUserToDB(java.lang.String, java.lang.String)}
         @return preparedStatement.executeUpdate(), preparedStatement.toString()
     
         @throws SQLException statement
         @throws IndexOutOfBoundsException String[] split = userName.split(" ");
         */
        private @NotNull String execAutoResolvedUser(@NotNull PreparedStatement preparedStatement) throws SQLException {
            String[] split = userName.split(" ");
            preparedStatement.setString(1, pcName);
            if (userName.toLowerCase().contains(ModelAttributeNames.USERS)) {
                userName = userName.split(" ")[1];
                userName = Paths.get(userName).getFileName().toString();
            }
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, UsefulUtilities.thisPC());
            preparedStatement.setString(4, split[0]);
            return MessageFormat.format("{0}: {1}", preparedStatement.executeUpdate(), preparedStatement.toString());
        }
    
        private @NotNull String uniqueUserAddToDB(String pcName, String userName) {
            this.pcName = pcName;
            String sql = "insert into pcuser (pcName, userName) values(?,?)";
            String msg = MessageFormat.format("{0} on {1} is set.", userName, pcName);
            int retIntExec;
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
                 PreparedStatement p = connection.prepareStatement(sql)) {
                p.setString(1, pcName);
                p.setString(2, userName);
                retIntExec = p.executeUpdate();
                NetKeeper.getPcUser().put(retIntExec + " executeUpdate", msg);
                return MessageFormat.format("Setting {0} to {1} executeUpdate: {2}", userName, pcName, retIntExec);
            }
            catch (SQLException e) {
                return MessageFormat.format("{0} already exists in database {1} on {2}", userName, ConstantsFor.DB_VELKOMPCUSER, pcName);
            }
        }
    
        private @NotNull String updTime(String pcName, boolean isOffline) {
            this.pcName = pcName;
            AppConfigurationLocal.getInstance().execute(this::countWorkTime, 10);
            StringBuilder stringBuilder = new StringBuilder();
            String sql;
            String sqlOn = String.format("UPDATE `velkom`.`pcuser` SET `lastOnLine`='%s', `On`= `On`+1, `Total`= `On`+`Off` WHERE `pcName` like ?", Timestamp
                    .valueOf(LocalDateTime.now()));
            String sqlOff = "UPDATE `velkom`.`pcuser` SET `Off`= `Off`+1, `Total`= `On`+`Off` WHERE `pcName` like ?";
            if (isOffline) {
                sql = sqlOff;
                stringBuilder.append("ISOFFLINE\n");
            }
            else {
                stringBuilder.append("ONLINE (ELSE)\n");
                long nowMinusDelay = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY);
                System.out.println("new Date(nowMinusDelay) = " + new Date(nowMinusDelay));
                System.out.println("START_STAMP = " + new Date(ConstantsFor.START_STAMP));
                boolean startSmallerDelay = ConstantsFor.START_STAMP >= nowMinusDelay;
    
                if (startSmallerDelay) {
                    AppConfigurationLocal.getInstance().execute(new TimeOnActualizer(pcName));
                    sql = sqlOn;
                }
                else if (wasOffline()) {
                    stringBuilder.append("*WAS OFFLINE*: ");
                    sql = String
                            .format("UPDATE `velkom`.`pcuser` SET `lastOnLine`='%s', `timeon`='%s', `On`= `On`+1, `Total`= `On`+`Off` WHERE `pcName` like ?", Timestamp
                                    .valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().minus(1, ChronoUnit.MINUTES)));
                    
                }
                else {
                    sql = sqlOn;
                }
            }
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, String.format("%s%%", pcName));
                    stringBuilder.append(preparedStatement.toString()).append(" : ");
                    stringBuilder.append(preparedStatement.executeUpdate()).append("\n");
                }
            }
            catch (SQLException | RuntimeException e) {
                stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
            }
            return stringBuilder.toString();
        }
    
        private void countWorkTime() {
            final String sql = "select * from pcuser where pcname like ?";
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, String.format("%s%%", pcName));
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            countTime(connection, resultSet);
                        }
                    }
                }
            }
            catch (SQLException e) {
                messageToUser.warn(UserInfo.DatabaseWriter.class.getSimpleName(), "countWorkTime", e.getMessage() + Thread.currentThread().getState().name());
            }
        }
        
        private boolean wasOffline() {
            @SuppressWarnings("DuplicateStringLiteralInspection") final String sql = String.format("SELECT lastonline FROM pcuser WHERE pcname LIKE '%s%%'", pcName);
            boolean retBool = false;
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMPCUSER);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp(ConstantsFor.DBFIELD_LASTONLINE);
                    long defaultStampMinutes = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(Year.now().getValue() - 1984);
                    retBool = timestamp.getTime() < defaultStampMinutes;
                }
            }
            catch (SQLException e) {
                messageToUser.warn(UserInfo.DatabaseWriter.class.getSimpleName(), "wasOffline", e.getMessage() + Thread.currentThread().getState().name());
            }
            return retBool;
        }
        
        private void countTime(@NotNull Connection connection, @NotNull ResultSet resultSet) throws SQLException {
            Timestamp timeOn = resultSet.getTimestamp(ConstantsFor.DBFIELD_TIMEON);
            Timestamp lastOn = resultSet.getTimestamp(ConstantsFor.DBFIELD_LASTONLINE);
            long timeSpend = lastOn.getTime() - timeOn.getTime();
            timeSpend = TimeUnit.MILLISECONDS.toMinutes(timeSpend);
            final String sql = String.format("UPDATE pcuser SET spendtime=%d WHERE pcname LIKE '%s%%'", (int) timeSpend, pcName);
            try (PreparedStatement setTimeSpend = connection.prepareStatement(sql)) {
                setTimeSpend.executeUpdate();
            }
        }
        
        private boolean writeAllPrefixToDB() {
            int exUpInt = 0;
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_VELKOMVELKOMPC)) {
                connection.setAutoCommit(false);
                connection.setSavepoint();
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                try (PreparedStatement prepStatement = connection
                        .prepareStatement("insert into  velkompc (NamePP, AddressPP, SegmentPP , OnlineNow, instr, userName) values (?,?,?,?,?,?)")) {
                    List<String> toSort = new ArrayList<>(NetKeeper.getPcNamesForSendToDatabase());
                    toSort.sort(null);
                    for (String resolvedStrFromSet : toSort) {
                        exUpInt = makeVLANSegmentation(resolvedStrFromSet, prepStatement);
                        messageToUser.info(MessageFormat.format("Update = {0}: {1})", exUpInt, prepStatement.toString()));
                    }
                    connection.commit();
                }
                catch (SQLException e) {
                    connection.rollback();
                }
            }
            catch (SQLException | RuntimeException e) {
                bigDatabaseException(e);
                exUpInt = -666;
            }
            return exUpInt > 0;
        }
    
        /**
         @param resolvedStrFromSet строка {@link NetKeeper#getPcNamesForSendToDatabase()} {@code do0009:10.200.213.132 online true}
         @param prStatement {@link PreparedStatement}
         @return {@link PreparedStatement#executeUpdate()}
     
         @throws SQLException insert into  velkompc
         */
        @SuppressWarnings("OverlyLongMethod")
        private int makeVLANSegmentation(@NotNull String resolvedStrFromSet, PreparedStatement prStatement) throws SQLException {
            String pcSegment;
            //noinspection IfStatementWithTooManyBranches
            if (resolvedStrFromSet.contains("200.200")) {
                pcSegment = "Торговый дом";
            }
            else if (resolvedStrFromSet.contains("200.201")) {
                pcSegment = NetSegments.IPPHONE;
            }
            else if (resolvedStrFromSet.contains("200.202")) {
                pcSegment = "Техслужба";
            }
            else if (resolvedStrFromSet.contains("200.203")) {
                pcSegment = "СКУД";
            }
            else if (resolvedStrFromSet.contains("200.204")) {
                pcSegment = "Упаковка";
            }
            else if (resolvedStrFromSet.contains("200.205")) {
                pcSegment = "МХВ";
            }
            else if (resolvedStrFromSet.contains("200.206")) {
                pcSegment = "Здание склада 5";
            }
            else if (resolvedStrFromSet.contains("200.207")) {
                pcSegment = "Сырокопоть";
            }
            else if (resolvedStrFromSet.contains("200.208")) {
                pcSegment = "Участок убоя";
            }
            else if (resolvedStrFromSet.contains("200.209")) {
                pcSegment = "Да ладно?";
            }
            else if (resolvedStrFromSet.contains("200.210")) {
                pcSegment = "Мастера колб";
            }
            else if (resolvedStrFromSet.contains("200.212")) {
                pcSegment = "Мастера деликатесов";
            }
            else if (resolvedStrFromSet.contains("200.213")) {
                pcSegment = "2й этаж. АДМ.";
            }
            else if (resolvedStrFromSet.contains("200.214")) {
                pcSegment = "WiFiCorp";
            }
            else if (resolvedStrFromSet.contains("200.215")) {
                pcSegment = "WiFiFree";
            }
            else if (resolvedStrFromSet.contains("200.217")) {
                pcSegment = "1й этаж АДМ";
            }
            else if (resolvedStrFromSet.contains("200.218")) {
                pcSegment = "ОКК";
            }
            else if (resolvedStrFromSet.contains("192.168")) {
                pcSegment = "Может быть в разных местах...";
            }
            else if (resolvedStrFromSet.contains("172.16.200")) {
                pcSegment = "Open VPN авторизация - сертификат";
            }
            else {
                pcSegment = "Новый?";
            }
            boolean onLine = false;
            if (resolvedStrFromSet.contains("true")) {
                onLine = true;
            }
            String namePP = resolvedStrFromSet.split(":")[0];
            String addressPP = resolvedStrFromSet.split(":")[1];
            String resolveUser = addressPP.split("<")[1];
            
            prStatement.setString(1, namePP);
            prStatement.setString(2, addressPP.split("<")[0]);
            prStatement.setString(3, pcSegment);
            prStatement.setBoolean(4, onLine);
            prStatement.setString(5, UsefulUtilities.thisPC());
            prStatement.setString(6, resolveUser);
    
            messageToUser.info(this.getClass().getSimpleName(), "executing statement", MessageFormat
                    .format("{0} namePP; {1} addressPP; {2} pcSegment; {3} onLine; {4} resolveUser", namePP, addressPP, pcSegment, onLine, resolveUser));
            return prStatement.executeUpdate();
        }
    
        private void bigDatabaseException(Exception e) {
            String methName = "writeAllPrefixToDB";
            File appendTo = new File(ConstantsFor.DB_VELKOMVELKOMPC);
            if (e instanceof SQLException) {
                messageToUser.error(getClass().getSimpleName(), methName, FileSystemWorker.error(getClass().getSimpleName() + ".writeAllPrefixToDB", e));
                if (e.getMessage().contains(ConstantsFor.MARKEDASCRASHED)) {
                    String repairTable = new MyISAMRepair().repairTable("REPAIR TABLE " + ConstantsFor.DB_VELKOMVELKOMPC);
                    FileSystemWorker.appendObjectToFile(appendTo, repairTable);
                }
            }
            FileSystemWorker.appendObjectToFile(appendTo, e.getClass().getSimpleName());
            messageToUser.warn(
                this.getClass().getSimpleName(),
                methName,
                    FileSystemWorker.appendObjectToFile(appendTo, AbstractForms.fromArray(NetKeeper.getPcNamesForSendToDatabase())));
        
        }
    }
    
    static String resolvePCUserOverDB(String pcOrUser) {
        String result;
        try {
            List<String> userLogins = new ArrayList<>(new ResolveUserInDataBase().getLogins(pcOrUser, 1));
            result = userLogins.get(0);
        }
        catch (IndexOutOfBoundsException e) {
            result = new ResolveUserInDataBase(pcOrUser).getLoginFromStaticDB(pcOrUser);
        }
        return result;
    }
}
