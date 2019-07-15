// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.componentsrepo.FilePropsLocal;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.DataConnectTo;
import ru.vachok.networker.restapi.InitProperties;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.RegRuMysqlLoc;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.props.PropertiesAdapter;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings("DuplicateStringLiteralInspection")
public class DBPropsCallable implements Callable<Properties>, InitProperties {
    
    
    private final MessageToUser messageToUser = new MessageLocal(DBPropsCallable.class.getSimpleName());
    
    /**
     Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();
    
    private final Properties retProps = new Properties();
    
    private boolean isForced;
    
    private DataConnectTo dataConnectTo = new RegRuMysqlLoc();
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    private MysqlDataSource mysqlDataSource = dataConnectTo.getDataSource();
    
    public DBPropsCallable(Properties toUpdate) {
        this.propsToSave = toUpdate;
        mysqlDataSource.setUser(toUpdate.getProperty(ConstantsFor.PR_DBUSER));
        mysqlDataSource.setPassword(toUpdate.getProperty(ConstantsFor.PR_DBPASS));
    }
    
    protected DBPropsCallable() {
    
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        DataConnectTo dataConnectTo = new RegRuMysqlLoc();
        return dataConnectTo.getDataSource();
    }
    
    @Override
    public Properties getProps() {
        return call();
    }
    
    @Override
    public boolean setProps(Properties properties) {
        Properties updateProps = this.propsToSave;
    
        sqlUserPassSet();
        retBool.set(upProps());
        messageToUser.info(MessageFormat.format("Updating database {0} is {1}", mysqlDataSource.getURL(), retBool.get()));
        return retBool.get();
    }
    
    @Override
    public Properties call() {
        synchronized(retProps) {
            Properties props = new Properties();
            props = findRightProps();
            return props;
        }
    }
    
    @Override
    public boolean delProps() {
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
        try (InputStream inputStream = getClass().getResourceAsStream("/msqldata.properties")) {
            userPass.load(inputStream);
            mysqlDataSource.setUser(userPass.getProperty(ConstantsFor.PR_DBUSER));
            mysqlDataSource.setPassword(userPass.getProperty(ConstantsFor.PR_DBPASS));
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat
                .format("DBPropsCallable.setProps threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
    }
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private boolean upProps() {
        final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        miniLogger.add("2. " + sql);
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
        try (Connection c = mysqlDataSource.getConnection()) {
            mysqlDataSource.setRelaxAutoCommit(true);
            mysqlDataSource.setDatabaseName("u0466446_properties");
            Savepoint savepoint = c.setSavepoint("BeforeUpdate");
            if (propsToSave.size() > 5) {
                Objects.requireNonNull(propsToSave)
                    .store(new FileOutputStream(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES), getClass().getSimpleName() + ".upProps");
            }
            else {
                propsToSave.putAll(new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps());
            }
            int executeUpdateInt = 0;
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                retBool.set(delProps());
                mysqlDataSource.setContinueBatchOnError(true);
                for (Map.Entry<Object, Object> entry : propsToSave.entrySet()) {
                    Object x = entry.getKey();
                    Object y = entry.getValue();
                    preparedStatement.setString(1, x.toString());
                    preparedStatement.setString(2, y.toString());
                    preparedStatement.setString(3, ConstantsFor.class.getSimpleName());
                    executeUpdateInt += preparedStatement.executeUpdate();
                }
                return executeUpdateInt > 0;
            }
            catch (Exception e) {
                messageToUser.error(e.getMessage(), savepoint.getClass().getSimpleName(), savepoint.getSavepointName());
                c.rollback();
                c.releaseSavepoint(savepoint);
            }
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("DBPropsCallable.upProps threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat
                .format("DBPropsCallable.upProps threw away: {0}, ({1}).\n\n{2}", e.getMessage(), e.getClass().getName(), new TForms().fromArray(e)));
        }
        return false;
    }
    
    private Properties findRightProps() {
        File constForProps = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES);
        if (constForProps.exists() & !constForProps.canWrite()) {
            readOnlyFileReturnFile(constForProps);
        }
        else {
            fileIsWritableOrNotExists();
        }
        retProps.putAll(getApplicationProperties());
        return retProps;
    }
    
    private void fileIsWritableOrNotExists() {
        DBRegProperties dbRegProperties = PropertiesAdapter.getDBRegProps(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        retProps.putAll(dbRegProperties.getProps());
        retProps.setProperty("loadedFromFile", ConstantsFor.STR_FALSE);
        new AppComponents().updateProps(retProps);
    }
    
    private void readOnlyFileReturnFile(File prFile) {
        InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
        retProps.putAll(initProperties.getProps());
        new AppComponents().updateProps(retProps);
    }
    
    private Map<?, ?> getApplicationProperties() {
        Map pMap = new HashMap();
        try (InputStream inputStream = getClass().getResourceAsStream("/application.properties");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(x->{
                if (!x.contains("#")) {
                    try {
                        String[] split = x.split("=");
                        pMap.put(split[0], split[1]);
                    }
                    catch (IndexOutOfBoundsException ignore) {
                        //
                    }
                }
            });
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " " + e.getClass() + ".getApplicationProperties");
        }
        ;
        return pMap;
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
