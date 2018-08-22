package ru.vachok.money;


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
    private static boolean myPC;


    public static boolean isMyPC() {
        return myPC;
    }


    public static void setMyPC( boolean myPC ) {
        ConstantsFor.myPC = myPC;
    }

}
