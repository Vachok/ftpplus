// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.*;
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
public class DBPropsCallable implements Callable<Properties>, ru.vachok.networker.restapi.props.InitProperties {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, DBPropsCallable.class.getSimpleName());
    
    /**
     Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();
    
    private final Properties retProps = new Properties();
    
    private static final TForms T_FORMS = AbstractForms.getI();
    
    private String propsDBID = ConstantsFor.class.getSimpleName();
    
    private String callerStack = "not set";
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    private MysqlDataSource mysqlDataSource;
    
    public DBPropsCallable() {
        this.mysqlDataSource = InitProperties.getInstance(InitProperties.ATAPT).getRegSourceForProperties();
        this.propsDBID = ConstantsFor.class.getSimpleName();
        setPassSQL();
    }
    
    private void setPassSQL() {
        String dbUser = InitProperties.getUserPref().get(PropertiesNames.DBUSER, "");
        String dbPass = InitProperties.getUserPref().get(PropertiesNames.DBPASS, "");
        mysqlDataSource.setUser(dbUser);
        mysqlDataSource.setPassword(dbPass);
        mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        if (dbUser.toLowerCase().contains("u0466446")) {
            setUserPrefUserPass(dbUser, dbPass);
        }
        else {
            setUserPassFromPropsFile();
        }
    }
    
    private void setUserPrefUserPass(String dbUser, String dbPass) {
        InitProperties.setPreference(PropertiesNames.DBUSER, dbUser);
        InitProperties.setPreference(PropertiesNames.DBPASS, dbPass);
    }
    
    private void setUserPassFromPropsFile() {
        InitProperties fileInit = InitProperties.getInstance(InitProperties.FILE);
        Properties props = fileInit.getProps();
        String user = props.getProperty(PropertiesNames.DBUSER, "");
        this.mysqlDataSource.setUser(user);
        String pass = props.getProperty(PropertiesNames.DBPASS, "");
        this.mysqlDataSource.setPassword(pass);
        this.mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        setUserPrefUserPass(user, pass);
    }
    
    public DBPropsCallable(@NotNull String propsIDClass) {
        if (propsIDClass.equalsIgnoreCase(InitProperties.DB_LOCAL)) {
            this.mysqlDataSource = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDataSource();
            propsIDClass = ConstantsFor.class.getSimpleName();
        }
        else {
            initDefaultDB();
        }
        this.propsDBID = propsIDClass;
        Thread.currentThread().setName(this.getClass().getSimpleName());
    }
    
    private void initDefaultDB() {
        try {
            this.mysqlDataSource = DataConnectTo.getDefaultI().getDataSource();
            mysqlDataSource.setUser(InitProperties.getUserPref().get(PropertiesNames.DBUSER, "nouser"));
            mysqlDataSource.setPassword(InitProperties.getUserPref().get(PropertiesNames.DBPASS, "nopass"));
            mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            this.mysqlDataSource = InitProperties.getInstance(InitProperties.ATAPT).getRegSourceForProperties();
            messageToUser.warn(this.getClass().getSimpleName(), "constructed with:", mysqlDataSource.getURL());
        }
    }
    
    protected DBPropsCallable(@NotNull Properties toUpdate) {
        this.mysqlDataSource = InitProperties.getInstance(InitProperties.ATAPT).getRegSourceForProperties();
        this.propsToSave = toUpdate;
    
        mysqlDataSource.setUser(toUpdate.getProperty(PropertiesNames.DBUSER));
        mysqlDataSource.setPassword(toUpdate.getProperty(PropertiesNames.DBPASS));
        Thread.currentThread().setName("DBPr(Pr)");
    }
    
    @Override
    public MysqlDataSource getRegSourceForProperties() {
        MysqlDataSource dS = InitProperties.getInstance(InitProperties.ATAPT).getRegSourceForProperties();
        dS.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        return dS;
    }
    
    @Override
    public Properties getProps() {
        Properties calledPr = call();
        if (calledPr.size() < 9) {
            calledPr = getPropsFromSRVDatabase();
        }
        if (calledPr.size() > 9) {
            InitProperties.getInstance(InitProperties.FILE).setProps(calledPr);
        }
        return calledPr;
    }
    
    private Properties getPropsFromSRVDatabase() {
        this.mysqlDataSource = DataConnectTo.getDefaultI().getDataSource();
        mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        final String sql = "SELECT * FROM `ru_vachok_networker`";
        try (Connection connection = mysqlDataSource.getConnection()) {
            try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
                pStatement.setString(1, propsDBID);
                try (ResultSet r = pStatement.executeQuery()) {
                    while (r.next()) {
                        retProps.setProperty(r.getString(ConstantsFor.DBCOL_PROPERTY), r.getString(ConstantsFor.DBCOL_VALUEOFPROPERTY));
                    }
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getPropsPr", e));
            retProps.putAll(InitProperties.getInstance(FILE).getProps());
        }
        return retProps;
    }
    
    @Override
    public boolean setProps(Properties properties) {
        DBPropsCallable.LocalPropertiesFinder localPropsFinder = new DBPropsCallable.LocalPropertiesFinder();
        this.propsToSave = properties;
    
        retBool.set(localPropsFinder.upProps());
        boolean isFileSet = InitProperties.getInstance(InitProperties.FILE).setProps(properties);
        if (properties.size() < 9) {
            messageToUser.error(this.getClass().getSimpleName(), "setProps: " + isFileSet, "PROPS SIZE TO SMALL: " + properties.size());
        }
        return retBool.get();
    }
    
    @Override
    public Properties call() {
        DBPropsCallable.LocalPropertiesFinder localPropertiesFinder = new DBPropsCallable.LocalPropertiesFinder();
        synchronized(retProps) {
            this.callerStack = T_FORMS.fromArray(Thread.currentThread().getStackTrace());
            return localPropertiesFinder.findRightProps();
        }
    }
    
    @Override
    public boolean delProps() {
        final String sql = "DELETE FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        mysqlDataSource.setDatabaseName(ConstantsFor.DBBASENAME_U0466446_PROPERTIES);
        try (Connection c = mysqlDataSource.getConnection()) {
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
        final StringBuilder sb = new StringBuilder("DBPropsCallable{");
        sb.append("retProps=").append(retProps.size());
        sb.append(", retBool=").append(retBool);
        sb.append(", propsToSave=").append(propsToSave.size());
        sb.append(", propsDBID='").append(propsDBID).append('\'');
        sb.append(", mysqlDataSource=").append(mysqlDataSource.getURL());
        sb.append(", miniLogger=").append(miniLogger.size());
        sb.append('}');
        return sb.toString();
    }
    
    protected class LocalPropertiesFinder extends DBPropsCallable {
        
        
        private boolean upProps() {
            final String sql = "insert props (property, valueofproperty, javaid, stack) values (?,?,?,?)";
            mysqlDataSource.setDatabaseName(ConstantsFor.STR_VELKOM);
            retBool.set(false);
            callerStack = T_FORMS.fromArray(Thread.currentThread().getStackTrace());
    
            try (Connection c = mysqlDataSource.getConnection()) {
                int executeUpdateInt = 0;
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    retBool.set(delProps());
                    propsToSave.setProperty(PropertiesNames.THISPC, UsefulUtilities.thisPC());
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
                    messageToUser.error("LocalPropertiesFinder.upProps", e.getMessage(), T_FORMS.exceptionNetworker(e.getStackTrace()));
                    retBool.set(false);
                    tryWithLibsInit();
                }
            }
            miniLogger.add(callerStack);
            FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
            return retBool.get();
        }
        
        private void tryWithLibsInit() {
            ru.vachok.mysqlandprops.props.InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            retBool.set(initProperties.setProps(propsToSave));
        }
    
        private Properties findRightProps() {
            File constForProps = new File(ConstantsFor.class.getSimpleName() + FileNames.EXT_PROPERTIES);
            addApplicationProperties();
            long fiveHRSAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5);
            
            boolean fileIsFiveHoursAgo = constForProps.lastModified() < fiveHRSAgo;
            boolean canNotWrite = !constForProps.canWrite();
            boolean isFileOldOrReadOnly = constForProps.exists() & (canNotWrite | fileIsFiveHoursAgo);
            
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
        
        private void addApplicationProperties() {
    
            try (InputStream inputStream = getClass().getResourceAsStream("/application.properties")) {
                retProps.load(inputStream);
                messageToUser.info(MessageFormat.format("Added {0} properties from application.", retProps.size()));
            }
            catch (IOException e) {
                messageToUser.error(MessageFormat.format("DBPropsCallable.addApplicationProperties threw away: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        
        private void propsFileIsReadOnly() {
            InitProperties initProperties = InitProperties.getInstance(FILE);
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
        
        private void fileIsWritableOrNotExists() {
            InitProperties initProperties = new FilePropsLocal(ConstantsFor.class.getSimpleName());
            Properties props = initProperties.getProps();
            retBool.set(props.size() > 10);
            if (retBool.get()) {
                retProps.putAll(props);
                setProps(retProps);
            }
            else {
                retProps.putAll(InitProperties.getInstance(InitProperties.ATAPT).getProps());
            }
        }
    
    }
}
