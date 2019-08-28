// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.pc;


import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.user.UserInfo;
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
    public String toString() {
        return new StringJoiner(",\n", PCOn.class.getSimpleName() + "[\n", "\n]")
                .add("sql = '" + sql + "'")
                .add("pcName = '" + pcName + "'")
                .toString();
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
    public void setOption(Object option) {
        this.pcName = (String) option;
    }
    
    private @NotNull String getLinkToInternetPCInfo() {
        userInfo.setOption(pcName);
        String namesToFile = userInfo.getInfo();
        UserInfo.autoResolvedUsersRecord(pcName, namesToFile);
        return pcNameWithHTMLLink();
    }
    
    private String resolveCurrentUser() {
        UserInfo userInfo = UserInfo.getInstance(UserInfo.ADUSER);
        userInfo.setOption(pcName);
        return userInfo.getInfo();
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.pcName = aboutWhat;
        if (this.pcName.contains(ConstantsFor.DOMAIN_EATMEATRU)) {
            this.pcName = pcName.split(ConstantsFor.DOMAIN_EATMEATRU)[0];
        }
        return getHTMLCurrentUserName();
    }
    
    private @NotNull String pcNameWithHTMLLink() {
        List<String> pcLogins = userInfo.getPCLogins(pcName, 1);
        String lastUserRaw = pcName;
        if (pcLogins.size() > 0) {
            lastUserRaw = pcLogins.get(pcLogins.size() - 1);
        }
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
    
    private void addToMap(String addToMapString) {
        String pcOnline = "online is " + true + "<br>";
        NetKeeper.getUsersScanWebModelMapWithHTMLLinks().put(addToMapString, true);
        messageToUser.info(MessageFormat.format("{0} {1}", pcName, pcOnline));
        int onlinePC = AppComponents.getUserPref().getInt(PropertiesNames.ONLINEPC, 0);
        onlinePC += 1;
        UsefulUtilities.setPreference(PropertiesNames.ONLINEPC, String.valueOf(onlinePC));
    }
    
    private @NotNull String getHTMLCurrentUserName() {
        UserInfo userInfo = UserInfo.getInstance(UserInfo.ADUSER);
        List<String> timeName = userInfo.getPCLogins(pcName, 50);
        
        String timesUserLast = timeName.get(timeName.size() - 1);
        StringBuilder stringBuilder = new StringBuilder();
        
        stringBuilder.append("<p>  Список всех зарегистрированных пользователей ПК:<br>");
        
        for (String userFolderFile : timeName) {
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
    
}