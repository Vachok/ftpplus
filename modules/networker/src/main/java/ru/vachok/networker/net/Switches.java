package ru.vachok.networker.net;


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


    public static String toStringS() {
        final StringBuilder sb = new StringBuilder("<center>Switches{<br>");
        sb.append("ATC_201_HP=").append(ATC_201_HP).append("<br>");
        sb.append("ATC_201_HP_VLAN203=").append(ATC_201_HP_VLAN203).append("<br>");
        sb.append("CORE_HP_3800=").append(CORE_HP_3800).append("<br>");
        sb.append("HOTEL_MIKROTIK=").append(HOTEL_MIKROTIK).append("<br>");
        sb.append("HP_2530_24_G_ADM_YURDEP_2=").append(HP_2530_24_G_ADM_YURDEP_2).append("<br>");
        sb.append("HP_2530_24_G_PO_EP_SK=").append(HP_2530_24_G_POE_P_SK).append("<br>");
        sb.append("HP_2530_48_1_FLOOR_VLAN_217=").append(HP_2530_48_1_FLOOR_VLAN_217).append("<br>");
        sb.append("HP_2530_48_ADM_YURDEP=").append(HP_2530_48_ADM_YURDEP).append("<br>");
        sb.append("HP_2530_48_G_ADM_IT_2=").append(HP_2530_48_G_ADM_IT_2).append("<br>");
        sb.append("HP_2530_48_IT_OTDEL=").append(HP_2530_48_IT_OTDEL).append("<br>");
        sb.append("HP_2530_48_UBOY=").append(HP_2530_48_UBOY).append("<br>");
        sb.append("HP_2530_48_UPAKOVKA=").append(HP_2530_48_UPAKOVKA).append("<br>");
        sb.append("HP_3500_2=").append(HP_3500_2).append("<br>");
        sb.append("HP_3500_YL_217_SCHETFAKT=").append(HP_3500_YL_217_SCHETFAKT).append("<br>");
        sb.append("MKC_VLAN_210_HP=").append(MKC_VLAN_210_HP).append("<br>");
        sb.append("PF_HP=").append(PF_HP).append("<br>");
        sb.append("SB_PAVILION_MIKROTIK=").append(SB_PAVILION_MIKROTIK).append("<br>");
        sb.append("SKLAD_2_MXV_HP=").append(SKLAD_2_MXV_HP).append("<br>");
        sb.append("SKLAD_5_MIKROTIK=").append(SKLAD_5_MIKROTIK).append("<br>");
        sb.append("TD_HP_V1910_24G=").append(TD_HP_V1910_24G).append("<br>");
        sb.append("TECH_SLUJBA_HP=").append(TECH_SLUJBA_HP).append("<br>");
        sb.append("<font color=\"#ff77fc\">WTF=").append(WTF).append("</font><br>");
        sb.append("}</center>");
        return sb.toString();
    }}
