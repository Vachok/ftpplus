package ru.vachok.networker.ad.user;


import ru.vachok.networker.ad.pc.WalkerToUserFolder;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;


/**
 @see ru.vachok.networker.ad.user.ADUserResolverTest
 @since 22.08.2019 (14:14) */
class ADUserResolver extends UserInfo {
    
    
    private Object classOption;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    @Override
    public List<String> getPossibleVariantsOfPC(String userName, int resultsLimit) {
        throw new TODOException("24.08.2019 (12:37)");
    }
    
    @Override
    public String getInfoAbout(String userName) {
        this.classOption=userName;
        return (String) classOption;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        this.classOption = classOption;
    }
    
    @Override
    public List<String> getPossibleVariantsOfUser(String pcName) {
        this.classOption = pcName;
        
        WalkerToUserFolder walkerToUserFolder = new WalkerToUserFolder(pcName);
        StringBuilder pathBuilder = new StringBuilder();
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        
        try {
            pathBuilder.append("\\\\").append(pcName).append("\\c$\\users\\").append(resolveUser());
            Files.walkFileTree(Paths.get(pathBuilder.toString()), walkerToUserFolder); //fixme 24.08.2019 (14:00)
        }
        catch (ArrayIndexOutOfBoundsException | IOException e) {
            messageToUser.error(MessageFormat.format("ADUserResolver.getPossibleVariantsOfUser {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
        }
        return walkerToUserFolder.getTimePath();
    }
    
    private String resolveUser() {
        UserInfo userInfo = UserInfo.getI("");
        return userInfo.getInfoAbout((String) classOption);
    }
    
    @Override
    public String getInfo() {
        throw new TODOException("ru.vachok.networker.ad.user.ADUserResolver.getInfo created 22.08.2019 (14:13)");
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADUserResolver{");
        sb.append("classOption=").append(classOption);
        sb.append('}');
        return sb.toString();
    }
}
