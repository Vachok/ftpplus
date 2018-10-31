package ru.vachok.money.calc;


import java.security.SecureRandom;


/**
 @since 31.10.2018 (19:38) */
public class ChooseYouDestiny {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ChooseYouDestiny.class.getSimpleName();

    String destinyCooser(String[] strings) {
        int secureRandom = new SecureRandom().nextInt(strings.length);
        return strings[secureRandom];
    }
}