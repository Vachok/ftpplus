// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.FileNames;
import ru.vachok.networker.enums.PropertiesNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.database.DataConnectToAdapter;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class DBPropsCallableTest implements InitProperties {
    
    
    private final Properties retProps = new FilePropsLocal(ConstantsFor.class.getSimpleName()).getProps();
    
    private InitProperties initProperties;
    
    private MysqlDataSource mysqlDataSource = DataConnectToAdapter.getLibDataSource();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    private String propsDBID = ConstantsFor.class.getSimpleName();
    
    private Properties propsToSave = new Properties();
    
    private String callerStack = "not set";
    
    @Test
    public void testGetRegSourceForProperties() {
        MysqlDataSource sourceForProperties = initProperties.getRegSourceForProperties();
        String propertiesURL = sourceForProperties.getURL();
        Assert.assertEquals(propertiesURL, "jdbc:mysql://server202.hosting.reg.ru:3306/u0466446_properties");
    }
    
    @Test
    public void testGetProps() {
        Properties propertiesProps = initProperties.getProps();
        Assert.assertFalse(propertiesProps.isEmpty());
    }
    
    @Test
    public void testSetProps() {
        this.initProperties = new DBPropsCallable(ConstantsFor.APPNAME_WITHMINUS, this.getClass().getSimpleName());
        Properties properties = new Properties();
        properties.setProperty("test", "test");
        initProperties.setProps(properties);
        Properties initPropertiesProps = initProperties.getProps();
        Assert.assertEquals(initPropertiesProps.getProperty("test"), "test");
    }
    
    @Test
    public void testCall() {
        Properties call = new DBPropsCallable().call();
        long lastscan = Long.parseLong(call.getProperty(ConstantsNet.PR_LASTSCAN));
        Assert.assertTrue(lastscan > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)), new Date(lastscan).toString());
    }
    
    @Test
    public void localFind() {
        Properties props = new LocalPropertiesFinder$$COPY().findRightProps();
        Assert.assertTrue(props.size() > 10);
    }
    
    @Test
    public void testDelProps() {
        this.initProperties = new DBPropsCallable(ConstantsFor.APPNAME_WITHMINUS, this.getClass().getSimpleName());
        Assert.assertTrue(initProperties.delProps());
    }
    
    @Test
    public void testToString1() {
        String toString = initProperties.toString();
        Assert.assertTrue(toString.contains("RegRuMysqlLoc["), toString);
    }
    
    @Override
    public Properties getProps() {
        final String sql = "SELECT * FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        try (Connection connection = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_TESTING)) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
                p.setString(1, propsDBID);
                try (ResultSet r = p.executeQuery()) {
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
        DBPropsCallableTest.LocalPropertiesFinder$$COPY propertiesFinder$$COPY = new DBPropsCallableTest.LocalPropertiesFinder$$COPY();
        this.propsToSave = properties;
        sqlUserPassSet();
        retBool.set(propertiesFinder$$COPY.upProps());
        messageToUser.info(MessageFormat.format("Updating database {0} is {1}", mysqlDataSource.getURL(), retBool.get()));
        return retBool.get();
    }
    
    @Override
    public boolean delProps() {
        final String sql = "DELETE FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        try (Connection connection = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_TESTING)) {
            try (PreparedStatement p = connection.prepareStatement(sql)) {
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
    
    private boolean setProps$$COPY(Properties props) {
        DBPropsCallableTest.LocalPropertiesFinder$$COPY localPropsFinder = new DBPropsCallableTest.LocalPropertiesFinder$$COPY();
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
    
    private boolean delProps$$COPY() {
        final String sql = "DELETE FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        try (Connection connection = DataConnectToAdapter.getRegRuMysqlLibConnection(ConstantsFor.DBBASENAME_U0466446_PROPERTIES)) {
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, propsDBID);
                int update = pStatement.executeUpdate();
                return update > 0;
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DBPropsCallable.delProps: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return false;
        }
    }
    
    protected class LocalPropertiesFinder$$COPY extends DBPropsCallableTest {
        
        
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
        
        private boolean upProps() {
            final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid, stack) values (?,?,?,?)";
            retBool.set(false);
            callerStack = new TForms().fromArray(Thread.currentThread().getStackTrace());
            FileSystemWorker.appendObjectToFile(new File(this.getClass().getSimpleName()), "UPDATE:\n" + callerStack);
            try (Connection c = mysqlDataSource.getConnection()) {
                int executeUpdateInt = 0;
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    retBool.set(delProps$$COPY());
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
            return retBool.get();
        }
        
        private void tryWithLibsInit() {
            ru.vachok.mysqlandprops.props.InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            retBool.set(initProperties.setProps(propsToSave));
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
            mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_TESTING);
            Savepoint savepoint = c.setSavepoint("BeforeUpdate");
            retBool.set(!savepoint.getSavepointName().isEmpty());
            return savepoint;
        }
        
        private Properties findRightProps() {
            File constForProps = new File(ConstantsFor.class.getSimpleName() + FileNames.FILEEXT_TEST);
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
                setProps$$COPY(props);
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
            
            int pDeleted = 0;
            try (Connection connection = mysqlDataSource.getConnection()) {
                mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_TESTING);
                mysqlDataSource.setRelaxAutoCommit(true);
                mysqlDataSource.setContinueBatchOnError(false);
                Savepoint before = connection.setSavepoint("BeforeDel");
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    pDeleted = preparedStatement.executeUpdate();
                }
                catch (Exception e) {
                    messageToUser.error(e.getMessage(), before.getClass().getSimpleName(), before.getSavepointName());
                    connection.rollback(before);
                    connection.releaseSavepoint(before);
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