// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
    
    @Contract("_ -> new")
    public static @NotNull PCInfo getInstance(@NotNull String aboutWhat) {
        if (aboutWhat.equals(InformationFactory.TV)) {
            return new TvPcInformation();
        }
        else {
            if (NetScanService.isReach(aboutWhat) && new NameOrIPChecker(aboutWhat).isLocalAddress()) {
                return new PCOn(aboutWhat);
            }
            else if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
                return new PCOff(aboutWhat);
            }
            else if (aboutWhat.equals(PCOff.class.getSimpleName())) {
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
        InetAddress inetAddress;
        if (pcName.matches(String.valueOf(ConstantsFor.PATTERN_IP))) {
            try {
                inetAddress = InetAddress.getByAddress(InetAddress.getByName(pcName).getAddress());
                return inetAddress.getHostName();
            }
            catch (UnknownHostException e) {
                messageToUser.error(e.getMessage() + " see line: 58 ***");
            }
        }
        else if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
    
        try {
            inetAddress = new NameOrIPChecker(pcName).resolveInetAddress();
        }
        catch (UnknownFormatConversionException e) {
            return new UnknownPc(PCInfo.class.getSimpleName()).getInfoAbout(pcName);
        }
        if (inetAddress.equals(InetAddress.getLoopbackAddress())) {
            return new UnknownPc(PCInfo.class.toString()).getInfoAbout(pcName);
        }
        else {
            String hostName = inetAddress.getHostName();
            return hostName.replaceAll(ConstantsFor.DOMAIN_EATMEATRU, "");
        }
    }
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    @Override
    public abstract void setClassOption(Object option);
    
    static @NotNull String defaultInformation(String pcName, boolean isOnline) {
        DBPCHTMLInfo dbpchtmlInfo = new DBPCHTMLInfo(pcName);
        String retStr;
        
        if (isOnline) {
            retStr = MessageFormat.format("{0}. {1} online true <br>", pcName, new PCOn(pcName).pcNameWithHTMLLink());
        }
        else {
            retStr = MessageFormat.format("{0}. {1}", dbpchtmlInfo.fillWebModel()) + "<br>";
        }
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(retStr, isOnline);
        return retStr;
    }
}
