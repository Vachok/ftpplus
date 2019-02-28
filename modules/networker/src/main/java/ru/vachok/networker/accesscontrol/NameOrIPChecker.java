package ru.vachok.networker.accesscontrol;


import ru.vachok.networker.TForms;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Проверяет пользовательский ввод на соотв. паттерну IP или имя ПК

 @since 03.12.2018 (16:42) */
class NameOrIPChecker {


    private static final Pattern PATTERN_NAME = Pattern.compile("^(([aAdDTtNn])(([0-3])|([oOTtPp])))((\\d{2})|(\\d{4}))");

    private static final Pattern PATTERN_IP = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    String checkPat(String userIn) {
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        if (mName.matches()) return userIn;
        else if (mIP.matches()) {
            try {
                return resolveName(userIn);
            } catch (UnknownHostException e) {
                return e.getMessage();
            }
        } else throw new IllegalArgumentException();
    }

    private String resolveName(String userIn) throws UnknownHostException {

        InetAddress[] allByName = InetAddress.getAllByName(userIn + ConstantsNet.DOMAIN_EATMEATRU);
        List<InetAddress> inetAddresses = Arrays.asList(allByName);
        return new TForms().fromArray(inetAddresses, true);
    }
}
