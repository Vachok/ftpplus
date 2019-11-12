// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.database.DataConnectTo;

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
    
    @Contract(pure = true)
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    static @NotNull Properties getTheProps() {
        @NotNull Properties result = PropsHelper.getAppPr();
        return result;
    }
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    static @NotNull InitProperties getInstance(@NotNull String type) {
        switch (type) {
            case DB_MEMTABLE:
                return new MemoryProperties();
            case DB_LOCAL:
                return new DBPropsCallable(ConstantsFor.class.getSimpleName());
            default:
                return new FilePropsLocal(ConstantsFor.class.getSimpleName());
        }
    }
    
    @Override
    default MysqlDataSource getRegSourceForProperties() {
        MysqlDataSource retSource = DataConnectTo.getDefaultI().getDataSource();
        retSource.setDatabaseName("mem");
        return retSource;
    }
    
    static void setPreference(String prefName, String prefValue) {
        Preferences userPref = getUserPref();
        userPref.put(prefName, prefValue);
        try {
            userPref.sync();
        }
        catch (BackingStoreException e) {
            System.err.println(MessageFormat.format("getUserPref: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
    }
    
    static Preferences getUserPref() {
        return Preferences.userRoot();
    }
}
