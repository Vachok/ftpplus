// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.PhotoConverterSRV;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.DatabasePCSearcher;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageFooter;
import ru.vachok.networker.restapi.internetuse.InternetUse;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;


/**
 Управление Active Directory
 <p>
 
 @see ru.vachok.networker.controller.ActDirectoryCTRLTest
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {
    
    
    /**
     Небольшое описание, для показа на сайте.
     */
    private static final String ALERT_AD_FOTO =
        "<p>Для корректной работы, вам нужно положить фото юзеров <a href=\"file://srv-mail3.eatmeat.ru/c$/newmailboxes/fotoraw/\" " +
            "target=\"_blank\">\\\\srv-mail3.eatmeat" +
            ".ru\\c$\\newmailboxes\\fotoraw\\</a>\n";
    
    private static final String ATT_DETAILS = "details";
    
    protected static final String STR_ADPHOTO = "adphoto";
    
    private final InformationFactory pageFooter = new PageFooter();
    
    private static MessageToUser messageToUser = new MessageLocal(ActDirectoryCTRL.class.getSimpleName());
    
    private InternetUse internetUse = new InetUserPCName();
    
    private InformationFactory informationFactory = new DatabasePCSearcher();
    
    /**
     {@link ADSrv}
     */
    private ADSrv adSrv;
    
    private Visitor visitor;
    
    /**
     Заголовок страницы.
     */
    private String titleStr = "PowerShell. Применить на SRV-MAIL3";
    
    private PhotoConverterSRV photoConverterSRV;
    
    /**
     @param adSrv {@link AppComponents#adSrv()}
     @param photoConverterSRV {@link PhotoConverterSRV}
     @param sshActs {@link SshActs}
     */
    @Contract(pure = true)
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV, SshActs sshActs) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
    }
    
    @GetMapping("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) {
        this.visitor = UsefulUtilites.getVis(request);
        List<ADUser> adUsers = adSrv.userSetter();
        if (request.getQueryString() != null) {
            return queryStringExists(request.getQueryString(), model);
        }
        else {
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute(ModelAttributeNames.ATT_PHOTO_CONVERTER, photoConverterSRV);
            model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER) + "<p>" + visitor);
            model.addAttribute("pcs", ADSrv.showADPCList(adComputer.getAdComputers(), true));
            model.addAttribute(ModelAttributeNames.ATT_USERS, ADSrv.fromADUsersList(adUsers));
        }
        return "ad";
    }
    
    /**
     Get adphoto.html
     <p>
     1. {@link UsefulUtilites#getVis(HttpServletRequest)}. Записываем визит ({@link Visitor}). <br>
     2. {@link UsefulUtilites#isPingOK()}. Доступность проверим. <br>
     3. {@link PhotoConverterSRV#psCommands} - {@link Model} аттрибут {@code content} <br>
     4.5. {@link PageFooter#getFooterUtext()} - аттрибут {@link ModelAttributeNames#ATT_FOOTER} + 6. {@link Visitor#toString()} <br><br>
     <b>{@link NullPointerException}:</b><br>
     7. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} пишем в файл.
     <p>
 
     @param photoConverterSRV {@link PhotoConverterSRV}
     @param model {@link Model}
     @param request {@link HttpServletRequest}.
     @return adphoto.html
     */
    @GetMapping("/adphoto")
    public String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model, HttpServletRequest request) {
        this.visitor = UsefulUtilites.getVis(request);
        this.photoConverterSRV = photoConverterSRV;
        try {
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            if (!UsefulUtilites.isPingOK()) {
                titleStr = "ping srv-git.eatmeat.ru is " + false;
            }
            model.addAttribute(ModelAttributeNames.ATT_TITLE, titleStr);
            model.addAttribute("content", photoConverterSRV.psCommands());
            model.addAttribute("alert", ALERT_AD_FOTO);
            model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER) + "<br>" + visitor);
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
    
    /**
     AdItem
     <br> 3. {@link ADSrv#getDetails(String)} <br> 4. {@link
    PageFooter#getFooterUtext()}
 
     @param queryString {@link HttpServletRequest#getQueryString()}
     @param model {@link Model}
     @return aditem.html
     */
    private @NotNull String queryStringExists(String queryString, @NotNull Model model) {
        String attributeValue = informationFactory.getInfoAbout(queryString);
    
        model.addAttribute(ModelAttributeNames.ATT_TITLE, queryString);
        model.addAttribute(ModelAttributeNames.ATT_USERS, attributeValue);
        try {
            adDetails(queryString, attributeValue, model);
        }
        catch (IOException e) {
            messageToUser.error(MessageFormat.format("ActDirectoryCTRL.queryStringExists: {0}, ({1})", e.getMessage(), e.getClass().getName()));
        }
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        return "aditem";
    }
    
    private void adDetails(String queryString, String attributeValue, @NotNull Model model) throws IOException {
        String adSrvDetails = adSrv.getDetails(queryString);
        model.addAttribute(ATT_DETAILS, adSrvDetails);
        adSrvDetails = adSrvDetails.replaceAll("</br>", "\n").replaceAll("<p>", "\n\n").replaceAll("<p><b>", "\n\n");
        String finalAdSrvDetails = adSrvDetails;
        messageToUser.info(getClass().getSimpleName(), queryString, attributeValue);
    }
}