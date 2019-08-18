// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.PCUserNameResolver;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.ad.user.FileADUsersParser;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.util.List;


/**
 @since 13.08.2019 (11:41) */
public abstract class PCInformation extends DatabasePCInfo {
    
    public static void setPcName(@NotNull String pcName) {
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            DatabasePCInfo.setAboutWhat(pcName + ConstantsFor.DOMAIN_EATMEATRU);
        }
        else {
            DatabasePCInfo.setAboutWhat(pcName);
        }
    }
    
    public static List<ADUser> getADUsers() {
        PCInformation pcInformation = new FileADUsersParser();
        return pcInformation.getADUsers();
    }
    
    @Override
    public void setClassOption(Object classOption) {
        DatabasePCInfo.setAboutWhat((String) classOption);
    }
    
    @Override
    public String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
    
    @Override
    public abstract String getInfo();
    
    @Contract(" -> new")
    static @NotNull PCInformation getPCInfo() {
        return new PCUserNameResolver(new CurrentPCUser());
    }
}
