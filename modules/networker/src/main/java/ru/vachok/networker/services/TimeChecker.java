// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.services;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;

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
     {@link AppComponents#getLogger(String)}
     */
    private static final MessageToUser messageToUser = new MessageLocal(TimeChecker.class.getSimpleName());

    @Override
    public TimeInfo call() {
        AppComponents.threadConfig().thrNameSet("ntp");
        TimeInfo info = null;
        try {
            info = ntpCheck();
        
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return info;
    }
    
    private static TimeInfo ntpCheck() throws IOException {
        NTPUDPClient ntpudpClient = new NTPUDPClient();
        try {
            ntpudpClient.open();
        } catch (SocketException e) {
            messageToUser.errorAlert("TimeChecker", "ntpCheck", e.getMessage());
            FileSystemWorker.error("TimeChecker.ntpCheck", e);
        }
        TimeInfo ntpudpClientTime = null;
        try {
            ntpudpClientTime = ntpudpClient.getTime(InetAddress.getByName("rups00.eatmeat.ru"));
        } catch (IOException e) {
            ntpudpClientTime = ntpudpClient.getTime(InetAddress.getByName("time.windows.com"));
        }
        Objects.requireNonNull(ntpudpClientTime).computeDetails();
        ntpudpClient.close();
        return ntpudpClientTime;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeChecker{");
        sb.append("call=").append(call());
        sb.append('}');
        return sb.toString();
    }
}
