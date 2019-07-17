// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.MessageFormat;
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
    
    protected static final String DB_ID_FOR_PROPS = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    private final Properties retProps = new Properties();
    
    private String propsID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    private String callerStack = "not set";
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc(DB_ID_FOR_PROPS);
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    private MysqlDataSource mysqlDataSource = dataConnectTo.getDataSource();
    
    public DBPropsCallable() {
        this.mysqlDataSource.setDatabaseName(ConstantsFor.DBNAME_PROPERTIES);
        mysqlDataSource.setUser(AppComponents.getUserPref().get(ConstantsFor.PR_DBUSER, "nouser"));
        mysqlDataSource.setPassword(AppComponents.getUserPref().get(ConstantsFor.PR_DBPASS, "nopass"));
    }
    
    public DBPropsCallable(@NotNull Properties toUpdate) {
        this.propsToSave = toUpdate;
        mysqlDataSource.setUser(toUpdate.getProperty(ConstantsFor.PR_DBUSER));
        mysqlDataSource.setPassword(toUpdate.getProperty(ConstantsFor.PR_DBPASS));
    }
    
    public DBPropsCallable(String propsID) {
        this.propsID = propsID;
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        return dataConnectTo.getDataSource();
    }
    
    @Override
    public Properties getProps() {
        Properties outsideProps = new Properties();
        outsideProps.putAll(call());
        if (outsideProps.size() > 9) {
            return outsideProps;
        }
        else {
            return InitPropertiesAdapter.getProps();
        }
    }
    
    @Override
    public boolean setProps(Properties properties) {
        DBPropsCallable.LocalPropertiesFinder localPropertiesFinder = new DBPropsCallable.LocalPropertiesFinder();
        this.propsToSave = properties;
        localPropertiesFinder.sqlUserPassSet();
        retBool.set(localPropertiesFinder.upProps());
        messageToUser.info(MessageFormat.format("Updating database {0} is {1}", mysqlDataSource.getURL(), retBool.get()));
        return retBool.get();
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
        DBPropsCallable.LocalPropertiesFinder localPropertiesFinder = new DBPropsCallable.LocalPropertiesFinder();
        FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName()), "DELETE:\n" + new TForms().fromArray(Thread.currentThread().getStackTrace()));
        return localPropertiesFinder.delFromDataBase() > 0;
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
    
    private void tryWithLibsInit() {
        ru.vachok.mysqlandprops.props.InitProperties initProperties = new DBRegProperties(DB_ID_FOR_PROPS);
        retBool.set(initProperties.setProps(propsToSave));
        
    }
    
    protected class LocalPropertiesFinder extends DBPropsCallable {
        
        
        private void sqlUserPassSet() {
            Properties userPass = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream("/static/msqldata.properties")) {
                userPass.load(inputStream);
                mysqlDataSource.setUser(userPass.getProperty(ConstantsFor.PR_DBUSER));
                mysqlDataSource.setPassword(userPass.getProperty(ConstantsFor.PR_DBPASS));
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat
                    .format("DBPropsCallable.setProps threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
            }
        }
        
        private boolean upProps() {
            final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid, stack) values (?,?,?,?)";
            retBool.set(false);
            FileSystemWorker
                .appendObjectToFile(new File(this.getClass().getSimpleName()), "UPDATE:\n" + new TForms().fromArray(Thread.currentThread().getStackTrace()));
            try (Connection c = mysqlDataSource.getConnection()) {
                int executeUpdateInt = 0;
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    retBool.set(delProps());
                    propsToSave.setProperty("thispc", ConstantsFor.thisPC());
                    for (Map.Entry<Object, Object> entry : propsToSave.entrySet()) {
                        Object x = entry.getKey();
                        Object y = entry.getValue();
                        preparedStatement.setString(1, x.toString());
                        preparedStatement.setString(2, y.toString());
                        preparedStatement.setString(3, ConstantsFor.class.getSimpleName());
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
        
        private Savepoint makeSavePoint(@NotNull Connection c) throws SQLException {
            mysqlDataSource.setDatabaseName(ConstantsFor.DBNAME_PROPERTIES);
            Savepoint savepoint = c.setSavepoint("BeforeUpdate");
            retBool.set(!savepoint.getSavepointName().isEmpty());
            return savepoint;
        }
        
        private Properties findRightProps() {
            File constForProps = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES);
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
            
            retBool.set(props.size() > 9);
            if (retBool.get()) {
                retProps.putAll(props);
                initProperties.setProps(retProps);
            }
            else {
                retProps.putAll(initProperties.getProps());
            }
        }
        
        private void propsFileIsReadOnly() {
            ru.vachok.networker.restapi.InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
            Properties props = initProperties.getProps();
            retProps.putAll(props);
            
            if (retProps.size() > 9) {
                messageToUser.warn(MessageFormat.format("props size is {1}. Set = {0} to {2}.",
                    initProperties.setProps(retProps), retProps.size(), initProperties.getClass().getTypeName()));
    
                InitPropertiesAdapter.setProps(retProps);
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
                mysqlDataSource.setDatabaseName(ConstantsFor.DBNAME_PROPERTIES);
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
    }
}
