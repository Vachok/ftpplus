// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.MessageToUser;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class InetUserPCName extends InternetUse {
    
    @Override public String getUsage(String userCred) {
        InternetUse.aboutWhat = userCred;
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
    
    @Override
    public void setClassOption(Object classOption) {
        MessageToUser messageToUser = (MessageToUser) classOption;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InetUserPCName{");
        sb.append('}');
        return sb.toString();
    }
}
