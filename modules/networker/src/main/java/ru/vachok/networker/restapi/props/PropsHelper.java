package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.PropertiesNames;

import java.util.Properties;


/**
 @since 10.11.2019 (17:08) */
class PropsHelper {
    
    
    private static final Properties APP_PR = new Properties();
    
    @Contract(pure = true)
    static Properties getAppPr() {
        return APP_PR;
    }
    
    void loadPropsFromDB() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_LOCAL);
        APP_PR.putAll(initProperties.getProps());
        try {
            initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        }
        catch (RuntimeException e) {
            initProperties = InitProperties.getInstance(InitProperties.FILE);
        }
        finally {
            APP_PR.setProperty(PropertiesNames.DBSTAMP, String.valueOf(System.currentTimeMillis()));
            APP_PR.setProperty(PropertiesNames.THISPC, UsefulUtilities.thisPC());
        }
    }
}