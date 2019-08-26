// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.info.InformationFactory;

import java.text.MessageFormat;
import java.util.List;
import java.util.StringJoiner;


/**
 * @see UserInfoTest$$ABS
 */
public abstract class UserInfo implements InformationFactory {
    
    public static final String ADUSER = ModelAttributeNames.ADUSER;
    
    @Contract(" -> new")
    public static @NotNull UserInfo getI(String type) {
        if(type==null){
            throw new InvokeIllegalException(MessageFormat.format("No correct {0} instance chosen! type is NULL", UserInfo.class.getTypeName()));
        }
        if (ADUSER.equals(type)) {
            return new UserOnlineResolverDBSender();
        }
        return new ResolveUserInDataBase();
    }
    
    public abstract List<String> getUserLogins(String userName, int resultsLimit);
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public abstract String getInfo();
    
    public abstract List<String> getPCLogins(String pcName, int resultsLimit);
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", UserInfo.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
