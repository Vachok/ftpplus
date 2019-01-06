package ru.vachok.money.services;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.filesys.FileSysWorker;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;


/**
 @since 04.12.2018 (20:21) */
public class TimeChecker implements Callable<TimeInfo> {

    /*Fields*/
    private static final Logger LOGGER = AppComponents.getLogger();


    @Override
    public TimeInfo call() throws Exception {
        return ntpCheck();
    }

    /**
     Получаем время от time.windows.com
     <p>

     @return {@link TimeInfo}
     @throws IOException {@link InetAddress} unreachable.
     */
    private TimeInfo ntpCheck() throws IOException {
        NTPUDPClient ntpudpClient = new NTPUDPClient();
        TimeInfo atomTime = ntpudpClient.getTime(InetAddress.getByName("time.windows.com"));
        atomTime.computeDetails();
        String msg = atomTime.getMessage().toString();
        LOGGER.info(msg);
        return atomTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeChecker{");
        try{
            sb.append("call=").append(call().getMessage());
        }
        catch(Exception e){
            FileSysWorker.writeFile(e.getMessage(), new TForms().toStringFromArray(e, false));
        }
        sb.append('}');
        return sb.toString();
    }
}