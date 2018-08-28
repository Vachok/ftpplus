package ru.vachok.networker.web;


import javax.servlet.http.HttpServletRequest;
import java.io.File;


/**
 @since 12.08.2018 (16:26) */
public enum ConstantsFor {
   ;

   /**
    <b>1 мегабайт в байтах</b>
    */
   public static final int MBYTE = 1024 * 1024;

   public static final String DB_PREFIX = "u0466446_";

   public static final File SSH_ERR = new File("ssh_err.txt");

   public static final File SSH_OUT = new File("ssh_out.txt");

   public static final int TIMEOUT_2 = 2000;

   public static final String SRV_NAT = "192.168.13.30";

   public static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a"};
    public static final int NOPC = 50;
    public static final int PPPC = 70;
    public static final int DOPC = 250;
    public static final int APC = 350;
    public static final int TDPC = 15;

    public static final int TIMEOUT_650 = 650;


    public String getPC( HttpServletRequest request ) {
      return request.getRemoteAddr();
   }
}
