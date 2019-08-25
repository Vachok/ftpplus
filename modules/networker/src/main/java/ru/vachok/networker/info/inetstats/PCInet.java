// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info.inetstats;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.info.InformationFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;


class PCInet extends InternetUse {
    
    
    InformationFactory userInfo;
    
    private String hostName;
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.hostName = aboutWhat;
        return this.getClass().getSimpleName();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.hostName = (String) classOption;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InetUserPCName{");
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("ru.vachok.networker.accesscontrol.inetstats.InetUserPCName.getInfo created 21.08.2019 (12:34)");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        PCInet inet = (PCInet) o;
        
        if (userInfo != null ? !userInfo.equals(inet.userInfo) : inet.userInfo != null) {
            return false;
        }
        return hostName != null ? hostName.equals(inet.hostName) : inet.hostName == null;
    }
    
    @Override
    public int hashCode() {
        int result = userInfo != null ? userInfo.hashCode() : 0;
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        return result;
    }
    
    private @NotNull String getUsage(String userCred) {
        this.userInfo = InformationFactory.getInstance(USER);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InetAddress userAddr = InetAddress.getByName(userCred);
            stringBuilder.append(userInfo.getInfoAbout(userAddr.toString().split("/")[1]));
        }
        catch (UnknownHostException e) {
            stringBuilder.append(new AccessLogUSER().getInfoAbout(userCred));
        }
        return stringBuilder.toString().replaceAll("юзер", ConstantsFor.RUSSTR_KOMPUTER);
    }
}
