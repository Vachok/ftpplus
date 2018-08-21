package ru.vachok.money;


/**
 * @since 20.08.2018 (11:31)
 */
public enum ConstantsFor {
    ;

   /**
    *  Кол-во байт в мегабайте
    */
    public static final int MEGABYTE = 1024 * 1024;

   /**
    * Название приложения, для поиска properties
    */
    public static final String APP_NAME = ConstantsFor.class.getPackage().getName() + "-";

   /**
    * Кол-во байт в килобайте
    */
   public static final int KILOBYTE = 1024;
}
