// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.fileworks.FileSystemWorker;


/**
 @since 13.08.2019 (11:41) */
public class PCInformation extends PCInfo {
    
    public static void setPcName(@NotNull String pcName) {
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            PCInfo.setAboutWhat(pcName + ConstantsFor.DOMAIN_EATMEATRU);
        }
        else {
            PCInfo.setAboutWhat(pcName);
        }
    }
    
    @Override
    public void setClassOption(Object classOption) {
        PCInfo.setAboutWhat((String) classOption);
    }
    
    @Override
    public String getInfo() {
        return null;
    }
    
    @Override
    public String writeLog(String logName, String information) {
        return FileSystemWorker.writeFile(logName, information);
    }
    
    @Override
    protected String getUserByPCNameFromDB(String pcName) {
        throw new TODOException("18.08.2019 (18:07)");
    }
}
