// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.PropertiesNames;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.restapi.MessageToUser;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UnknownFormatConversionException;
import java.util.concurrent.Executors;


/**
 @see ru.vachok.networker.ad.pc.PCOnTest
 @since 31.01.2019 (0:20) */
class PCOn extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOn.class.getSimpleName());
    
    private @NotNull String sql;
    
    private UserInfo userInfo = UserInfo.getInstance(UserInfo.ADUSER);
    
    private String pcName;
    
    public PCOn(@NotNull String pcName) {
        this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        if (this.pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            this.pcName = pcName.split(ConstantsFor.DOMAIN_EATMEATRU)[0];
        }
        return getHTMLCurrentUserName();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "PC is not set " + this.toString();
        }
        else {
            return getLinkToInternetPCInfo();
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCOn.class.getSimpleName() + "[\n", "\n]")
                .add("sql = '" + sql + "'")
                .add("pcName = '" + pcName + "'")
                .toString();
    }
    
    @Override
    public void setOption(Object option) {
        this.pcName = (String) option;
    }
    
    private @NotNull String getHTMLCurrentUserName() {
        UserInfo userInfo = UserInfo.getInstance(UserInfo.ADUSER);
        List<String> timeName = userInfo.getPCLogins(pcName, 50);
        String timesUserLast = MessageFormat.format("User {0} not found", pcName);
        if (timeName.size() > 0) {
            timesUserLast = timeName.get(0);
        }
        StringBuilder stringBuilder = new StringBuilder();
    
        stringBuilder.append("<p>  Список всех зарегистрированных пользователей ПК:<br>");
    
        for (String userFolderFile : timeName) {
            stringBuilder.append(parseUserFolders(userFolderFile));
        }
        if (pcName.contains(ConstantsFor.ERROR_DOUBLE_DOMAIN)) {
            pcName = pcName.replace(ConstantsFor.ERROR_DOUBLE_DOMAIN, ConstantsFor.DOMAIN_EATMEATRU);
        }
        if (!pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            pcName = pcName + ConstantsFor.DOMAIN_EATMEATRU;
        }
        long date = System.currentTimeMillis();
        try {
            date = Long.parseLong(timesUserLast.split(" ")[0]);
        }
        catch (NumberFormatException ignore) {
            //23.08.2019 (17:25)
        }
        String format = "Крайнее имя пользователя на ПК " + pcName + " - " + timesUserLast.split(" ")[1] + "<br>( " + new Date(date) + " )";
        return format + stringBuilder.toString();
    
    }
    
    private String parseUserFolders(String userFolderFile) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] strings = userFolderFile.split(" ");
        stringBuilder.append(strings[1])
                .append(" ");
        try {
            stringBuilder.append(new Date(Long.parseLong(strings[0])));
        }
        catch (NumberFormatException e) {
            stringBuilder.append("offline");
        }
        stringBuilder.append("<br>");
        return stringBuilder.toString();
    }
    
    private @NotNull String getLinkToInternetPCInfo() {
        userInfo.setOption(pcName);
        String namesToFile;
        try {
            namesToFile = userInfo.getPCLogins(pcName, 1).get(0);
        }
        catch (IndexOutOfBoundsException e) {
            namesToFile = "User not found";
        }
        final String finalNamesToFile = namesToFile;
        Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor())
                .execute(()->messageToUser.info(UserInfo.autoResolvedUsersRecord(pcName, finalNamesToFile)));
        return pcNameWithHTMLLink();
    }
    
    private void addToMap(String addToMapString) {
        String pcOnline = "online is " + true + "<br>";
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(addToMapString, true);
        messageToUser.info(MessageFormat.format("{0} {1}", pcName, pcOnline));
        int onlinePC = AppComponents.getUserPref().getInt(PropertiesNames.ONLINEPC, 0);
        onlinePC += 1;
        UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
        try {
            NetKeeper.getPcNamesForSendToDatabase().add(pcName + ":" + new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress() + " online true<br>");
        }
        catch (UnknownFormatConversionException e) {
            messageToUser.error(e.getMessage() + " see line: 148 ***");
        }
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks()
            .put(addToMapString + "online true <br>", true);
    }
    
    private @NotNull String pcNameWithHTMLLink() {
        userInfo.setOption(pcName);
        String lastUserRaw = userInfo.getInfo();
        String lastUser = new PageGenerationHelper().setColor("white", lastUserRaw);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<br><b>");
        builder.append(new PageGenerationHelper().getAsLink("/ad?" + pcName, pcName)).append(" : ");
        builder.append(lastUser);
        builder.append("</b>    ");
        builder.append(". ");
        addToMap(builder.toString());
        return builder.toString();
    }
    
}