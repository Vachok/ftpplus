// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.TvPcInformation;
import ru.vachok.networker.net.monitor.PingerFromFile;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;


/**
 @see ru.vachok.networker.AbstractNetworkerFactoryTest
 @since 15.07.2019 (10:45) */
public abstract class AbstractNetworkerFactory {
    
    public static SSHFactory getSSHFactory(String srvName, String commandSSHToExecute, String classCaller) {
        SSHFactory.Builder builder = new SSHFactory.Builder(srvName, commandSSHToExecute, classCaller);
        return builder.build();
    }
    
    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal(AbstractNetworkerFactory.class.getSimpleName());
    
    @Contract(pure = true)
    public static @NotNull InformationFactory getInfoFactory() {
        MESSAGE_TO_USER.warn(InformationFactory.getRunningInformation());
        return new TvPcInformation();
    }
    
    @Contract(" -> new")
    public static @NotNull PingerFromFile netScanServiceFactory() {
        return new PingerFromFile();
    }
    
}
