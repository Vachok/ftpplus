// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.user.UserInfo;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.componentsrepo.services.MyCalen;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.data.enums.ModelAttributeNames;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.info.NetScanService;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;


/**
 @see PCOnTest
 @since 31.01.2019 (0:20) */
class PCOn extends PCInfo {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PCOn.class.getSimpleName());
    
    private Set<String> ipsWithInet = FileSystemWorker.readFileToSet(new File(FileNames.INETSTATSIP_CSV).toPath().toAbsolutePath().normalize());
    
    private @NotNull String sql;
    
    private String pcName;
    
    private String userLogin;
    
    private String addressIp;
    
    public PCOn(@NotNull String pcName) {
        this.pcName = PCInfo.checkValidNameWithoutEatmeat(pcName).toLowerCase();
        this.sql = ConstantsFor.SQL_GET_VELKOMPC_NAMEPP;
        this.userLogin = getUserLogin();
        messageToUser.info(this.pcName, this.userLogin, MessageFormat.format("{0} pc online", englargeOnCounter()));
        this.addressIp = new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress();
    }
    
    private @NotNull String getUserLogin() {
        UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        String namesToFile;
        try {
            namesToFile = userInfo.getLogins(pcName, 1).get(0);
            NetScanService.autoResolvedUsersRecord(pcName, namesToFile);
            userInfo.setClassOption(pcName);
            namesToFile = Paths.get(namesToFile.split(" ")[1]).getFileName().toString();
        }
        catch (RuntimeException e) {
            namesToFile = ConstantsFor.ISNTRESOLVED;
        }
        
        return namesToFile;
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = checkValidNameWithoutEatmeat(aboutWhat).toLowerCase();
        if (pcName.contains("nknown")) {
            return pcName;
        }
        else {
            return pcNameWithHTMLLink();
        }
    }
    
    /**
     timeName должен отдавать строки вида {@code 1565794123799 \\do0213.eatmeat.ru\c$\Users\ikudryashov Wed Aug 14 17:48:43 MSK 2019 1565794123799}
     
     @return Крайнее имя пользователя на ПК do0213 - ikudryashov (Wed Aug 14 17:48:43 MSK 2019)
     */
    private @NotNull String getHTMLCurrentUserName() {
        UserInfo userInfo = UserInfo.getInstance(ModelAttributeNames.ADUSER);
        List<String> timeName = userInfo.getLogins(pcName, Integer.MAX_VALUE);
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
            String userFolderAndTimeStamp = parseUserFolders(userFolderFile);
            stringBuilder.append(userFolderAndTimeStamp);
        }
        long date = MyCalen.getLongFromDate(26, 12, 1991, 17, 30);
        String format = "Крайнее имя пользователя на ПК " + pcName + " - " + timesUserLast + " (" + new Date(date) + ")";
        return format + stringBuilder.toString();
    
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
    
    private @NotNull String parseUserFolders(@NotNull String userFolderFile) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] strings = userFolderFile.split(" ");
        stringBuilder.append(strings[1])
                .append(" ");
        try {
            stringBuilder.append(new Date(Long.parseLong(strings[0])));
        }
        catch (NumberFormatException e) {
            stringBuilder.append(ConstantsFor.OFFLINE);
        }
        stringBuilder.append("<br>");
        return stringBuilder.toString();
    }
    
    @Override
    public String getInfo() {
        if (pcName == null | pcName.contains("unknown")) {
            return "PC is not set or on DNS record" + this.toString();
        }
        else {
            this.pcName = checkValidNameWithoutEatmeat(pcName);
            String currentName = pcNameWithHTMLLink();
            messageToUser.warn(this.getClass().getSimpleName(), "added to getPcNamesForSendToDatabase", addToMap(pcName, addressIp, true, userLogin));
            NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(currentName + "<br>", true);
            return currentName;
        }
    }
    
    @NotNull String pcNameWithHTMLLink() {
        String lastUserRaw = pcName + " : " + userLogin; // pcName : userName
        String lastUser = new PageGenerationHelper().setColor("#00ff69", lastUserRaw);
        if (lastUser.contains(".err") || lastUser.contains(ConstantsFor.ISNTRESOLVED)) {
            lastUser = new PageGenerationHelper().setColor(ConstantsFor.YELLOW, UserInfo.getInstance("ResolveUserInDataBase").getLogins(pcName, 1).get(0));
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<br><b>");
        builder.append(new PageGenerationHelper().getAsLink("/ad?" + pcName, new NameOrIPChecker(pcName).resolveInetAddress().getHostAddress())).append(" ");
        builder.append(lastUser);
        builder.append("</b>    ");
        builder.append(". ");
        builder.append(HTMLGeneration.getInstance("PageGenerationHelper").setColor(ConstantsFor.WHITE, new DBPCHTMLInfo(pcName).fillAttribute(pcName)));
        if (ipsWithInet.contains(addressIp)) {
            builder.append(" ***");
        }
        return builder.toString().replaceAll("\n", " ");
    }
    
    private int englargeOnCounter() {
        int onlinePC = AppComponents.getUserPref().getInt(PropertiesNames.ONLINEPC, 0);
        onlinePC += 1;
        UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
        AppComponents.getProps().setProperty(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
        return onlinePC;
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
    
}