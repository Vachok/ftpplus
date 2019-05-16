// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst;


/**
 @since 02.05.2019 (19:52) */
public enum ConstantsFor {
    ;
    
    public static final String FILENAME_PROPERTIES = "app.properties";
    
    public static final String PR_READING = "reading";
    
    public static final String PR_WRITING = "write";
    
    public static final String PR_CAPACITY = "capacity";
    
    public static final int KBYTE_BYTES = 1024;
    
    public static final String SYSTEM_SEPARATOR = getSeparator();
    
    private static String getSeparator() {
        if (System.getProperty("os.name").contains("indows")) {
            return "\\";
        }
        else {
            return "/";
        }
    }
}
