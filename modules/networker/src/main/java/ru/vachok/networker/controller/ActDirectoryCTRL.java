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
import ru.vachok.networker.UsefulUtilities;
import ru.vachok.networker.ad.*;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PCInfo;
import ru.vachok.networker.restapi.message.MessageLocal;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;


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
    
    protected static final String STR_ADPHOTO = "adphoto";
    
    private final HTMLGeneration pageFooter = new PageGenerationHelper();
    
    private static MessageToUser messageToUser = new MessageLocal(ActDirectoryCTRL.class.getSimpleName());
    
    private InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
    
    /**
     {@link ADSrv}
     */
    private ADSrv adSrv;
    
    /**
     Заголовок страницы.
     */
    private String titleStr = "PowerShell. Применить на SRV-MAIL3";
    
    private PhotoConverterSRV photoConverterSRV;
    
    private Model model;
    
    @Contract(pure = true)
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
    }
    
    @GetMapping("/ad")
    public String adUsersComps(@NotNull HttpServletRequest request, Model model) {
        this.model = model;
        if (request.getQueryString() != null) {
            return queryStringExists(request.getQueryString(), model);
        }
        else {
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute(ModelAttributeNames.PHOTO_CONVERTER, photoConverterSRV);
            model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER) + "<p>");
            model.addAttribute(ModelAttributeNames.PCS, ADSrv.showADPCList(adComputer.getAdComputers(), true));
            model.addAttribute(ModelAttributeNames.USERS, this.getClass().getSimpleName());
        }
        return "ad";
    }
    
    /**
     Get adphoto.html
     <p>
     1. {@link UsefulUtilities#getVis(HttpServletRequest)}. Записываем визит ({@link Visitor}). <br>
     2. {@link UsefulUtilities#isPingOK()}. Доступность проверим. <br>
     3. {@link PhotoConverterSRV#psCommands} - {@link Model} аттрибут {@code content} <br>
     4.5. {@link PageGenerationHelper#getFooterUtext()} - аттрибут {@link ModelAttributeNames#FOOTER} + 6. {@link Visitor#toString()} <br><br>
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
    
    /**
     AdItem
     <br> 3. {@link ADSrv#getInternetUsage(String)} <br> 4. {@link
    PageGenerationHelper#getFooterUtext()}
     
     @param queryString {@link HttpServletRequest#getQueryString()}
     @param model {@link Model}
     @return aditem.html
     */
    private @NotNull String queryStringExists(String queryString, @NotNull Model model) {
        this.model = model;
        InetAddress address = InetAddress.getLoopbackAddress();
        try {
            address = InetAddress.getByName(queryString);
        }
        catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        model.addAttribute(ModelAttributeNames.TITLE, queryString);
        
        checkPCOnline(address);
        
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.INET_USAGE);
        informationFactory.setClassOption(queryString);
        model.addAttribute(ModelAttributeNames.ATT_HEAD, (informationFactory).getInfo());
        model.addAttribute(ModelAttributeNames.DETAILS, informationFactory.getInfoAbout(queryString));
        return "aditem";
    }
    
    private void checkPCOnline(@NotNull InetAddress address) {
        InformationFactory informationFactory;
        informationFactory = InformationFactory.getInstance(InformationFactory.LOCAL);
        informationFactory.setClassOption(address.getHostAddress());
        model.addAttribute(ModelAttributeNames.USERS, ((PCInfo) informationFactory).getUserByPC(address.getHostName()));
    }
}