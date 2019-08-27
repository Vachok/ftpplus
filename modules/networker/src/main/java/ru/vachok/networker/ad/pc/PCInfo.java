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
import java.util.Properties;
import java.util.StringJoiner;


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
            return new UnknownPc();
        }
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCInfo.class.getSimpleName() + "[\n", "\n]")
                .toString();
    }
    
    static @NotNull String checkValidName(String pcName) {
        InetAddress inetAddress = new NameOrIPChecker(pcName).resolveInetAddress();
        String hostName = inetAddress.getHostName();
        return hostName.replaceAll(ConstantsFor.DOMAIN_EATMEATRU, "");
    }
    
}
