package ru.vachok.networker.web;



import java.io.File;


/**
 * @since 12.08.2018 (16:26)
 */
public enum ConstantsFor {
    ;
    /**
     * <b>1 мегабайт в байтах</b>
     */
    public static final int MBYTE = 1024 * 1024;
    public static final float USD_IN_14 = 34.26f;
    public static final float E_IN_14 = 46.9f;

    public static final File SSH_ERR = new File("ssh_err.txt");

    public static final File SSH_OUT = new File("ssh_out.txt");

    public static final int TIMEOUT_2 = 2000;

    public static final String SRV_NAT = "192.168.13.30";
    private boolean myPc;


    public void setPc( boolean b ) {
        this.myPc = b;
    }
}
