// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.net.NetScanService;

import java.net.InetAddress;
import java.util.*;


/**
 @see ru.vachok.networker.ad.pc.PCInfoTest
 @since 13.08.2019 (17:15) */
public abstract class PCInfo implements InformationFactory {
    
    
    static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    @Contract("_ -> new")
    public static @NotNull PCInfo getInstance(@NotNull String aboutWhat) {
        if (aboutWhat.equals(InformationFactory.TV)) {
            return new TvPcInformation();
        }
        else if (NetScanService.isReach(aboutWhat) && new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            return new PCOn(aboutWhat);
        }
        else if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            return new PCOff(aboutWhat);
        }
        else {
            return new UnknownPc(aboutWhat);
        }
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    public static @NotNull String checkValidNameWithoutEatmeat(@NotNull String pcName) {
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        InetAddress inetAddress;
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
    public abstract void setOption(Object option);
    
}
