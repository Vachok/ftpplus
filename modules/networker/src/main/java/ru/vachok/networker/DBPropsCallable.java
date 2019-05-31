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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 Сохранение {@link Properties} в базу
 <p>
 
 @see AppComponents#saveAppPropsForce()
 @since 25.02.2019 (10:12) */
@SuppressWarnings("DuplicateStringLiteralInspection") public class DBPropsCallable implements Callable<Properties>, DataBaseRegSQL {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(DBPropsCallable.class.getSimpleName());
    
    private static final String pathToPropsName = "ConstantsFor.properties";
    
    /**
     Запишем .mini
     */
    private final Collection<String> miniLogger = new PriorityQueue<>();
    
    private IllegalComponentStateException in_progress = new IllegalComponentStateException("In progress");
    
    private MysqlDataSource mysqlDataSource = new RegRuMysql().getDataSourceSchema(ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES);
    
    /**
     {@link Properties} для сохданения.
     */
    private Properties propsToSave = new Properties();
    
    private boolean isForced;
    
    private AtomicBoolean retBool = new AtomicBoolean(false);
    
    public DBPropsCallable(Properties toUpdate) {
        this.propsToSave = toUpdate;
    }
    
    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave) {
        this.mysqlDataSource = mysqlDataSource;
        this.propsToSave = propsToSave;
    }
    
    DBPropsCallable(MysqlDataSource mysqlDataSource, Properties propsToSave, boolean isForced) {
        this.mysqlDataSource = mysqlDataSource;
        this.propsToSave = propsToSave;
        this.isForced = isForced;
    }
    
    private DBPropsCallable() {
    }
    
    
    @Override
    public Properties call() {
        if (isForced) {
            propsToSave.setProperty(ConstantsFor.PR_FORCE , String.valueOf(forceUpdate()));
            return propsToSave;
        }
        else {
            
            return findRightProps();
        }
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
        String methName = ".upProps";
        final String sql = "insert into ru_vachok_networker (property, valueofproperty, javaid) values (?,?,?)";
        miniLogger.add("2. " + sql);
        FileSystemWorker.writeFile(getClass().getSimpleName() + ".mini", miniLogger.stream());
        try (Connection c = mysqlDataSource.getConnection()) {
            Objects.requireNonNull(propsToSave)
                .store(new FileOutputStream(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES), getClass().getSimpleName() + ".upProps");
            int executeUpdateInt = 0;
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
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
        }
        return false;
    }
    
    private Properties findRightProps() {
        File prFile = new File(ConstantsFor.class.getSimpleName() + ConstantsFor.FILEEXT_PROPERTIES);
        InitProperties initProperties;
        Properties retProps;
        if (prFile.exists() & !prFile.canWrite()) {
            initProperties = new FileProps(prFile.getName().replace(ConstantsFor.FILEEXT_PROPERTIES, ""));
            retProps = initProperties.getProps();
            retProps.setProperty("file", "true");
            new AppComponents().updateProps(retProps);
        }
        else {
            initProperties = new DBRegProperties(ConstantsFor.APPNAME_WITHMINUS + ConstantsFor.class.getSimpleName());
            retProps = initProperties.getProps();
            retProps.setProperty("file", "false");
        }
        retProps.setProperty(ConstantsFor.PR_FORCE , "false");
        return retProps;
    }
    
    
    private boolean forceUpdate() {
        InitProperties initProperties = new FileProps(ConstantsFor.class.getSimpleName());
        Properties updateProps = this.propsToSave;
        initProperties.delProps();
        initProperties.setProps(updateProps);
        messageToUser.info(getClass().getSimpleName() + ".forceUpdate" , "delFromDataBase()" , " = " + delFromDataBase());
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
