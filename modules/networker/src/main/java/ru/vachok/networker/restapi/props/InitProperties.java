// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 14.07.2019 (17:33) */
public interface InitProperties extends ru.vachok.mysqlandprops.props.InitProperties {
    
    
    String DB_MEMTABLE = "db";
    
    String FILE = "file";
    
    String ATAPT = "adapt";
    
    String DB_LOCAL = "srv-inetstat";
    
    String TEST = "test";
    
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    static @NotNull Properties getTheProps() {
        @NotNull Properties result = PropsHelper.getAppPr();
        
        boolean isSmallSize = result.size() < 9;
        if (isSmallSize) {
            new PropsHelper().loadPropsFromDB();
            isSmallSize = result.size() < 9;
            if (isSmallSize) {
                result.putAll(new DBPropsCallable().call());
                isSmallSize = result.size() < 9;
            }
            if (!isSmallSize) {
                getInstance(FILE).setProps(result);
            }
            else {
                System.err.println(MessageFormat.format("{0}. APP_PR getter. line 135 size = {1} !!!", InitProperties.class.getSimpleName(), result.size()));
            }
        }
        return result;
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    static @NotNull InitProperties getInstance(@NotNull String type) {
        setPreference(InitProperties.class.getSimpleName(), type);
        switch (type) {
            case DB_MEMTABLE:
                return new MemoryProperties();
            case DB_LOCAL:
                return new DBPropsCallable(ConstantsFor.class.getSimpleName());
            default:
                return new FilePropsLocal(ConstantsFor.class.getSimpleName());
        }
    }
    
    static void setPreference(String prefName, String prefValue) {
        Preferences userPref = getUserPref();
        userPref.put(prefName, prefValue);
        try {
            userPref.flush();
            userPref.sync();
        }
        catch (BackingStoreException e) {
            System.err.println(MessageFormat.format("setPreference: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    static void reloadApplicationPropertiesFromFile() {
        File propsFile = new File(ConstantsFor.class.getSimpleName() + FileNames.EXT_PROPERTIES);
        try {
            Files.setAttribute(propsFile.toPath(), "dos:readonly", true);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override
    default MysqlDataSource getRegSourceForProperties() {
        MysqlDataSource retSource = DataConnectTo.getDefaultI().getDataSource();
        retSource.setDatabaseName(ConstantsFor.STR_VELKOM);
        return retSource;
    }
    
    @Scope(ConstantsFor.SINGLETON)
    static Preferences getUserPref() {
        Preferences prefsNeededNode = Preferences.userRoot();
        try {
            prefsNeededNode.flush();
            prefsNeededNode.sync();
        }
        catch (BackingStoreException e) {
            System.err.println(MessageFormat.format("getUserPref: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return prefsNeededNode;
    }
}
