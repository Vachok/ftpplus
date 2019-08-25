package ru.vachok.networker.ad.user;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.WalkerToUserFolder;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;


/**
 @see ru.vachok.networker.ad.user.ADUserResolverTest
 @since 22.08.2019 (14:14) */
class ADUserResolver extends UserInfo {
    
    
    private Object classOption;
    
    private String forADUser;
    
    private MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private WalkerToUserFolder walkerToUserFolder;
    
    @Override
    public void setClassOption(Object classOption) {
        this.classOption = classOption;
    }
    
    @Override
    public List<String> getPCLogins(String pcName, int resultsLimit) {
        this.classOption = pcName;
    
        this.walkerToUserFolder = new WalkerToUserFolder(pcName);
        StringBuilder pathBuilder = new StringBuilder();
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        
        try {
            pathBuilder.append("\\\\").append(pcName).append("\\c$\\users\\");
            if (new NameOrIPChecker(pcName).isLocalAddress()) {
                Files.walkFileTree(Paths.get(pathBuilder.toString()), Collections.singleton(FileVisitOption.FOLLOW_LINKS), 1, walkerToUserFolder);
//                PCInfo.recToDB(); todo 24.08.2019 (23:21)
            }
        }
        catch (ArrayIndexOutOfBoundsException | IOException e) {
            messageToUser.error(MessageFormat.format("ADUserResolver.getPossibleVariantsOfUser {0} - {1}", e.getClass().getTypeName(), e.getMessage()));
        }
        return walkerToUserFolder.getTimePath();
    }
    
    @Override
    public String getInfo() {
        String retStr = "null";
        try {
            retStr = MessageFormat.format("For user {1}, resolved pc (LIMIT 20) :\n{0} ", getInfoAbout((String) this.classOption), this.forADUser);
        }
        catch (IndexOutOfBoundsException e) {
            messageToUser.error(e.getMessage() + " see line: 70 ***");
        }
        return retStr;
    }
    
    @Override
    public String getInfoAbout(String userName) {
        this.classOption = userName;
        StringBuilder stringBuilder = new StringBuilder();
        for (String name : getUserLogins(userName, 20)) {
            stringBuilder.append(parseList(name));
        }
        return stringBuilder.toString();
    }
    
    @Override
    public List<String> getUserLogins(String userName, int resultsLimit) {
        this.classOption = userName;
        List<String> pcVariantsFromDB = UserInfo.getI(DB).getUserLogins(userName, resultsLimit);
        String pcName = new TForms().fromArray(pcVariantsFromDB);
        pcName = pcName.split("\\Q.eatmeat.ru : \\E")[0];
        this.walkerToUserFolder = new WalkerToUserFolder(pcName);
        return pcVariantsFromDB;
    }
    
    private @NotNull String parseList(@NotNull String name) {
        String[] splitNamePC = name.split(".eatmeat.ru : ");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(splitNamePC[0]).append("\n");
        try {
            this.forADUser = splitNamePC[1].replaceFirst("\\Q\\\\E", "").split("\\Q\\\\E")[0];
            
        }
        catch (IndexOutOfBoundsException e) {
            this.forADUser = e.getMessage();
        }
        return stringBuilder.toString();
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ADUserResolver.class.getSimpleName() + "[\n", "\n]")
            .add("classOption = " + classOption)
            .add("resolvedPC = '" + forADUser + "'")
            .add("walkerToUserFolder = " + !(walkerToUserFolder == null))
            .toString();
    }
}
