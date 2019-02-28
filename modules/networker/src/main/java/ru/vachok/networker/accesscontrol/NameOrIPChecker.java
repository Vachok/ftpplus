package ru.vachok.networker.accesscontrol;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UnknownFormatConversionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 Проверяет пользовательский ввод на соотв. паттерну IP или имя ПК

 @since 03.12.2018 (16:42) */
public class NameOrIPChecker {


    private static final Pattern PATTERN_NAME = Pattern.compile("^(([aAdDTtNn])(([0-3])|([oOTtPp])))((\\d{2})|(\\d{4}))");

    private static final Pattern PATTERN_IP = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    private String userIn;

    private MessageToUser messageToUser = new MessageLocal();

    public NameOrIPChecker(String userIn) {
        this.userIn = userIn;
    }

    String checkPat(String userIn) {
        this.userIn = userIn;
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        if (mName.matches()) return userIn + ConstantsNet.DOMAIN_EATMEATRU;
        else if (mIP.matches()) {
            try {
                return resolveName(userIn);
            } catch (UnknownHostException e) {
                return e.getMessage();
            }
        } else throw new IllegalArgumentException();
    }

    InetAddress resolveIP() {
        InetAddress inetAddress = null;
        Matcher mName = PATTERN_NAME.matcher(userIn);
        Matcher mIP = PATTERN_IP.matcher(userIn);
        try {
            if (mIP.matches()) {
                byte[] addressBytes = InetAddress.getByName(userIn).getAddress();
                inetAddress = InetAddress.getByAddress(addressBytes);
            } else if (mName.matches()) {
                userIn = userIn + ConstantsNet.DOMAIN_EATMEATRU;
                inetAddress = InetAddress.getByName(userIn);
            } else {
                throw new UnknownFormatConversionException("Can't convert user input to Inet Address :(");
            }
        } catch (UnknownHostException e) {
            messageToUser.errorAlert("NameOrIPChecker", "resolveIP", e.getMessage());
            FileSystemWorker.error("NameOrIPChecker.resolveIP", e);
        }
        return inetAddress;
    }

    private String resolveName(String userIn) throws UnknownHostException {
        byte[] addressBytes = InetAddress.getByName(userIn).getAddress();
        InetAddress inetAddress = InetAddress.getByAddress(addressBytes);
        return inetAddress.getHostName();
    }
}
