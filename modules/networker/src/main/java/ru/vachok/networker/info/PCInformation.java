// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ad.PCUserNameResolver;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.ad.user.FileADUsersParser;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.util.List;


/**
 @since 13.08.2019 (11:41) */
public abstract class PCInformation implements InformationFactory {
    
    
    protected static String pcName = "NO NAME GIVEN!";
    
    public static void setPcName(String pcName) {
        PCInformation.pcName = pcName;
    }
    
    public static List<ADUser> getADUsers() {
        PCInformation PCInformation = new FileADUsersParser();
        return PCInformation.getADUsers();
    }
    
    public static void setCurrentPCName(String pcName) {
        PCInformation.pcName = pcName;
    }
    
    @Override
    public abstract String getInfoAbout(String samAccountName);
    
    @Override
    public void setClassOption(Object classOption) {
        PCInformation.pcName = (String) classOption;
    }
    
    @Override
    public String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
    
    @Override
    public abstract String getInfo();
    
    @Contract(" -> new")
    static @NotNull PCInformation getUserInfo() {
        return new PCUserNameResolver(pcName);
    }
}
