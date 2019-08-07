// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 @see ru.vachok.networker.restapi.props.DBPropsCallableTest */
@SuppressWarnings("DuplicateStringLiteralInspection")
public class DBPropsCallable implements Callable<Properties>, InitProperties {
    
    private final MessageToUser messageToUser = new MessageLocal(DBPropsCallable.class.getSimpleName());
    
    /**
     Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();
    
    private String propsDBID = ConstantsFor.class.getSimpleName();
    
    private final Properties retProps = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
    
    private String callerStack = "not set";
    
    private DataConnectTo dataConnectTo;
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    private MysqlDataSource mysqlDataSource;
    
    public DBPropsCallable() {
        this.dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        this.mysqlDataSource = dataConnectTo.getDataSource();
        this.mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
    
        mysqlDataSource.setUser(retProps.getProperty(PropertiesNames.PR_DBUSER, "nouser"));
        mysqlDataSource.setPassword(retProps.getProperty(PropertiesNames.PR_DBPASS, "nopass"));
        Thread.currentThread().setName("DBPr()");
    }
    
    public DBPropsCallable(String appName, String propsIDClass) {
        this.dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        this.mysqlDataSource = dataConnectTo.getDataSource();
        this.propsDBID = propsIDClass;
    
        mysqlDataSource.setUser(AppComponents.getUserPref().get(PropertiesNames.PR_DBUSER, "nouser"));
        mysqlDataSource.setPassword(AppComponents.getUserPref().get(PropertiesNames.PR_DBPASS, "nopass"));
        Thread.currentThread().setName("DBPr(ID)");
    }
    
    protected DBPropsCallable(@NotNull Properties toUpdate) {
        this.dataConnectTo = new RegRuMysqlLoc(ConstantsFor.DBBASENAME_U0466446_TESTING);
        this.mysqlDataSource = dataConnectTo.getDataSource();
        this.propsToSave = toUpdate;
    
        mysqlDataSource.setUser(toUpdate.getProperty(PropertiesNames.PR_DBUSER));
        mysqlDataSource.setPassword(toUpdate.getProperty(PropertiesNames.PR_DBPASS));
        Thread.currentThread().setName("DBPr(Pr)");
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        return dataConnectTo.getDataSource();
    }
    
    @Override
    public Properties getProps() {
        final String sql = "SELECT * FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        try (Connection connection = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_PROPERTIES)) {
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, propsDBID);
                try (ResultSet r = pStatement.executeQuery()) {
                    while (r.next()) {
                        retProps.setProperty(r.getString("property"), r.getString("valueofproperty"));
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DBPropsCallable.getProps: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return retProps;
    }
    
    @Override
    public boolean setProps(Properties properties) {
        DBPropsCallable.LocalPropertiesFinder localPropsFinder = new DBPropsCallable.LocalPropertiesFinder();
        this.propsToSave = properties;
        sqlUserPassSet();
        retBool.set(localPropsFinder.upProps());
        messageToUser.info(MessageFormat.format("Updating database {0} is {1}", mysqlDataSource.getURL(), retBool.get()));
        return retBool.get();
    }
    
    private void sqlUserPassSet() {
        Properties userPass = new Properties();
        try (InputStream inputStream = getClass().getResourceAsStream("/static/msqldata.properties")) {
            userPass.load(inputStream);
            mysqlDataSource.setUser(userPass.getProperty(PropertiesNames.PR_DBUSER));
            mysqlDataSource.setPassword(userPass.getProperty(PropertiesNames.PR_DBPASS));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("DBPropsCallable.setProps threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
    }
    
    @Override
    public Properties call() {
        DBPropsCallable.LocalPropertiesFinder localPropertiesFinder = new DBPropsCallable.LocalPropertiesFinder();
        synchronized(retProps) {
            this.callerStack = new TForms().fromArray(Thread.currentThread().getStackTrace());
            return localPropertiesFinder.findRightProps();
        }
    }
    
    @Override
    public boolean delProps() {
        final String sql = "DELETE FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        try (Connection c = dataConnectTo.getDefaultConnection(ConstantsFor.DBBASENAME_U0466446_PROPERTIES)) {
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, propsDBID);
                int update = p.executeUpdate();
                return update > 0;
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DBPropsCallable.delProps: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", DBPropsCallable.class.getSimpleName() + "[\n", "\n]")
            .add("callerStack = '" + callerStack + "'")
            .add("retProps = " + retProps.size())
            .add("dataConnectTo = " + dataConnectTo.toString())
            .add("retBool = " + retBool)
            .add("mysqlDataSource = " + mysqlDataSource.getURL())
            .toString();
    }
    
    protected class LocalPropertiesFinder extends DBPropsCallable {
        
        private boolean upProps() {
            final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid, stack) values (?,?,?,?)";
            retBool.set(false);
            callerStack = new TForms().fromArray(Thread.currentThread().getStackTrace());
            FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName()), "UPDATE:\n" + callerStack);
            try (Connection c = mysqlDataSource.getConnection()) {
                int executeUpdateInt = 0;
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    retBool.set(delProps());
                    propsToSave.setProperty("thispc", UsefulUtilites.thisPC());
                    for (Map.Entry<Object, Object> entry : propsToSave.entrySet()) {
                        Object x = entry.getKey();
                        Object y = entry.getValue();
                        preparedStatement.setString(1, x.toString());
                        preparedStatement.setString(2, y.toString());
                        preparedStatement.setString(3, propsDBID);
                        preparedStatement.setString(4, callerStack);
                        executeUpdateInt += preparedStatement.executeUpdate();
                    }
                    retBool.set(executeUpdateInt > 0);
                }
            }
            catch (SQLException e) {
                if (!(e instanceof MySQLIntegrityConstraintViolationException)) {
                    messageToUser.error(MessageFormat
                        .format("DBPropsCallable.upProps\n{0}: {1}\nParameters: []\nReturn: boolean\nStack:\n{2}", e.getClass().getTypeName(), e
                            .getMessage(), new TForms()
                            .fromArray(e)));
                    retBool.set(false);
                    tryWithLibsInit();
                }
            }
            miniLogger.add(callerStack);
            FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
            return retBool.get();
        }
        
        private boolean tryRollBack(@NotNull Connection c, Savepoint savepoint) {
            try {
                c.rollback(savepoint);
                c.setAutoCommit(true);
                c.releaseSavepoint(savepoint);
                return true;
            }
            catch (SQLException e) {
                messageToUser.error(MessageFormat
                    .format("DBPropsCallable.tryRollBack\n{0}: {1}\nParameters: [c, savepoint]\nReturn: boolean\nStack:\n{2}", e.getClass().getTypeName(), e
                        .getMessage(), new TForms().fromArray(e)));
                return false;
            }
        }
    
        private @NotNull Savepoint makeSavePoint(@NotNull Connection c) throws SQLException {
            mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
            Savepoint savepoint = c.setSavepoint("BeforeUpdate");
            retBool.set(!savepoint.getSavepointName().isEmpty());
            return savepoint;
        }
        
        private Properties findRightProps() {
            File constForProps = new File(ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_PROPERTIES);
            addApplicationProperties();
            long fiveHRSAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5);
    
            boolean fileIsFiveHoursAgo = constForProps.lastModified() < fiveHRSAgo;
            boolean canNotWrite = !constForProps.canWrite();
            boolean isFileOldOrReadOnly = constForProps.exists() & (canNotWrite || fileIsFiveHoursAgo);
    
            messageToUser
                .info(MessageFormat.format("File {1} last mod is: {0}. FileIsFiveHoursAgo ({5}) = {4} , canWrite: {2}\n\'isFileOldOrReadOnly\' boolean is: {3}",
                    new Date(constForProps.lastModified()), constForProps.getName(), constForProps
                        .canWrite(), isFileOldOrReadOnly, fileIsFiveHoursAgo, new Date(fiveHRSAgo)));
            
            if (isFileOldOrReadOnly) {
                boolean isWritableSet = constForProps.setWritable(true);
    
                messageToUser
                    .info(MessageFormat.format("Setting file {1} to writable: {0}. Starting propsFileIsReadOnly meth...", isWritableSet, constForProps.canWrite()));
                propsFileIsReadOnly();
            }
            else {
                fileIsWritableOrNotExists();
            }
            return retProps;
        }
        
        private void fileIsWritableOrNotExists() {
            InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
            Properties props = initProperties.getProps();
            retBool.set(props.size() > 10);
            if (retBool.get()) {
                retProps.putAll(props);
                setProps(retProps);
            }
            else {
                retProps.putAll(getProps());
            }
        }
        
        private void propsFileIsReadOnly() {
            InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
            Properties props = initProperties.getProps();
            retProps.clear();
            retProps.putAll(props);
            
            if (retProps.size() > 9) {
                messageToUser.warn(MessageFormat.format("props size is {1}. Set = {0} to {2}.",
                    initProperties.setProps(retProps), retProps.size(), initProperties.getClass().getTypeName()));
                setProps(props);
                messageToUser.warn(MessageFormat.format("props size is {1}. Set = {0} to {2}.",
                    initProperties.setProps(retProps), retProps.size(), InitPropertiesAdapter.class.getTypeName()));
            }
            else {
                retProps.putAll(props);
            }
            retBool.set(retProps.size() > 9);
        }
        
        private void addApplicationProperties() {
            try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
                retProps.load(inputStream);
                messageToUser.info(MessageFormat.format("Added {0} properties from application.", retProps.size()));
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat.format("DBPropsCallable.addApplicationProperties threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        
        private int delFromDataBase() {
            final String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";
            miniLogger.add("4. Starting DELETE: " + sql);
            int pDeleted = 0;
            try (Connection c = mysqlDataSource.getConnection()) {
                mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
                mysqlDataSource.setRelaxAutoCommit(true);
                mysqlDataSource.setContinueBatchOnError(false);
                Savepoint before = c.setSavepoint("BeforeDel");
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    pDeleted = preparedStatement.executeUpdate();
                }
                catch (Exception e) {
                    messageToUser.error(e.getMessage(), before.getClass().getSimpleName(), before.getSavepointName());
                    c.rollback(before);
                    c.releaseSavepoint(before);
                }
            }
            catch (SQLException e) {
                System.err.println(e.getErrorCode() + " " + e.getMessage() + " from" + getClass().getSimpleName());
            }
            retBool.set(pDeleted > 0);
            return pDeleted;
        }
    
        private void tryWithLibsInit() {
            ru.vachok.mysqlandprops.props.InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            retBool.set(initProperties.setProps(propsToSave));
        }
    
    }
}
