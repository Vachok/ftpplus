// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.Stats;

import java.text.MessageFormat;
import java.util.List;


/**
 * @see UserInfoTest$$ABS
 */
public abstract class UserInfo implements InformationFactory {
    
    private static final String ADUSER = ModelAttributeNames.ADUSER;
    
    private @NotNull Stats userStats = Stats.getInetStats();
    
    @Contract(" -> new")
    public static @NotNull UserInfo getI(String type) {
        if(type==null){
            throw new InvokeIllegalException(MessageFormat.format("No correct {0} instance chosen! type is NULL", UserInfo.class.getTypeName()));
        }
        switch (type) {
            case ADUSER:
                return new ADUserResolver();
            default: return new ResolveUserInDataBase();
        }
    }
    
    public abstract List<String> getPossibleVariantsOfPC(String userName, int resultsLimit);
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    @Override
    public abstract String getInfo();
    
    public abstract List<String> getPossibleVariantsOfUser(String pcName);
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserInfo{");
        sb.append("userStats=").append(userStats.toString());
        sb.append('}');
        return sb.toString();
    }
}
