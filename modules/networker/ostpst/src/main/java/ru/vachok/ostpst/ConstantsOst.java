// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


/**
 @since 02.05.2019 (19:52) */
public enum ConstantsOst {
    ;
    
    public static final String FILENAME_PROPERTIES = "app.properties";
    
    public static final String PR_READING = "reading";
    
    public static final String PR_WRITING = "write";
    
    public static final String PR_CAPACITY = "capacity";
    
    public static final int KBYTE_BYTES = 1024;
    
    public static final String SYSTEM_SEPARATOR = getSeparator();
    
    public static final String CP_WINDOWS_1251 = "windows-1251";
    
    public static final String FILENAME_CONTACTSCSV = "contacts.csv";
    
    public static final String FILENAME_FOLDERSTXT = "folders.txt";
    
    public static final String PR_CAPFLOOR = "capfloor";
    
    public static final String PR_TMPFILE = "tmpfile";
    
    public static final String STR_NOT_READY_YET = "Not ready yet";
    
    public static final String FILENAME_TESTPST = "tmp_t.p.magdich.pst";
    
    public static final String FILE_PREFIX_SEARCH_ = "search_";
    
    public static final String STR_ATTACHMENTS = "attachments";
    
    public static final String PROGNAME_OSTPST = "ostpst";
    
    private static String getSeparator() {
        if (System.getProperty("os.name").contains("indows")) {
            return "\\";
        }
        else {
            return "/";
        }
    }
}
