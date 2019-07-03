// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.enums;


import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.exe.schedule.DiapazonScan;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

/**
 Свичи

 @since 20.12.2018 (10:14) */
public enum SwitchesWiFi {
    ;

    public static final String CORE_HP_3800 = "10.200.200.1";

    public static final String SB_PAVILION_MIKROTIK = "10.200.200.253";

    public static final String TD_HP_V1910_24G = "10.200.200.254";

    public static final String ATC_201_HP = "10.200.201.254";

    public static final String TECH_SLUJBA_HP = "10.200.202.254";

    public static final String ATC_201_HP_VLAN203 = "10.200.203.254";

    public static final String HP_2530_48_UPAKOVKA = "10.200.204.254";

    public static final String SKLAD_2_MXV_HP = "10.200.205.254";

    public static final String SKLAD_5_MIKROTIK = "10.200.206.254";

    public static final String HP_2530_24_G_POE_P_SK = "10.200.207.254";

    public static final String HP_2530_48_UBOY = "10.200.208.254";

    public static final String MKC_VLAN_210_HP = "10.200.210.254";

    public static final String PF_HP = "10.200.212.254";

    public static final String HP_2530_48_IT_OTDEL = "10.200.213.254";

    public static final String HOTEL_MIKROTIK = "10.200.216.254";

    public static final String HP_2530_48_1_FLOOR_VLAN_217 = "10.200.217.254";

    public static final String HP_2530_48_G_ADM_IT_2 = "10.200.213.251";

    public static final String HP_3500_2 = "10.200.217.251";

    public static final String HP_2530_24_G_ADM_YURDEP_2 = "10.200.213.252";

    public static final String HP_3500_YL_217_SCHETFAKT = "10.200.217.252";

    public static final String HP_2530_217 = "10.200.217.253";

    public static final String HP_2530_48_ADM_YURDEP = "10.200.213.253";

    public static final String WTF = "10.200.215.250";

    public static final String DIR_615_SB = "10.200.200.47";

    public static final String C_202_57_TECH = "10.200.202.57";

    public static final String C_203_2_STOLOVKA = "10.200.203.2";

    public static final String C_204_2_UPAK = "10.200.204.2";

    public static final String C_204_3_UPAK = "10.200.204.3";

    public static final String C_204_10_GP = "10.200.204.10";

    public static final String C_207_4_SK = "10.200.207.4";

    public static final String C_207_3_SK = "10.200.207.3";

    public static final String C_210_3_PF = "10.200.210.3";

    public static final String C_213_5_2FLOOR = "10.200.213.5";

    public static final String C_213_4_2FLOOR = "10.200.213.4";

    public static final String C_213_RV_2FLOOR = "10.200.213.104";

    public static final String C_213_6_2FLOOR = "10.200.213.6";

    public static final String WPG_1000 = "10.200.214.3";

    public static final String C_216_2_HOTEL = "10.200.216.2";

    public static final String C_216_3_HOTEL = "10.200.216.3";

    public static final String C_216_4_HOTEL = "10.200.216.4";
    
    public static final String HP_3500_2_SERVERNAYA = "10.1.1.111";
    
    public static final String HP_2610_24_SERVERNAYA = "10.1.1.228";
    
    /**
     IP srv-nat.eatmeat.ru
     */
    public static final String IPADDR_SRVNAT = "192.168.13.30";
    
    public static final String RUPSGATE = "rupsgate.eatmeat.ru";
    
    public static String toStringS() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            List<String> swListAsStr = DiapazonScan.pingSwitch();
            Collections.sort(swListAsStr);
            for (String s : swListAsStr) {
                s = s.replaceAll("\n", "");
                InetAddress inetAddress = InetAddress.getByName(s);
                byte[] addressBytes = inetAddress.getAddress();
                inetAddress = InetAddress.getByAddress(addressBytes);

                if (inetAddress.isReachable(500)) {
                    stringBuilder.append(ConstantsFor.HTMLTAG_CENTER);
                    stringBuilder
                        .append("<font color=\"#00ff69\">")
                        .append(s)
                        .append("</font>");
                    stringBuilder.append(ConstantsFor.HTML_CENTER_CLOSE);
                } else {
                    stringBuilder.append(ConstantsFor.HTMLTAG_CENTER);
                    stringBuilder
                        .append("<strike><font color=\"red\">")
                        .append(s)
                        .append("</font></strike>");
                    stringBuilder.append(ConstantsFor.HTML_CENTER_CLOSE);
                }
            }
        } catch (IllegalAccessException | IOException e) {
            stringBuilder.append(e.getMessage());
        }

        return stringBuilder.toString();
    }
}
