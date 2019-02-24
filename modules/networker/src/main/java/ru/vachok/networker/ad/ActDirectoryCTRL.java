package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.messenger.MessageCons;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.SshActs;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.componentsrepo.Visitor;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScannerSvc;
import ru.vachok.networker.systray.ListenUserInfo;
import ru.vachok.networker.systray.MessageToTray;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
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
        "<p>Для корректной работы, вам нужно положить фото юзеров <a href=\"file://srv-mail3.eatmeat.ru/c$/newmailboxes/fotoraw/\" target=\"_blank\">\\\\srv-mail3.eatmeat" +
            ".ru\\c$\\newmailboxes\\fotoraw\\</a>\n";

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ActDirectoryCTRL.class.getSimpleName());

    private static final String ATT_DETAILS = "details";

    /**
     Доступность пк. online|offline сколько раз.

     @see NetScannerSvc#getInfoFromDB()
     */
    private static String inputWithInfoFromDB = null;

    /**
     {@link ADSrv}
     */
    private ADSrv adSrv;

    private Visitor visitor;

    /**
     {@link SshActs}
     */
    private SshActs sshActs;

    /**
     Заголовок страницы.
     */
    private String titleStr = "PowerShell. Применить на SRV-MAIL3";

    /**
     {@link PhotoConverterSRV}
     */
    private PhotoConverterSRV photoConverterSRV;

    /**
     @param inputWithInfoFromDB {@link NetScannerSvc#getInfoFromDB()}
     */
    public static void setInputWithInfoFromDB(String inputWithInfoFromDB) {
        ActDirectoryCTRL.inputWithInfoFromDB = inputWithInfoFromDB;
    }

    /**
     @param adSrv             {@link AppComponents#adSrv()}
     @param photoConverterSRV {@link PhotoConverterSRV}
     @param sshActs           {@link SshActs}
     */
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV, SshActs sshActs) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
        this.sshActs = sshActs;
        Thread.currentThread().setName(getClass().getSimpleName());
    }

    /**
     @param request {@link HttpServletRequest}
     @param model   {@link Model}
     @return ad.html
     */
    @GetMapping("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) {
        this.visitor = ConstantsFor.getVis(request);
        List<ADUser> adUsers = adSrv.userSetter();
        if (request.getQueryString() != null) {
            return queryStringExists(request.getQueryString(), model);
        } else {
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute(ConstantsFor.ATT_PHOTO_CONVERTER, photoConverterSRV);
            model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<p>" + visitor.toString());
            model.addAttribute("pcs", new TForms().adPCMap(adComputer.getAdComputers(), true));
            model.addAttribute(ConstantsFor.ATT_USERS, new TForms().fromADUsersList(adUsers, true));
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
     @param request {@link HttpServletRequest}.
     @return adphoto.html
     */
    @GetMapping("/adphoto")
    public String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model, HttpServletRequest request) {
        this.visitor = ConstantsFor.getVis(request);

        this.photoConverterSRV = photoConverterSRV;
        try {
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            model.addAttribute(ConstantsFor.ATT_SSH_ACTS, sshActs);
            if (!ConstantsFor.isPingOK()) {
                titleStr = "ping srv-git.eatmeat.ru is " + false;
            }
            model.addAttribute(ConstantsFor.ATT_TITLE, titleStr);
            model.addAttribute("content", photoConverterSRV.psCommands());
            model.addAttribute("alert", ALERT_AD_FOTO);
            model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext() + "<br>" + visitor.toString());
        } catch (NullPointerException e) {
            new MessageCons().errorAlert("ActDirectoryCTRL", "adFoto", e.getMessage());
            FileSystemWorker.error("ActDirectoryCTRL.adFoto", e);
        }
        return "adphoto";
    }

    /**
     <b>AdItem</b>
     Используемые методы и константы: <br> 1. {@link NetScannerSvc#setThePc(String)} <br> 2. {@link #inputWithInfoFromDB} <br> 3. {@link ADSrv#getDetails(String)} <br> 4. {@link
    PageFooter#getFooterUtext()}

     @param queryString {@link HttpServletRequest#getQueryString()}
     @param model       {@link Model}
     @return aditem.html
     */
    private String queryStringExists(String queryString, Model model) {
        NetScannerSvc netScannerSvc = AppComponents.netScannerSvc();
        netScannerSvc.setThePc(queryString);
        String attributeValue = NetScannerSvc.getInfoFromDB();
        model.addAttribute(ConstantsFor.ATT_TITLE, queryString + " " + attributeValue);
        model.addAttribute(ConstantsFor.ATT_USERS, inputWithInfoFromDB);
        try {
            String adSrvDetails = adSrv.getDetails(queryString);
            model.addAttribute(ATT_DETAILS, adSrvDetails);
            adSrvDetails = adSrvDetails.replaceAll("</br>", "\n").replaceAll("<p>", "\n\n").replaceAll("<p><b>", "\n\n");
            long l = new Calendar.Builder().setTimeOfDay(0, 0, 0).build().getTimeInMillis();
            String finalAdSrvDetails = adSrvDetails;
            new MessageToTray(new ListenUserInfo(queryString, attributeValue, finalAdSrvDetails)).info(queryString, attributeValue,
                ServiceInfoCtrl.percToEnd(new Date(l), 24));
        } catch (Exception e) {
            model.addAttribute(ATT_DETAILS, e.getMessage());
        }
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "aditem";
    }

}