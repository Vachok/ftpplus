// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.exe.runnabletasks.external.SaveLogsToDB;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.internetuse.InternetUse;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;


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
    public String getConnectStatistics(String userCred) {
        InetAddress inetAddress = InetAddress.getLoopbackAddress();
        try {
            
            inetAddress = new NameOrIPChecker(userCred).resolveIP();
        }
        catch (UnknownHostException e) {
            messageToUser.error(MessageFormat.format("InetUserPCName.getResponseTime: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        return new InetIPUser().getConnectStatistics(inetAddress.getHostAddress());
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InetUserPCName{");
        sb.append('}');
        return sb.toString();
    }
}
