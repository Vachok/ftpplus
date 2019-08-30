// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ModelAttributeNames;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.*;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;


/**
 @see ru.vachok.networker.ad.ActDirectoryCTRLTest
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {
    
    
    private static final String ALERT_AD_FOTO =
            "<p>Для корректной работы, вам нужно положить фото юзеров <a href=\"file://srv-mail3.eatmeat.ru/c$/newmailboxes/fotoraw/\" " +
                    "target=\"_blank\">\\\\srv-mail3.eatmeat" +
                    ".ru\\c$\\newmailboxes\\fotoraw\\</a>\n";
    
    protected static final String STR_ADPHOTO = "adphoto";
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    private static MessageToUser messageToUser = new MessageLocal(ActDirectoryCTRL.class.getSimpleName());
    
    private ADSrv adSrv;
    
    private String titleStr = "PowerShell. Применить на SRV-MAIL3";
    
    private PhotoConverterSRV photoConverterSRV;
    
    private HttpServletRequest request;
    
    private Model model;
    
    @Contract(pure = true)
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
    }
    
    @GetMapping("/ad")
    public String adUsersComps(@NotNull HttpServletRequest request, @NotNull Model model) {
        this.request = request;
        this.model = model;
        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER) + "<p>");
        if (request.getQueryString() != null) {
            return queryStringExists();
        }
        else {
            model.addAttribute(ModelAttributeNames.PHOTO_CONVERTER, photoConverterSRV);
            model.addAttribute(ModelAttributeNames.PCS, UsefulUtilities.getRunningInformation());
            model.addAttribute(ModelAttributeNames.USERS, this.getClass().getSimpleName());
        }
        return "ad";
    }
    
    private @NotNull String queryStringExists() {
        HTMLInfo inetUse = (HTMLInfo) HTMLGeneration.getInstance(InformationFactory.ACCESS_LOG);
        String queryString = this.request.getQueryString();
        inetUse.setClassOption(queryString);
        model.addAttribute(ModelAttributeNames.TITLE, queryString);
    
        String infoAboutInetUse = inetUse.fillAttribute(queryString);
        model.addAttribute(ModelAttributeNames.HEAD, infoAboutInetUse);
        String detailsHTML = inetUse.fillWebModel();
        model.addAttribute(ModelAttributeNames.DETAILS, detailsHTML);
        return "aditem";
    }
    
    @GetMapping("/adphoto")
    public String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model, HttpServletRequest request) {
        this.photoConverterSRV = photoConverterSRV;
        this.model = model;
        try {
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            model.addAttribute(ModelAttributeNames.TITLE, titleStr);
            model.addAttribute("content", photoConverterSRV.psCommands());
            model.addAttribute("alert", ALERT_AD_FOTO);
            model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER) + "<br>");
        }
        catch (NullPointerException e) {
            messageToUser.errorAlert("ActDirectoryCTRL", "adFoto", e.getMessage());
            FileSystemWorker.error("ActDirectoryCTRL.adFoto", e);
        }
        return STR_ADPHOTO;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActDirectoryCTRL{");
        sb.append("pageFooter=").append(pageFooter);
        sb.append(", adSrv=").append(adSrv.toString());
        sb.append(", titleStr='").append(titleStr).append('\'');
        sb.append('}');
        return sb.toString();
    }
}