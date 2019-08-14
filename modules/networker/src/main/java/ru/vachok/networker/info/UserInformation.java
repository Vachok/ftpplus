package ru.vachok.networker.info;


import ru.vachok.networker.ad.user.ADUser;

import java.util.List;


/**
 @since 13.08.2019 (11:41) */
public interface UserInformation extends InformationFactory {
    
    
    List<ADUser> getADUsers();
    
    String getCurrentUserName(String pcName);
    
    @Override
    String getInfoAbout(String samAccountName);
    
    @Override
    void setClassOption(Object classOption);
}
