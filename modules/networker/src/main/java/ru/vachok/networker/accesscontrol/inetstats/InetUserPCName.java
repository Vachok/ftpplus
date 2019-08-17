// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.inetstats;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class InetUserPCName extends InternetUse {
    
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        return getUsage(aboutWhat);
    }
    
    private @NotNull String getUsage(String userCred) {
        InternetUse.aboutWhat = userCred;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InetAddress userAddr = InetAddress.getByName(userCred);
            stringBuilder.append(new InetIPUser().getUsage(userAddr.toString().split("/")[1]));
        }
        catch (UnknownHostException e) {
            stringBuilder.append(new InetUserUserName().getInfoAbout(userCred));
        }
        return stringBuilder.toString().replaceAll("юзер", ConstantsFor.RUSSTR_KOMPUTER);
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InetUserPCName{");
        sb.append('}');
        return sb.toString();
    }
}
