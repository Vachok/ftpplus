package ru.vachok.networker.services;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import ru.vachok.networker.componentsrepo.AppComponents;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.Callable;

/**
 Сверка с сервером времени.
 <p>

 @since 04.12.2018 (16:42) */
public class TimeChecker implements Callable<TimeInfo> {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    @Override
    public TimeInfo call() {
        return ntpCheck();
    }

    private TimeInfo ntpCheck() {
        NTPUDPClient ntpudpClient = new NTPUDPClient();
        try {
            ntpudpClient.open();
        } catch (SocketException e) {
            LOGGER.warn(e.getMessage());
        }
        TimeInfo ntpudpClientTime = null;
        try {
            ntpudpClientTime = ntpudpClient.getTime(InetAddress.getByName("rups00.eatmeat.ru"));
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
        Objects.requireNonNull(ntpudpClientTime).computeDetails();
        long returnTime = ntpudpClientTime.getReturnTime() - System.currentTimeMillis();
        String msg = ntpudpClientTime.getMessage().toString() + " " + returnTime;
        ntpudpClient.close();
        LOGGER.info(msg);
        return ntpudpClientTime;
    }
}
