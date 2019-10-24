// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.UnknownFormatConversionException;


/**
 @see PCOnTest
 @since 31.01.2019 (0:20) */
class PCOn extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOn.class.getSimpleName());
    
    private @NotNull String sql;
    
    private UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
    
    private String pcName;
    
    public PCOn(@NotNull String pcName) {
        this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName).toLowerCase();
        this.sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
        NetScanService.autoResolvedUsersRecord(checkValidNameWithoutEatmeat(pcName), getUserLogin());
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = checkValidNameWithoutEatmeat(aboutWhat).toLowerCase();
        return pcNameWithHTMLLink();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null) {
            return "PC is not set " + this.toString();
        }
        else {
            String currentName = pcNameWithHTMLLink();
            NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(currentName, true);
            return currentName;
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
    
    /**
     timeName должен отдавать строки вида {@code 1565794123799 \\do0213.eatmeat.ru\c$\Users\ikudryashov Wed Aug 14 17:48:43 MSK 2019 1565794123799}
     
     @return Крайнее имя пользователя на ПК do0213 - ikudryashov (Wed Aug 14 17:48:43 MSK 2019)
     */
    private @NotNull String getHTMLCurrentUserName() {
        UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        List<String> timeName = userInfo.getLogins(pcName, 20);
        String timesUserLast;
        if (timeName.size() > 0) {
            timesUserLast = timeNameAndDate(timeName.get(0));
        }
        else {
            timesUserLast = new DBPCHTMLInfo(pcName).getUserNameFromNonAutoDB().toLowerCase();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<p>  Список всех зарегистрированных пользователей ПК:<br>");
    
        for (String userFolderFile : timeName) {
            stringBuilder.append(parseUserFolders(userFolderFile));
        }
        long date = MyCalen.getLongFromDate(26, 12, 1991, 17, 30);
        String format = "Крайнее имя пользователя на ПК " + pcName + " - " + timesUserLast + " (" + new Date(date) + ")";
        return format + stringBuilder.toString();
    
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
        builder.append(HTMLGeneration.getInstance("PageGenerationHelper").setColor(ConstantsFor.GREEN, new DBPCHTMLInfo(pcName).fillAttribute(pcName)));
        addToMap(builder.toString());
        return builder.toString().replaceAll("\n", " ");
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
            namesToFile = userInfo.getLogins(pcName, 1).get(0);
        }
        catch (IndexOutOfBoundsException e) {
            namesToFile = "User not found";
        }
        
        return namesToFile;
    }
    
    private @NotNull String timeNameAndDate(@NotNull String timeName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Paths.get(timeName.split(" ")[1]).getFileName().toString());
        try {
            stringBuilder.append(":date:").append(Long.parseLong(timeName.split(" ")[0]));
        }
        catch (NumberFormatException e) {
            messageToUser.error(e.getMessage() + " see line: 112 ***");
        }
        catch (RuntimeException e) {
            stringBuilder.append(e.getMessage()).append("\n").append(AbstractForms.fromArray(e));
        }
        return stringBuilder.toString();
    }
    
    private void addToMap(String addToMapString) {
        String pcOnline = "online is " + true + "<br>";
    
        messageToUser.info(MessageFormat.format("{0} {1}", pcName, pcOnline));
    
        int onlinePC = AppComponents.getUserPref().getInt(PropertiesNames.ONLINEPC, 0);
        onlinePC += 1;
        UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
        AppComponents.getProps().setProperty(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
    
        try {
            String toVelkomPCDB = pcName + ":" + new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress() + " online true<br>";
            NetKeeper.getPcNamesForSendToDatabase().add(toVelkomPCDB);
        }
        catch (UnknownFormatConversionException e) {
            messageToUser.error(MessageFormat.format("PCOn.addToMap", e.getMessage(), AbstractForms.exceptionNetworker(e.getStackTrace())));
        }
    }
    
}