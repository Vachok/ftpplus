package ru.vachok.ostpst.fileworks;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.ostpst.ConstantsOst;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 @since 16.05.2019 (11:48) */
public interface FileWorker {
    
    
    static final Map<String, String> PREF_MAP = new HashMap<>();
    
    static final Preferences PREFERENCES_USER_ROOT = Preferences.userRoot();
    
    /**
     Checker for file.
     <p>
     Checks the working file
     
     @return file is ok, or not
     */
    String chkFile();
    
    String clearCopy();
    
    long continuousCopy();
    
    void showCurrentResult();
    
    String saveAndExit();
    
    boolean processNewCopy();
    
    /**
     Initial method for copy/upload classes.
     <p>
     Based on {@link Preferences#userRoot()} or {@link DBRegProperties}
     
     @param writeFileName name of <b>copy</b> file.
     @throws IllegalStateException if copy file length is different reading file length
     */
    default void initMethod(String writeFileName) throws IllegalStateException {
        try {
            PREFERENCES_USER_ROOT.sync();
            PREF_MAP.putIfAbsent(ConstantsOst.PR_READFILENAME, PREFERENCES_USER_ROOT.get(ConstantsOst.PR_READFILENAME, ""));
            PREF_MAP.putIfAbsent(ConstantsOst.PR_WRITEFILENAME, PREFERENCES_USER_ROOT.get(ConstantsOst.PR_WRITEFILENAME, ""));
    
            long writeLen = new File(writeFileName).length();
            PREF_MAP.putIfAbsent(ConstantsOst.PR_POSWRITE, String.valueOf(writeLen));
            PREF_MAP.putIfAbsent(ConstantsOst.PR_POSREAD, String.valueOf(writeLen));
        }
        catch (BackingStoreException e) {
            tryingProperties(writeFileName);
        }
        
        String poRead = PREF_MAP.get(ConstantsOst.PR_POSREAD);
        String poWrite = PREF_MAP.get(ConstantsOst.PR_POSWRITE);
        if (!(poRead.equals(poWrite))) {
            String clearCopy = clearCopy();
            System.err.println("!(" + poRead + ".equals(" + poWrite + ")) " + clearCopy);
        }
    }
    default void tryingProperties(String writeFileName) {
        InitProperties initProperties = new DBRegProperties(ConstantsOst.APPNAME_OSTPST + getClass().getSimpleName());
        Properties properties = initProperties.getProps();
        
        PREF_MAP.putIfAbsent(ConstantsOst.PR_READFILENAME, properties.getProperty(ConstantsOst.PR_READFILENAME, ""));
        PREF_MAP.putIfAbsent(ConstantsOst.PR_WRITEFILENAME, String.valueOf(new File(writeFileName).length()));
        PREF_MAP.putIfAbsent(ConstantsOst.PR_POSWRITE, properties.getProperty(ConstantsOst.PR_POSWRITE, String.valueOf(0)));
        PREF_MAP.putIfAbsent(ConstantsOst.PR_POSREAD, properties.getProperty(ConstantsOst.PR_POSREAD, String.valueOf(0)));
    }
    
}
