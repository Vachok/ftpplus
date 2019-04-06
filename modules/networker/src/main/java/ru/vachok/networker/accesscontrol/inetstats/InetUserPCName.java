package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.stats.SaveLogsToDB;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class InetUserPCName implements InternetUse {


    private MessageToUser messageToUser = new MessageLocal(getClass().getSimpleName());

    @Override public String getUsage(String userCred) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InetAddress userAddr = InetAddress.getByName(userCred);
            stringBuilder.append(new InetIPUser().getUsage(userAddr.toString().split("/")[1]));
        }
        catch (UnknownHostException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".getUsage", e));
        }
        return stringBuilder.toString();
    }


    @Override public void showLog() {
        SaveLogsToDB saveLogsToDB = new AppComponents().saveLogsToDB();
        saveLogsToDB.showInfo();
    }
}
