// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.info.InformationFactory;


public abstract class UserInfo implements InformationFactory {
    
    
    private InternetUse userInfo;
    
    @Contract(" -> new")
    public static @NotNull InformationFactory getI() {
        return new UserPCInfo();
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public abstract String getInfo();
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserInfo{");
        sb.append("userInfo=").append(userInfo);
        sb.append('}');
        return sb.toString();
    }
}
