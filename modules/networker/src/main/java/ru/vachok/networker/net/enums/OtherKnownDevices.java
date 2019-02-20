package ru.vachok.networker.net.enums;


/**
 Прочие девайсы
 <p>
 Напр. мобильные

 @since 14.02.2019 (23:29) */
public enum OtherKnownDevices {

    PTV1,
    PTV2,
    ;

    /**
     Мой мобильный
     */
    public static final String MOB_KUDR = "10.200.214.80";

    public static final OtherKnownDevices[] DEVICES = OtherKnownDevices.values();

    public static final String PTV1_EATMEAT_RU = "ptv1.eatmeat.ru";

    public static final String PTV2_EATMEAT_RU = "ptv2.eatmeat.ru";}
