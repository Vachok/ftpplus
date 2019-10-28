// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.StringJoiner;
import java.util.UnknownFormatConversionException;


/**
 @see ru.vachok.networker.ad.pc.PCInfoTest
 @since 13.08.2019 (17:15) */
public abstract class PCInfo implements InformationFactory {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCInfo.class.getSimpleName());
    
    private static String pcName;
    
    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Contract("_ -> new")
    public static @NotNull PCInfo getInstance(@NotNull String aboutWhat) {
        PCInfo.pcName = aboutWhat;
        if (aboutWhat.equals(InformationFactory.TV)) {
            return new TvPcInformation();
        }
        else {
            if (NetScanService.isReach(aboutWhat) && new NameOrIPChecker(aboutWhat).isLocalAddress()) {
                UserInfo.renewOffCounter(pcName, false);
                return new PCOn(aboutWhat);
            }
            else if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
                UserInfo.renewOffCounter(pcName, true);
                return new PCOff(aboutWhat);
            }
            else if (aboutWhat.equals(PCOff.class.getSimpleName())) {
                UserInfo.renewOffCounter(pcName, true);
                return new PCOff();
            }
            else {
                return new UnknownPc(aboutWhat);
            }
        }
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    public static @NotNull String checkValidNameWithoutEatmeat(@NotNull String pcName) {
        PCInfo.pcName = pcName;
        @NotNull String result = "null";
        boolean finished = false;
        InetAddress inetAddress = null;
        if (pcName.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            try {
                inetAddress = InetAddress.getByAddress(InetAddress.getByName(pcName).getAddress());
                result = inetAddress.getHostName();
                finished = true;
            }
            catch (UnknownHostException e) {
                messageToUser.error(e.getMessage() + " see line: 58 ***");
            }
        }
        else if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        if (!finished) {
            result = notFinished(inetAddress, pcName);
        }
        return result;
    }
    
    private static String notFinished(InetAddress inetAddress, @NotNull String pcName) {
        @NotNull String result = "null";
        boolean finished = false;
        try {
            inetAddress = new NameOrIPChecker(pcName).resolveInetAddress();
        }
        catch (UnknownFormatConversionException e) {
            result = new UnknownPc(PCInfo.class.getSimpleName()).getInfoAbout(pcName);
            finished = true;
        }
        if (!finished) {
            if (inetAddress.equals(InetAddress.getLoopbackAddress())) {
                result = new UnknownPc(PCInfo.class.toString()).getInfoAbout(pcName);
            }
            else {
                String hostName = inetAddress.getHostName();
                result = hostName.replaceAll(ConstantsFor.DOMAIN_EATMEATRU, "");
            }
        }
        return result;
    }
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCInfo.class.getSimpleName() + "[\n", "\n]").add(defaultInformation(pcName, NetScanService.isReach(pcName)))
                .toString();
    }
    
    @Override
    public abstract void setClassOption(Object option);
    
    protected static @NotNull String addToMap(String pcName, String ipAddr) {
        return addToMap(pcName, ipAddr, false, ConstantsFor.OFFLINE);
    }
    
    protected static String addToMap(String pcName, String ipAddr, boolean isOnline, String userName) {
        String stringToAdd = pcName + ":" + ipAddr + " online " + isOnline + "<" + userName;
        try {
            NetKeeper.getPcNamesForSendToDatabase().add(stringToAdd);
        }
        catch (UnknownFormatConversionException e) {
            stringToAdd = MessageFormat.format(PCInfo.class.getSimpleName(), e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace()));
        }
        return stringToAdd;
    }
    
    static @NotNull String defaultInformation(String pcName, boolean isOnline) {
        String retStr;
        if (isOnline) {
            retStr = MessageFormat.format("{0}. {1} online true <br>", pcName, new PCOn(pcName).pcNameWithHTMLLink());
        }
        else {
            retStr = MessageFormat.format("{0}. {1}", pcName, new PCOff(pcName).getInfo()) + "<br>";
        }
        return retStr;
    }
}
