// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.RegRuMysql;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.abstr.DataBaseRegSQL;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;


@SuppressWarnings("DuplicateStringLiteralInspection") public class DBPropsCallable implements Callable<Properties>, DataBaseRegSQL {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(DBPropsCallable.class.getSimpleName());
    
    private static final String pathToPropsName = "ConstantsFor.properties";
    
    /**
     Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();
    
    private boolean isForced;
    
    private IllegalComponentStateException in_progress = new IllegalComponentStateException("In progress");
    
    private MysqlDataSource mysqlDataSource = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES);
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private Properties retProps = new Properties();
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    public DBPropsCallable(Properties toUpdate) {
        this.propsToSave = toUpdate;
    }
    
    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave) {
        this.mysqlDataSource = mysqlDataSource;
        this.propsToSave = propsToSave;
    }
    
    DBPropsCallable() {
    }
    
    @Override
    public Properties call() {
        Properties props = new Properties();
        try {
            props = findRightProps();
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return props;
    }
    
    
    @Override public int selectFrom() {
        throw in_progress;
    }
    
    
    @Override public int insertTo() {
        throw in_progress;
    }
    
    
    @Override public int deleteFrom() {
        return delFromDataBase();
    }
    
    
    @Override public int updateTable() {
        if (upProps()) {
            return 1;
        }
        else {
            return 0;
        }
    }
    
    
    @SuppressWarnings("DuplicateStringLiteralInspection") private boolean upProps() {
        final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        miniLogger.add("2. " + sql);
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
        try (Connection c = mysqlDataSource.getConnection()) {
            if (propsToSave.size() > 5)
                Objects.requireNonNull(propsToSave)
                    .store(new FileOutputStream(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES), getClass().getSimpleName() + ".upProps");
            else {
                propsToSave.putAll(new FileProps(ConstantsFor.class.getSimpleName()).getProps());
            }
            int executeUpdateInt = 0;
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
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
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + " " + e.getClass() + ".upProps");
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage());
            deleteFrom();
            new DBPropsCallable(this.propsToSave).updateTable();
        }
        return false;
    }
    
    private Properties findRightProps() throws IOException {
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
    
    private void fileIsWritableOrNotExists() throws IOException {
        InitProperties initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
        retProps.putAll(initProperties.getProps());
        retProps.setProperty("loadedFromFile", "false");
        new AppComponents().updateProps(retProps);
    }
    
    private void readOnlyFileReturnFile(File prFile) throws IOException {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
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
    
    private boolean forceUpdate() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        Properties updateProps = this.propsToSave;
        initProperties.delProps();
        initProperties.setProps(updateProps);
        messageToUser.info(getClass().getSimpleName() + ".forceUpdate", "delFromDataBase()", " = " + delFromDataBase());
        retBool.set(upProps());
        return retBool.get();
    }
    
    private int delFromDataBase() {
        final String sql = "delete FROM `ru_vachok_networker` where `javaid` =  'ConstantsFor'";
        miniLogger.add("4. Starting DELETE: " + sql);
        int pDeleted = 0;
        try (Connection c = mysqlDataSource.getConnection();
             PreparedStatement preparedStatement = c.prepareStatement(sql);
        ) {
            pDeleted = preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            messageToUser.error(e.getMessage() + " " + e.getClass() + ".delFromDataBase");
        }
        retBool.set(pDeleted > 0);
        return pDeleted;
    }
}
