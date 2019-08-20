// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.enums;


import ru.vachok.networker.AppComponents;


/**
 @since 06.08.2019 (16:33) */
public enum PropertiesNames {
    ;
    
    public static final String PR_OSNAME_LOWERCASE = System.getProperty("os.name").toLowerCase();
    
    public static final String PR_THISPC = "thispc";
    
    public static final String PR_ENCODING = "encoding";
    
    public static final String PR_WINDOWSOS = "windows";
    
    public static final String PR_DBSTAMP = "dbstamp";
    
    public static final String PRSYS_SEPARATOR = "file.separator";
    
    public static final String PR_ADPHOTOPATH = "adphotopath";
    
    public static final String PR_DBUSER = "dbuser";
    
    public static final String PR_DBPASS = "dbpass";
    
    /**
     Property name: lastworkstart
     */
    public static final String PR_LASTWORKSTART = "lastworkstart";
    
    /**
     <i>Boiler Plate</i>
     */
    public static final String PR_PFSCAN = "pfscan";
    
    /**
     Название property
     */
    public static final String PR_APP_VERSION = "appVersion";
    
    /**
     Название property
     */
    public static final String PR_TOTPC = "totpc";
    
    public static final String PR_LASTS = "lasts";
    
    public static final String PR_ONLINEPC = "onlinepc";
    
    public static final String PR_APP_BUILD = "build";
    
    public static final String PR_APP_BUILDTIME = "buildTime";
    
    public static final String PR_VLANNUM = "vlanNum";
    
    public static final String PR_SCANSINMIN = "scansInMin";
    
    /**
     Название настройки.
     <p>
     pingsleep. Сколько делать перерыв в пингах. В <b>миллисекундах</b>.
     
     @see AppComponents#getProps()
     */
    public static final String PR_PINGSLEEP = "pingsleep";
    
    public static final String DEFPASSFTPMD5HASH = "defpassftpmd5hash";
    
    public static final String PRID_PASS = "general-pass";
    
    /**
     Название property
     */
    public static final String PR_LASTSCAN = "lastscan";
}
