

// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.ad.ADComputer;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.ADSrv;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.NetScannerSvc;
import ru.vachok.networker.services.PhotoConverterSRV;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;


/**
 Управление Active Directory

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
    
    private static MessageToUser messageToUser = new MessageLocal(ActDirectoryCTRL.class.getSimpleName());
    
    /**
     {@link ADSrv}
     */
    private ADSrv adSrv;

    private Visitor visitor;

    /**
     Заголовок страницы.
     */
    private String titleStr = "PowerShell. Применить на SRV-MAIL3";
    
    /**
     {@link PhotoConverterSRV}
     */
    private PhotoConverterSRV photoConverterSRV;

    /**
     @param adSrv             {@link AppComponents#adSrv()}
     @param photoConverterSRV {@link PhotoConverterSRV}
     @param sshActs           {@link SshActs}
     */
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV, SshActs sshActs) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
        Thread.currentThread().setName(getClass().getSimpleName());
    }

    @GetMapping ("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) {
        this.visitor = ConstantsFor.getVis(request);
        List<ADUser> adUsers = adSrv.userSetter();
        if(request.getQueryString()!=null){
            return queryStringExists(request.getQueryString(), model);
        }
        else{
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute(ConstantsFor.ATT_PHOTO_CONVERTER, photoConverterSRV);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<p>" + visitor);
            model.addAttribute("pcs", ADSrv.adPCMap(adComputer.getAdComputers(), true));
            model.addAttribute(ConstantsFor.ATT_USERS, ADSrv.fromADUsersList(adUsers));
        }
        return "ad";
    }
    
    /**
     Get adphoto.html
     <p>
     1. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}. Записываем визит ({@link Visitor}). <br>
     2. {@link ConstantsFor#isPingOK()}. Доступность проверим. <br>
     3. {@link PhotoConverterSRV#psCommands} - {@link Model} аттрибут {@code content} <br>
     4.5. {@link PageFooter#getFooterUtext()} - аттрибут {@link ConstantsFor#ATT_FOOTER} + 6. {@link Visitor#toString()} <br><br>
     <b>{@link NullPointerException}:</b><br>
     7. {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} пишем в файл.
     <p>

     @param photoConverterSRV {@link PhotoConverterSRV}
     @param model             {@link Model}
     @param request           {@link HttpServletRequest}.
     @return adphoto.html
     */
    @GetMapping ("/adphoto")
    public String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model, HttpServletRequest request) {
        this.visitor = ConstantsFor.getVis(request);

        this.photoConverterSRV = photoConverterSRV;
        try{
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            if(!ConstantsFor.isPingOK()){
                titleStr = "ping srv-git.eatmeat.ru is " + false;
            }
            model.addAttribute(ConstantsFor.ATT_TITLE, titleStr);
            model.addAttribute("content", photoConverterSRV.psCommands());
            model.addAttribute("alert", ALERT_AD_FOTO);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>" + visitor);
        }
        catch(NullPointerException e){
            messageToUser.errorAlert("ActDirectoryCTRL", "adFoto", e.getMessage());
            FileSystemWorker.error("ActDirectoryCTRL.adFoto", e);
        }
        return "adphoto";
    }
    
    /**
     * AdItem
     * <br> 3. {@link ADSrv#getDetails(String)} <br> 4. {@link
     * PageFooter#getFooterUtext()}
     *
     * @param queryString {@link HttpServletRequest#getQueryString()}
     * @param model       {@link Model}
     * @return aditem.html
     */
    private String queryStringExists( String queryString , Model model ) {
        NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();
        netScannerSvc.setThePc(queryString);
        String attributeValue = netScannerSvc.getInfoFromDB();
        InternetUse internetUse = new InetUserPCName();
    
        model.addAttribute(ConstantsFor.ATT_TITLE , queryString + " " + attributeValue);
        model.addAttribute(ConstantsFor.ATT_USERS , netScannerSvc.getInputWithInfoFromDB());
    
        try {
            adDetails(queryString, attributeValue, model);
        }
        catch (Exception e) {
            model.addAttribute(ATT_DETAILS, ConstantsFor.HTMLTAG_CENTER + internetUse.getUsage(queryString + ConstantsFor.DOMAIN_EATMEATRU) + ConstantsFor.HTML_CENTER_CLOSE);
        }
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "aditem";
    }
    
    private void adDetails(String queryString, String attributeValue, Model model) throws IOException {
        String adSrvDetails = adSrv.getDetails(queryString);
        model.addAttribute(ATT_DETAILS, adSrvDetails);
        adSrvDetails = adSrvDetails.replaceAll("</br>", "\n").replaceAll("<p>", "\n\n").replaceAll("<p><b>", "\n\n");
        String finalAdSrvDetails = adSrvDetails;
        messageToUser.info(getClass().getSimpleName(), queryString, attributeValue);
    }
    
}