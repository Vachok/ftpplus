// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.networker.TForms;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.internetuse.InternetUse;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class InetUserPCName extends SaveLogsToDB implements InternetUse {
    
    
    private MessageToUser messageToUser = new MessageLocal(this.getClass().getSimpleName());
    
    
    @Override public String getUsage(String userCred) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InetAddress userAddr = InetAddress.getByName(userCred);
            stringBuilder.append(new InetIPUser().getUsage(userAddr.toString().split("/")[1]));
        }
        catch (UnknownHostException e) {
            stringBuilder.append(new TForms().fromArray(e, false));
        }
        return stringBuilder.toString();
    }
    
    
    @Override public void showLog() {
        int cleanTrash = cleanTrash();
        messageToUser.info(this.getClass().getSimpleName(), CLEANED, String.valueOf(cleanTrash));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InetUserPCName{");
        sb.append('}');
        return sb.toString();
    }
}
