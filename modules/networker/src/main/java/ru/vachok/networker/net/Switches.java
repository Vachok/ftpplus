package ru.vachok.networker.net;


import ru.vachok.networker.ConstantsFor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;

/**
 Свичи

 @since 20.12.2018 (10:14) */
public enum Switches {
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

    public static final String HP_2530_48_ADM_YURDEP = "10.200.213.253";

    public static final String WTF = "10.200.215.250";

    public static final String CONTROL = "192.168.13.13";

    public static String toStringS() {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            List<String> swListAsStr = DiapazonedScan.getInstance().pingSwitch();
            Collections.sort(swListAsStr);
            for (String s : swListAsStr) {
                s = s.replaceAll("\n", "");
                InetAddress inetAddress = InetAddress.getByName(s);
                byte[] addressBytes = inetAddress.getAddress();
                inetAddress = InetAddress.getByAddress(addressBytes);

                if (inetAddress.isReachable(500)) {
                    stringBuilder.append("<center>");
                    stringBuilder
                        .append("<font color=\"#00ff69\">")
                        .append(s)
                        .append("</font>");
                    stringBuilder.append(ConstantsFor.HTML_CENTER);
                } else {
                    stringBuilder.append("<center>");
                    stringBuilder
                        .append("<strike><font color=\"red\">")
                        .append(s)
                        .append("</font></strike>");
                    stringBuilder.append(ConstantsFor.HTML_CENTER);
                }
            }
        } catch (IllegalAccessException | IOException e) {
            stringBuilder.append(e.getMessage());
        }

        return stringBuilder.toString();
    }
}
