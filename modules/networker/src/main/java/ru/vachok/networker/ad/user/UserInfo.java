package ru.vachok.networker.ad.user;


import ru.vachok.networker.accesscontrol.inetstats.AccessLog;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.info.InformationFactory;


public abstract class UserInfo implements InformationFactory {
    
    
    private InternetUse userInfo;
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.userInfo = new AccessLog();
        userInfo.setClassOption(aboutWhat);
        return userInfo.getInfo();
    }
    
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
