// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.data.enums;


import ru.vachok.networker.restapi.props.InitProperties;


/**
 @since 06.08.2019 (16:33) */
public enum PropertiesNames {
    ;

    public static final String SYS_OSNAME_LOWERCASE = System.getProperty("os.name").toLowerCase();

    public static final String THISPC = "thispc";

    public static final String ENCODING = "encoding";

    public static final String WINDOWSOS = "windows";

    public static final String DBSTAMP = "dbstamp";

    public static final String SYS_SEPARATOR = "file.separator";

    public static final String ADPHOTOPATH = "adphotopath";

    public static final String DBUSER = "dbuser";

    public static final String DBPASS = "dbpass";

    /**
     Property name: lastworkstart
     */
    public static final String LASTWORKSTART = "lastworkstart";

    /**
     <i>Boiler Plate</i>
     */
    public static final String PFSCAN = "pfscan";

    /**
     Название property
     */
    public static final String APPVERSION = "appVersion";

    /**
     Название property
     */
    public static final String TOTPC = "totpc";

    public static final String LASTS = "lasts";

    public static final String ONLINEPC = "onlinepc";

    public static final String BUILD = "build";

    public static final String BUILDTIME = "buildTime";

    public static final String VLANNUM = "vlanNum";

    public static final String SCANSINMIN = "scansInMin";

    /**
     Название настройки.
     <p>
     pingsleep. Сколько делать перерыв в пингах. В <b>миллисекундах</b>.

     @see InitProperties#getTheProps()
     */
    public static final String PINGSLEEP = "pingsleep";

    public static final String DEFPASSFTPMD5HASH = "defpassftpmd5hash";

    public static final String PROPERTIESID_GENERAL_PASS = "general-pass";

    /**
     Название property
     */
    public static final String LASTSCAN = "lastscan";

    public static final String NEXTSCAN = "nextpcscan";

    public static final String ERROR = "Error";

    public static final String REALFTPPASS = "realftppass";

    public static final String TIMESTAMP = "timestamp";

    public static final String MINDELAY = "mindelay";

    public static final String PASSWORD = "password";

    public static final String CLASS = "class";

    public static final String JAVA_VERSION = "java.version";

    public static final String ID = "id";

    public static final String COMPUTERNAME = "COMPUTERNAME";
}
