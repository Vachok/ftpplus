// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.FilePropsLocal;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
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
    
    private final Properties retProps = new Properties();
    
    private final String propsID = ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName();
    
    private boolean isForced;
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc();
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    private MysqlDataSource mysqlDataSource = dataConnectTo.getDataSource();
    
    public DBPropsCallable() {
        this.mysqlDataSource.setDatabaseName("u0466446_properties");
    }
    
    private DBPropsCallable(@NotNull Properties toUpdate) {
        this.propsToSave = toUpdate;
        mysqlDataSource.setUser(toUpdate.getProperty(ConstantsFor.PR_DBUSER));
        mysqlDataSource.setPassword(toUpdate.getProperty(ConstantsFor.PR_DBPASS));
    }
    
    static {
        try {
            Driver driver = new com.mysql.jdbc.Driver();
            DriverManager.registerDriver(driver);
        }
        catch (SQLException e) {
            System.err.println(FileSystemWorker.error(DBPropsCallable.class.getSimpleName() + ".static initializer", e));
        }
        
    }
    
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        DataConnectTo dataConnectTo = new RegRuMysqlLoc();
        return dataConnectTo.getDataSource();
    }
    
    @Override
    public Properties getProps() {
    
        return PropertiesAdapter.getDBRegProps(propsID).getProps();
    }
    
    @Override
    public boolean setProps(Properties properties) {
        this.propsToSave = properties;
        sqlUserPassSet();
        retBool.set(upProps());
        messageToUser.info(MessageFormat.format("Updating database {0} is {1}", mysqlDataSource.getURL(), retBool.get()));
        return retBool.get();
    }
    
    @Override
    public Properties call() {
        synchronized(retProps) {
            return findRightProps();
        }
    }
    
    @Override
    public boolean delProps() {
        FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName()), "DELETE:\n" + new TForms().fromArray(Thread.currentThread().getStackTrace()));
        return delFromDataBase() > 0;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBPropsCallable{");
        sb.append(", miniLogger=").append(miniLogger);
        sb.append(", retProps=").append(retProps);
        sb.append(", dataConnectTo=").append(dataConnectTo.getClass().getTypeName());
        sb.append(", propsToSave=").append(propsToSave);
        sb.append(", mysqlDataSource=").append(mysqlDataSource.getURL());
        sb.append('}');
        return sb.toString();
    }
    
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
        final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        retBool.set(false);
        FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName()), "UPDATE:\n" + new TForms().fromArray(Thread.currentThread().getStackTrace()));
        
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
                    executeUpdateInt += preparedStatement.executeUpdate();
                }
                retBool.set(executeUpdateInt > 0);
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                .format("DBPropsCallable.upProps\n{0}: {1}\nParameters: []\nReturn: boolean\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms()
                    .fromArray(e)));
            retBool.set(false);
        }
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
        mysqlDataSource.setDatabaseName("u0466446_properties");
        Savepoint savepoint = c.setSavepoint("BeforeUpdate");
        retBool.set(!savepoint.getSavepointName().isEmpty());
        return savepoint;
    }
    
    private Properties findRightProps() {
        File constForProps = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES);
        addApplicationProperties();
    
        boolean isFileOldOrReadOnly = constForProps.exists() & (constForProps.lastModified() > (System.currentTimeMillis() - TimeUnit.DAYS
            .toSeconds(5)) || !constForProps.canWrite());
    
        if (isFileOldOrReadOnly) {
            constForProps.setWritable(true);
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
        retProps.putAll(PropertiesAdapter.getDBRegProps(propsID).getProps());
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        if (retProps.size() > 9) {
            messageToUser.warn(MessageFormat.format("Is DB {1}. Set = {0}. Local properties have {2} items",
                initProperties.setProps(retProps), mysqlDataSource.getURL(), retProps.size()));
        }
        else {
            retProps.putAll(initProperties.getProps());
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
            mysqlDataSource.setDatabaseName("u0466446_properties");
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
