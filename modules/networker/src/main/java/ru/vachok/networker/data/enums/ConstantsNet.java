// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.data.enums;


import org.jetbrains.annotations.Contract;
import ru.vachok.networker.restapi.props.InitProperties;


/**
 Константы пакета
 <p>

 @since 25.01.2019 (10:30) */
@SuppressWarnings("NonFinalFieldInEnum")
public enum ConstantsNet {
    ;

    public static final int TDPC = 15;

    /**
     {@link ConstantsFor#DBPREFIX} + velkom
     */
    public static final String DB_NAME = ConstantsFor.DBBASENAME_U0466446_VELKOM;

    public static final int APC = 350;

    public static final int DOPC = 250;

    public static final int PPPC = 70;

    /**
     Кол-во ноутов NO
     */
    public static final int NOPC = 50;

    public static final String STR_COMPNAME_USERS_MAP_SIZE = " COMPNAME_USERS_MAP size";

    public static final String ONLINE_NOW = "OnlineNow";

    public static final int DOTDPC = 50;

    public static final int NOTDPC = 50;

    public static final int MAX_IN_ONE_VLAN = 255;

    public static final int IPS_IN_VELKOM_VLAN = Integer.parseInt(InitProperties.getTheProps().getProperty(PropertiesNames.VLANNUM, "59")) * MAX_IN_ONE_VLAN;

    private static final String[] PC_PREFIXES = {"do", "pp", "td", "no", "a", "dotd", "notd"};

    private static String sshMapStr = "SSH Temp list is empty";

    /**
     Префиксы имён ПК Велком.

     @return {@link #PC_PREFIXES}
     */
    @Contract(pure = true)
    public static String[] getPcPrefixes() {
        return PC_PREFIXES;
    }

    @Contract(pure = true)
    public static String getSshMapStr() {
        return sshMapStr;
    }

    public static void setSshMapStr(String sshMapStr) {
        ConstantsNet.sshMapStr = sshMapStr;
    }
}
