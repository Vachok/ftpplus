// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.InternetUse;
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
            messageToUser.error(new TForms().fromArray(e, false));
        }
        int deletedRows = cleanTrash();
        messageToUser.info("clients1", "deletedRows", " = " + deletedRows);
        return stringBuilder.toString();
    }
    
    
    @Override public void showLog() {
        SaveLogsToDB saveLogsToDB = new AppComponents().saveLogsToDB();
        saveLogsToDB.showInfo();
    }
}
