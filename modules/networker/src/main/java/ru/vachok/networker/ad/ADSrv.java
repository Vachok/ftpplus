// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.vachok.networker.ad.pc.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.restapi.message.MessageToUser;

import java.util.List;


/**
 @see ru.vachok.networker.ad.ADSrvTest
 @since 25.09.2018 (15:10) */
public class ADSrv {
    
    
    private static final String FILEPATHSTR_USERSTXT = "/static/texts/users.txt";
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ADSrv.class.getSimpleName());
    
    private String userInputRaw;
    
    @Contract(pure = true)
    public ADSrv() {
    }
    
    @Autowired
    public ADSrv(@NotNull ADUser adUser) {
        this.userInputRaw = adUser.getInputName();
    }
    
    public String getUserInputRaw() {
        return userInputRaw;
    }
    
    public void setUserInputRaw(String userInputRaw) {
        this.userInputRaw = userInputRaw;
    }
    
    public static @NotNull String fromADUsersList(@NotNull List<ADUser> adUsers) {
        StringBuilder nStringBuilder = new StringBuilder();
        nStringBuilder.append("\n");
        for (ADUser ad : adUsers) {
            nStringBuilder.append(ad).append("\n");
        }
        nStringBuilder.append("\n");
        return nStringBuilder.toString();
    }
    
    public static @NotNull String showADPCList(@NotNull List<ADComputer> adComputers, boolean br) {
        StringBuilder brStringBuilder = new StringBuilder();
        StringBuilder nStringBuilder = new StringBuilder();
        brStringBuilder.append("<p>");
        nStringBuilder.append("\n");
        for (ADComputer ad : adComputers) {
            brStringBuilder
                    .append(ad)
                    .append("<br>");
            nStringBuilder
                    .append(ad)
                    .append("\n\n");
        }
        brStringBuilder.append("</p>");
        nStringBuilder.append("\n\n\n");
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ADSrv{");
        sb.append(", userInputRaw='").append(userInputRaw).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    /**
     <b>Запрос на конвертацию фото</b>
     
     @see PhotoConverterSRV
     */
    private void psComm() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        String psCommands = photoConverterSRV.psCommands();
    }
    
}
