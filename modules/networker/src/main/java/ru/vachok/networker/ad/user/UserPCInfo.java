// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.info.InformationFactory;

import java.text.MessageFormat;
import java.util.StringJoiner;


/**
 @see ru.vachok.networker.ad.user.UserPCInfo
 @since 02.04.2019 (10:25) */
public class UserPCInfo extends UserInfo {
    
    
    private Object aboutWhat;
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
    
    public String getUsage(String userCred) {
        throw new TODOException("21.08.2019 (12:50)");
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        return getPCByUserName();
    }
    
    private @NotNull String getPCByUserName() {
        return MessageFormat.format("{0} 21.08.2019 (19:44) {1}", aboutWhat, this.getClass().getSimpleName());
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.aboutWhat = aboutWhat;
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
