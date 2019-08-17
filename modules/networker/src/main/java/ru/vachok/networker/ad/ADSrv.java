// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.enums.ADAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageGenerationHelper;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


/**
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
    
    @SuppressWarnings({"DuplicateStringLiteralInspection", "OverlyLongMethod"})
    public List<ADUser> userSetter() {
        List<String> fileAsList = new ArrayList<>();
        List<ADUser> adUserList = new ArrayList<>();
        try (InputStream usrInputStream = getClass().getResourceAsStream(FILEPATHSTR_USERSTXT);
             InputStreamReader inputStreamReader = new InputStreamReader(usrInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            while (bufferedReader.ready()) {
                fileAsList.add(bufferedReader.readLine());
            }
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        int indexUser = 0;
        int h = 10;
        ADUser adU = new ADUser();
        for (int i = 0; i < fileAsList.size(); i += 10) {
            indexUser++;
            try {
                List<String> list = fileAsList.subList(i, h);
                for (String s : list) {
                    if (s.contains("DistinguishedName")) {
                        adU.setDistinguishedName(s.split(": ")[1]);
                    }
                    if (s.contains("Enabled")) {
                        adU.setEnabled(s.split(": ")[1]);
                    }
                    if (s.contains("GivenName")) {
                        adU.setGivenName(s.split(": ")[1]);
                    }
                    if (s.contains("Name")) {
                        adU.setName(s.split(": ")[1]);
                    }
                    if (s.contains("ObjectClass")) {
                        adU.setObjectClass(s.split(": ")[1]);
                    }
                    if (s.contains("ObjectGUID")) {
                        adU.setObjectGUID(s.split(": ")[1]);
                    }
                    if (s.contains(ADAttributeNames.SAM_ACCOUNT_NAME)) {
                        adU.setSamAccountName(s.split(": ")[1]);
                    }
                    if (s.contains("SID")) {
                        adU.setSid(s.split(": ")[1]);
                    }
                    if (s.contains("Surname")) {
                        adU.setSurname(s.split(": ")[1]);
                    }
                    if (s.contains("UserPrincipalName")) {
                        adU.setUserPrincipalName(s.split(": ")[1]);
                    }
                    else {
                        if (s.equals("")) {
                            adUserList.add(adU);
                            adU = new ADUser();
                        }
                    }
                }
            }
            catch (IndexOutOfBoundsException | IllegalArgumentException ignore) {
                //
            }
            h += 10;
        }
        String msg = indexUser + " users read";
        messageToUser.warn(msg);
        psComm();
        return adUserList;
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
        sb.append(", attr='").append(new TForms().fromArray(ADAttributeNames.values())).append('\'');
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
    
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private @NotNull ADUser setUserFromInput(@NotNull List<String> uList) {
        ADUser adU = new ADUser();
        for (String s : uList) {
            if (s.contains("DistinguishedName")) {
                adU.setDistinguishedName(s.split(": ")[1]);
            }
            if (s.contains("Enabled")) {
                adU.setEnabled(s.split(": ")[1]);
            }
            if (s.contains("GivenName")) {
                adU.setGivenName(s.split(": ")[1]);
            }
            if (s.contains("Name")) {
                adU.setName(s.split(": ")[1]);
            }
            if (s.contains("ObjectClass")) {
                adU.setObjectClass(s.split(": ")[1]);
            }
            if (s.contains("ObjectGUID")) {
                adU.setObjectGUID(s.split(": ")[1]);
            }
            if (s.contains(ADAttributeNames.SAM_ACCOUNT_NAME)) {
                adU.setSamAccountName(s.split(": ")[1]);
            }
            if (s.contains("SID")) {
                adU.setSid(s.split(": ")[1]);
            }
            if (s.contains("Surname")) {
                adU.setSurname(s.split(": ")[1]);
            }
            if (s.contains("UserPrincipalName")) {
                adU.setUserPrincipalName(s.split(": ")[1]);
            }
        }
        return adU;
    }
    
    private @NotNull List<String> adUsrFromFile() {
        List<String> retList = new ArrayList<>();
        try (InputStream inputStream = adUser.getUsersAD().getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().forEach(retList::add);
        }
        catch (IOException e) {
            messageToUser.errorAlert(ADSrv.class.getSimpleName(), "adUsrFromFile", e.getMessage());
            FileSystemWorker.error("ADSrv.adUsrFromFile", e);
        }
        return retList;
    }
    
    /**
     <b>Запрос на конвертацию фото</b>
     
     @see PhotoConverterSRV
     */
    private void psComm() {
        PhotoConverterSRV photoConverterSRV = new PhotoConverterSRV();
        photoConverterSRV.psCommands();
    }
    
}
