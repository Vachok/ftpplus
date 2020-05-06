// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.restapi.props;


import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.database.DataConnectTo;

import java.io.File;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 14.07.2019 (17:33) */
public interface InitProperties extends ru.vachok.mysqlandprops.props.InitProperties {


    String DB_MEMTABLE = "db";

    String FILE = "file";

    String DB_LOCAL = "srv-inetstat";

    String TEST = "test";

    String DB_REGRU = "regru";

    String FIREBASE = "firebase";

    @Contract(pure = true)
    @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
    @NotNull
    static Properties getTheProps() {
        @NotNull Properties result = PropsHelper.getAppPr();
        return result;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    @NotNull
    static InitProperties getInstance(@NotNull String type) {
        switch (type) {
            case FILE:
                FilePropsLocal filePr = new FilePropsLocal(ConstantsFor.class.getSimpleName());
                filePr.setPropFile(new File(FileNames.CONSTANTSFOR_PROPERTIES));
                return filePr;
            case DB_MEMTABLE:
                return new MemoryProperties();
            case DB_LOCAL:
                return new DBPropsCallable(ConstantsFor.class.getSimpleName());
            case DB_REGRU:
                return new RegRuProperties();
            case FIREBASE:
                return new FBProps();
            default:
                return new DBPropsCallable(type);
        }
    }

    @Contract(pure = true)
    static Properties getMAilPr() {
        Thread.currentThread().setName(InitProperties.class.getSimpleName());
        return PropsHelper.getMailPr();
    }

    @Override
    default MysqlDataSource getRegSourceForProperties() {
        MysqlDataSource retSource = DataConnectTo.getInstance(DataConnectTo.DEFAULT_I).getDataSource();
        retSource.setDatabaseName(ConstantsFor.STR_VELKOM);
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
