package ru.vachok.networker.restapi.props;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;


/**
 @since 10.11.2019 (17:08) */
class PropsHelper {


    private static final String ID_MAILREGRU = "mail-regru";

    private static final Properties APP_PR = new Properties();

    private static final Properties MAIL_PR = new Properties();

    private static final File PR_FILE = new File(FileNames.CONSTANTSFOR_PROPERTIES);

    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PropsHelper.class.getSimpleName());

    @Contract(pure = true)
    static Properties getMailPr() {
        Thread.currentThread().setName(PropsHelper.class.getSimpleName());
        if (MAIL_PR.size() > 0) {
            return MAIL_PR;
        }
        else {
            MAIL_PR.putAll(new DBPropsCallable(ID_MAILREGRU).getProps());
            InitProperties initProperties = new FilePropsLocal("mail");
            initProperties.setProps(MAIL_PR);
            return MAIL_PR;
        }
    }

    @Contract(pure = true)
    static Properties getAppPr() {
        boolean isSmallSize = APP_PR.size() < 9;
        boolean fileReadOnly = PR_FILE.exists() && !PR_FILE.canWrite();
        if (fileReadOnly) {
            APP_PR.putAll(InitProperties.getInstance(InitProperties.FILE).getProps());
            isSmallSize = APP_PR.size() < 9;
            if (!isSmallSize) {
                InitProperties.getInstance(InitProperties.DB_MEMTABLE).setProps(APP_PR);
            }
        }
        else if (isSmallSize) {
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
                reloadApplicationPropertiesFromFile();
            }
        }
        return APP_PR;
    }

    static void reloadApplicationPropertiesFromFile() {
        Properties fromApp = getAppPr();
        Properties propsFromFile = InitProperties.getInstance(InitProperties.FILE).getProps();
        fromApp.clear();
        fromApp.putAll(propsFromFile);
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

    @Override
    public String toString() {
        return new StringJoiner(",\n", PropsHelper.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}