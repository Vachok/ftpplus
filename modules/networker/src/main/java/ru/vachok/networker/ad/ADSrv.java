// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.text.MessageFormat;
import java.util.List;


/**
 @see ru.vachok.networker.ad.ADSrvTest
 @since 25.09.2018 (15:10) */
@Service("adSrv")
public class ADSrv implements Runnable {
    
    
    private static final String FILEPATHSTR_USERSTXT = "/static/texts/users.txt";
    
    private static final MessageToUser messageToUser = new MessageLocal(ADSrv.class.getSimpleName());
    
    private ADUser adUser;
    
    private String userInputRaw;
    
    private ADComputer adComputer;
    
    @Contract(pure = true)
    @Autowired
    public ADSrv(ADUser adUser, ADComputer adComputer) {
        this.adUser = adUser;
        this.adComputer = adComputer;
    }
    
    public ADSrv(@NotNull ADUser adUser) {
        this.userInputRaw = adUser.getInputName();
        this.adUser = adUser;
    }
    
    @Contract(pure = true)
    protected ADSrv() {
    }
    
    public ADComputer getAdComputer() {
        return adComputer;
    }
    
    public String getUserInputRaw() {
        return userInputRaw;
    }
    
    public void setUserInputRaw(String userInputRaw) {
        this.userInputRaw = userInputRaw;
    }
    
    public ADUser getAdUser() {
        return adUser;
    }
    
    public static @NotNull String fromADUsersList(@NotNull List<ADUser> adUsers) {
        StringBuilder nStringBuilder = new StringBuilder();
        nStringBuilder.append("\n");
        for (ADUser ad : adUsers) {
            nStringBuilder
                .append(ad)
                .append("\n");
        }
        nStringBuilder.append("\n");
        return nStringBuilder.toString();
    }
    
    public String getInternetUsage(@NotNull String queryString) {
        if (queryString.toLowerCase().contains(ConstantsFor.EATMEAT)) {
            queryString = queryString.split("\\Q.eatmeat\\E")[0];
        }
        ;
        InformationFactory informationFactory = InformationFactory.getInstance(queryString);
        
        String internetUsageInfo = informationFactory.getInfoAbout(queryString + ConstantsFor.DOMAIN_EATMEATRU);
        String internetStatistics = new PageGenerationHelper().setColor(ConstantsFor.YELLOW, informationFactory.getInfo());
        String htmlLikePresentation = MessageFormat.format("{0}<p>{1}<p>", internetStatistics, internetUsageInfo);
    
        htmlLikePresentation = htmlLikePresentation.replace("юзер", ConstantsFor.RUSSTR_KOMPUTER);
        return ConstantsFor.HTML_PCENTER + htmlLikePresentation + ConstantsFor.HTML_CENTER_CLOSE;
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
        sb.append("CLASS_NAME_PCUSERRESOLVER='").append(ConstantsFor.CLASS_NAME_PCUSERRESOLVER).append('\'');
        sb.append(", adUser=").append(adUser);
        sb.append(", userInputRaw='").append(userInputRaw).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Запуск.
     */
    @Override
    public void run() {
        new MessageCons(getClass().getSimpleName()).errorAlert("ADSrv.run");
    }
    
    /**
     <b>Запрос на конвертацию фото</b>
     
     @see PhotoConverterSRV
     */
    private void psComm() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        String psCommands = photoConverterSRV.psCommands();
        System.out.println("psCommands = " + psCommands);
    }
    
}
