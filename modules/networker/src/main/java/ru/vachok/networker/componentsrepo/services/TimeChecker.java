// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.services;


import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.data.enums.OtherKnownDevices;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Callable;


/**
 Сверка с сервером времени.
 <p>

 @since 04.12.2018 (16:42) */
public class TimeChecker implements Callable<TimeInfo> {


    private static final MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, TimeChecker.class.getSimpleName());

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TimeChecker{");
        sb.append("call=").append(new TForms().fromArray(call().getComments()));
        sb.append('}');
        return sb.toString();
    }

    @Override
    public TimeInfo call() {
        Thread.currentThread().setName("TimeChecker.call");
        TimeInfo info = null;
        try {
            info = ntpCheck();

        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
        }
        return info;
    }

    private TimeInfo ntpCheck() throws IOException {
        if (UsefulUtilities.thisPC().contains("mint")) {
            throw new IllegalArgumentException(UsefulUtilities.getRunningInformation());
        }
        NTPUDPClient ntpudpClient = new NTPUDPClient();
        String methName = "ntpCheck";
        try {
            ntpudpClient.open();
        }
        catch (SocketException e) {
            messageToUser.warn(TimeChecker.class.getSimpleName(), e.getMessage(), " see line: 64 ***");
            FileSystemWorker.error("TimeChecker.ntpCheck", e);
        }
        TimeInfo ntpudpClientTime;
        try {
            ntpudpClientTime = ntpudpClient.getTime(InetAddress.getByName(OtherKnownDevices.SRV_RUPS00));
            ntpudpClientTime.computeDetails();
            messageToUser.info(this.getClass().getName(), methName, String.valueOf(ntpudpClientTime.getMessage()));
        }
        catch (IOException e) {
            ntpudpClientTime = ntpudpClient.getTime(InetAddress.getByName("time.windows.com"));
            messageToUser.warn(TimeChecker.class.getSimpleName(), e.getMessage(), " see line: 75 ***");
        }
        finally {
            ntpudpClient.close();
        }
        return ntpudpClientTime;
    }
}
