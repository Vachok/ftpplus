package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.OtherConstants;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 @since 10.11.2019 (17:08) */
class PropsHelper {
    
    
    private static final Properties APP_PR = new Properties();
    
    private static final Properties MAIL_PR = new Properties();
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PropsHelper.class.getSimpleName());
    
    @Contract(pure = true)
    static Properties getMailPr() {
        if (MAIL_PR.size() > 0) {
            return MAIL_PR;
        }
        else {
            MAIL_PR.putAll(new DBPropsCallable(PropertiesNames.ID_MAILREGRU).getProps());
            return MAIL_PR;
        }
    }
    
    public static void reloadApplicationPropertiesFromFile() {
        File propsFile = new File(FileNames.CONSTANTSFOR_PROPERTIES);
        Properties fromApp = getAppPr();
        try {
            Files.setAttribute(propsFile.toPath(), OtherConstants.READONLY, true);
            Properties propsFromFile = InitProperties.getInstance(InitProperties.FILE).getProps();
            fromApp.clear();
            fromApp.putAll(propsFromFile);
            Files.setAttribute(propsFile.toPath(), OtherConstants.READONLY, false);
        }
        catch (IOException e) {
            System.err.println(MessageFormat.format("Reloading properties from file {0} error: {1}", FileNames.CONSTANTSFOR_PROPERTIES, e.getMessage()));
        }
        
    }
    
    @Contract(pure = true)
    static Properties getAppPr() {
        boolean isSmallSize = APP_PR.size() < 9;
        if (isSmallSize) {
            new PropsHelper().loadPropsFromDB();
            isSmallSize = APP_PR.size() < 9;
            if (isSmallSize) {
                APP_PR.putAll(new DBPropsCallable().call());
                isSmallSize = APP_PR.size() < 9;
            }
            if (!isSmallSize) {
                InitProperties.getInstance(InitProperties.FILE).setProps(APP_PR);
                copyPropsToPref();
            }
            else {
                messageToUser.error("PropsHelper.getAppPr", "Can't load properties", AbstractForms.networkerTrace(Thread.currentThread().getStackTrace()));
            }
        }
        return APP_PR;
    }
    
    private void loadPropsFromDB() {
        InitProperties initProperties = InitProperties.getInstance(InitProperties.DB_MEMTABLE);
        APP_PR.putAll(initProperties.getProps());
        APP_PR.setProperty(PropertiesNames.DBSTAMP, String.valueOf(System.currentTimeMillis()));
        APP_PR.setProperty(PropertiesNames.THISPC, UsefulUtilities.thisPC());
    }
    
    private static void copyPropsToPref() {
        Set<Map.Entry<Object, Object>> entries = APP_PR.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            InitProperties.setPreference(entry.getKey().toString(), entry.getValue().toString());
        }
    }
    
    
}