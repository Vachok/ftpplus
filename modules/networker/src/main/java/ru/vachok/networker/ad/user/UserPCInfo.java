// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import ru.vachok.networker.componentsrepo.exceptions.TODOException;

import java.util.StringJoiner;


/**
 @see ru.vachok.networker.ad.user.UserPCInfo
 @since 02.04.2019 (10:25) */
public class UserPCInfo extends UserInfo {
    
    
    public String getUsage(String userCred) {
        throw new TODOException("21.08.2019 (12:50)");
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        throw new TODOException("ru.vachok.networker.ad.user.UserPCInfo.getInfoAbout created 21.08.2019 (12:29)");
    }
    
    @Override
    public void setClassOption(Object classOption) {
        throw new TODOException("ru.vachok.networker.ad.user.UserPCInfo.setClassOption created 21.08.2019 (12:47)");
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("ru.vachok.networker.ad.user.UserPCInfo.getInfo created 21.08.2019 (12:29)");
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UserPCInfo.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
