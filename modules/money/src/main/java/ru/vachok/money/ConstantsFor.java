package ru.vachok.money;


import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @since 20.08.2018 (11:31)
 */
@SuppressWarnings("NonFinalFieldInEnum")
public enum ConstantsFor {
    ;

   /**
    *  Кол-во байт в мегабайте
    */
    public static final int MEGABYTE = 1024 * 1024;

   /**
    * Название приложения, для поиска properties
    */
   public static final String APP_NAME = ConstantsFor.class.getPackage().getName().replaceAll("\\Q.\\E" , "_") + "-";

   /**
    * Кол-во байт в килобайте
    */
   public static final int KILOBYTE = 1024;
    public static final int YEAR_BIRTH = 1984;
    public static final float FILES_TO_ENC_BLOCK = 111.0f;
    public static final String DB_PREFIX = "u0466446_";
    public static final int MONTH_BIRTH = 1;
    public static final int DAY_OF_B_MONTH = 7;
    public static final double NRIGA = 32.2;
    public static final double A107 = 21.6;
    public static final int INITIAL_DELAY = 30;
    public static final int DELAY = 300;
    public static String scheduleSpeedAct;
    private static boolean myPC;



    public static boolean isMyPC() {
       try{
          myPC = InetAddress.getLocalHost().getHostAddress().contains("10.10.111");
       }
       catch(UnknownHostException e){
          ApplicationConfiguration.getLogger().error(ConstantsFor.class.getSimpleName());
       }
       return myPC;
    }


    public static void setMyPC( boolean myPC ) {
        ConstantsFor.myPC = myPC;
    }

}
