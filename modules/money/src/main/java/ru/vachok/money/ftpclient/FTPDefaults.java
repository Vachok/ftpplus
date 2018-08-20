package ru.vachok.money.ftpclient;



import org.apache.commons.net.ftp.FTP;


/**
 * @since 20.08.2018 (23:10)
 */
public enum FTPDefaults {
    FTP_DEFAULTS;

    /**
     * IP камера в комнате
     */
    public static final String HOSTNAME = "10.10.111.52";

    /**
     * Порт {@link #HOSTNAME}
     */
    public static final int PORT = 50021;

    /**
     * имя пользователя
     */
    public static final String USER_NAME = "kudr";

    /**
     * пароль
     */
    public static final String DEF_PASSWORD = "36e42yoak8";

    /**
     * Тип передачи. Нужен для корректной загрузки.
     */
    public static final int MY_FILE_TYPE = FTP.BINARY_FILE_TYPE;
}
