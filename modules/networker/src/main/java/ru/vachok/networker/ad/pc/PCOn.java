// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.NetKeeper;
import ru.vachok.networker.componentsrepo.data.enums.*;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;


/**
 @see ru.vachok.networker.ad.pc.PCOnTest
 @since 31.01.2019 (0:20) */
class PCOn extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOn.class.getSimpleName());
    
    private @NotNull String sql;
    
    private UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
    
    private String pcName;
    
    public PCOn(@NotNull String pcName) {
        this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName);
        this.sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = checkValidNameWithoutEatmeat(aboutWhat);
        return getHTMLCurrentUserName();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "PC is not set " + this.toString();
        }
        else {
            getHTMLCurrentUserName();
            return PCInfo.defaultInformation(pcName, true);
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
    public void setClassOption(Object option) {
        this.pcName = (String) option;
    }
    
    private @NotNull String getHTMLCurrentUserName() {
        UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        List<String> timeName = userInfo.getPCLogins(pcName, 20);
        String timesUserLast;
        if (timeName.size() > 0) {
            timesUserLast = Paths.get(timeName.get(0).split(" ")[1]).getFileName().toString();
        }
        else {
            timesUserLast = new DBPCHTMLInfo(pcName).getUserNameFromNonAutoDB();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p>  Список всех зарегистрированных пользователей ПК:<br>");
        for (String userFolderFile : timeName) {
            stringBuilder.append(parseUserFolders(userFolderFile));
        }
    
        long date = MyCalen.getLongFromDate(26, 12, 1991, 17, 30);
        
        try {
            date = Long.parseLong(timesUserLast.split(" ")[0]);
        }
        catch (NumberFormatException ignore) {
            //23.08.2019 (17:25)
        }
        String format = "Крайнее имя пользователя на ПК " + pcName + " - " + timesUserLast + " (" + new Date(date) + " )";
        messageToUser.info(NetScanService.autoResolvedUsersRecord(checkValidNameWithoutEatmeat(pcName), getUserLogin()));
        return format + stringBuilder.toString();
    
    }
    
    private @NotNull String parseUserFolders(@NotNull String userFolderFile) {
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
    
    private @NotNull String getUserLogin() {
        userInfo.setClassOption(pcName);
        String namesToFile;
        try {
            namesToFile = userInfo.getPCLogins(pcName, 1).get(0);
        }
        catch (IndexOutOfBoundsException e) {
            namesToFile = "User not found";
        }
        
        return namesToFile;
    }
    
    @NotNull String pcNameWithHTMLLink() {
        userInfo.setClassOption(pcName);
        String lastUserRaw = userInfo.getInfo();
        String lastUser = new PageGenerationHelper().setColor("white", lastUserRaw);
        
        StringBuilder builder = new StringBuilder();
        builder.append("<br><b>");
        builder.append(new PageGenerationHelper().getAsLink("/ad?" + pcName, pcName)).append(" : ");
        builder.append(lastUser);
        builder.append("</b>    ");
        builder.append(". ");
        builder.append(new DBPCHTMLInfo(pcName).fillAttribute(pcName));
        addToMap(builder.toString());
        return builder.toString().replaceAll("\n", " ");
    }
    
    private void addToMap(String addToMapString) {
        String pcOnline = "online is " + true + "<br>";
        messageToUser.info(MessageFormat.format("{0} {1}", pcName, pcOnline));
        int onlinePC = AppComponents.getUserPref().getInt(PropertiesNames.ONLINEPC, 0);
        onlinePC += 1;
        UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
        AppComponents.getProps().setProperty(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
        try {
            NetKeeper.getPcNamesForSendToDatabase().add(pcName + ":" + new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress() + " online true<br>");
        }
        catch (UnknownFormatConversionException e) {
            messageToUser.error(e.getMessage() + " see line: 148 ***");
        }
    }
    
}