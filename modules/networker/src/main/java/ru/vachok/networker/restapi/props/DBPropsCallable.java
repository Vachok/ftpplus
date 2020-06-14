// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.database.DataConnectTo;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
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

    private Properties retProps = new Properties();

    private String propsDBID = ConstantsFor.class.getSimpleName();

    private String callerStack = "not set";

    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();

    private final AtomicBoolean retBool = new AtomicBoolean(false);

    public DBPropsCallable() {
        this.propsDBID = ConstantsFor.class.getSimpleName();
        setPassSQL();
    }

    private void setPassSQL() {
        String dbUser = InitProperties.getUserPref().get(PropertiesNames.DBUSER, "");
        String dbPass = InitProperties.getUserPref().get(PropertiesNames.DBPASS, "");
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

    @Override
    public boolean setProps(Properties properties) {
        try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.props")) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setSavepoint();
            try (PreparedStatement preparedStatement = connection.prepareStatement("insert into props values (property, valueofproperty), (?, ?)")) {
                final Set<Object> objects = properties.keySet();
                for (Object pr : objects) {
                    preparedStatement.setString(1, pr.toString());
                    preparedStatement.setString(2, properties.get(pr).toString());
                    preparedStatement.executeUpdate();
                }
                connection.commit();
                return true;
            }
        }
        catch (SQLException e) {
            messageToUser.warn(DBPropsCallable.class.getSimpleName(), e.getMessage(), " see line: 112 ***");
            return false;
        }
    }

    public DBPropsCallable(@NotNull String propsIDClass) {
        this.propsDBID = propsIDClass;
    }

    protected DBPropsCallable(@NotNull Properties toUpdate) {
        this.propsToSave = toUpdate;
        Thread.currentThread().setName("DBPr(Pr)");
    }

    @Override
    public Properties getProps() {
        retProps = call();
        return retProps;
    }

    @Override
    public boolean delProps() {
        final String sql = "DELETE FROM `ru_vachok_networker` WHERE `javaid` LIKE ? ";
        try (Connection c = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection(ConstantsFor.DB_MEMPROPERTIES)) {
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
    public Properties call() {
        DBPropsCallable.LocalPropertiesFinder localPropertiesFinder = new DBPropsCallable.LocalPropertiesFinder();
        this.callerStack = AbstractForms.fromArray(Thread.currentThread().getStackTrace());
        return localPropertiesFinder.findRightProps();
    }

    private void setUserPassFromPropsFile() {
        InitProperties fileInit = InitProperties.getInstance(InitProperties.FILE);
        Properties props = fileInit.getProps();
        String user = props.getProperty(PropertiesNames.DBUSER, "");
        String pass = props.getProperty(PropertiesNames.DBPASS, "");

        setUserPrefUserPass(user, pass);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DBPropsCallable{");
        sb.append("retProps=").append(retProps.size());
        sb.append(", retBool=").append(retBool);
        sb.append(", propsToSave=").append(propsToSave.size());
        sb.append(", propsDBID='").append(propsDBID).append('\'');
        sb.append(", miniLogger=").append(miniLogger.size());
        sb.append('}');
        return sb.toString();
    }

    protected class LocalPropertiesFinder extends DBPropsCallable {


        @NotNull
        private Properties getPropsByID() {
            Properties properties = new Properties();
            String[] propsId = propsDBID.split("-");
            final String sql = String.format("SELECT * FROM u0466446_properties.%s", propsId[0]);
            try (Connection connection = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("u0466446_properties." + propsId[0]);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    if (resultSet.getString("javaid").equalsIgnoreCase(propsId[1])) {
                        properties.put(resultSet.getString(ConstantsFor.DBCOL_PROPERTY), resultSet.getString(ConstantsFor.DBCOL_VALUEOFPROPERTY));
                    }
                }
            }
            catch (SQLException | RuntimeException e) {
                messageToUser.warn(DBPropsCallable.LocalPropertiesFinder.class.getSimpleName(), e.getMessage(), " see line: 250 ***");
                properties.putAll(InitProperties.getInstance(InitProperties.FILE).getProps());
            }
            retProps.putAll(properties);
            return properties;
        }

        private void tryWithLibsInit() {
            ru.vachok.mysqlandprops.props.InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            retBool.set(initProperties.setProps(propsToSave));
        }

        private Properties findRightProps() {
            File constForProps = new File(FileNames.CONSTANTSFOR_PROPERTIES);
            addApplicationProperties();
            long fiveHRSAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(5);
            boolean fileIsFiveHoursAgo = constForProps.lastModified() < fiveHRSAgo;
            boolean canNotWrite = !constForProps.canWrite();
            boolean isFileOldOrReadOnly = constForProps.exists() & (canNotWrite | fileIsFiveHoursAgo);
            if (propsDBID.contains("-")) {
                return getPropsByID();
            }
            else if (isFileOldOrReadOnly) {
                propsFileIsReadOnly();
                boolean isWritableSet = constForProps.setWritable(true);
                messageToUser.info(this.getClass().getSimpleName(), "mem.properties updated", String.valueOf(isWritableSet));
            }
            else {
                try {
                    fileIsWritableOrNotExists();
                }
                catch (IOException e) {
                    messageToUser.error("LocalPropertiesFinder.findRightProps", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
                }
                finally {
                    retBool.set(retProps.size() > 10);
                }
            }
            return retProps;
        }

        private boolean upProps() {
            final String sql = "insert props (property, valueofproperty, javaid, stack) values (?,?,?,?)";
            retBool.set(false);
            callerStack = AbstractForms.fromArray(Thread.currentThread().getStackTrace());

            try (Connection c = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDefaultConnection("velkom.props")) {
                c.setAutoCommit(false);
                c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                c.setSavepoint();
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
                        c.commit();
                    }
                    retBool.set(executeUpdateInt > 0);
                }
            }
            catch (SQLException e) {
                if (!(e instanceof MySQLIntegrityConstraintViolationException)) {
                    messageToUser.error("LocalPropertiesFinder.upProps", e.getMessage(), AbstractForms.networkerTrace(e.getStackTrace()));
                    retBool.set(false);
                    tryWithLibsInit();
                }
                messageToUser.warn(DBPropsCallable.LocalPropertiesFinder.class.getSimpleName(), e.getMessage(), " see line: 190 ***");
            }
            miniLogger.add(callerStack);
            return retBool.get();
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
            InitProperties initProperties = InitProperties.getInstance(InitProperties.FILE);
            Properties props = initProperties.getProps();
            retProps.clear();
            retProps.putAll(props);
            if (retProps.size() > 9) {
                initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
                initProperties.setProps(retProps);
            }
            else {
                retProps.putAll(props);
            }
            retBool.set(retProps.size() > 9);
        }

        private void fileIsWritableOrNotExists() throws IOException {
            if (retProps.size() < 10) {
                retProps.clear();
                Properties props = InitProperties.getInstance(InitProperties.DB_MEMTABLE).getProps();
                retProps.putAll(props);
                retBool.set(retProps.size() > 10);
            }
            if (retBool.get()) {
                retProps.store(new FileOutputStream(FileNames.CONSTANTSFOR_PROPERTIES), this.getClass().getSimpleName());
            }
        }

    }
}
