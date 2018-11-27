package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.net.NetScannerSvc;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActDirectoryCTRL.class.getSimpleName());

    private ADSrv adSrv;

    private static String inputWithInfoFromDB;

    private String titleStr = "PowerShell. Применить на SRV-MAIL3";

    private PhotoConverterSRV photoConverterSRV;

    /*Instances*/
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
        Thread.currentThread().setName(getClass().getSimpleName());
    }

    public static void setInputWithInfoFromDB(String inputWithInfoFromDB) {
        ActDirectoryCTRL.inputWithInfoFromDB = inputWithInfoFromDB;
    }

    @GetMapping("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) {
        List<ADUser> adUsers = adSrv.userSetter();
        if (request.getQueryString() != null) return queryStringExists(request.getQueryString(), model);
        else {
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute("photoConverter", photoConverterSRV);
            model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
            model.addAttribute("pcs", new TForms().adPCMap(adComputer.getAdComputers(), true));
            model.addAttribute(ConstantsFor.USERS, new TForms().fromADUsersList(adUsers, true));
        }
        return "ad";
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
        NetScannerSvc iScan = NetScannerSvc.getI();
        iScan.setThePc(queryString);
        model.addAttribute("title", queryString + " " + iScan.getInfoFromDB());
        model.addAttribute(ConstantsFor.USERS, inputWithInfoFromDB);
        try {
            model.addAttribute("details", adSrv.getDetails(queryString));
        } catch (Exception e) {
            model.addAttribute("details", e.getMessage());
        }
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        return "aditem";
    }

    @GetMapping("/adphoto")
    private String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model) {
        this.photoConverterSRV = photoConverterSRV;
        try {
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            if (!ConstantsFor.isPingOK()) titleStr = "ping to srv-git.eatmeat.ru is " + false;
            model.addAttribute("title", titleStr);
            model.addAttribute("content", photoConverterSRV.psCommands());
            model.addAttribute("alert", ConstantsFor.ALERT_AD_FOTO);
            model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "adphoto";
    }

    /**
     @return {@link ADUser}
     @since 13.11.2018 (11:24)
     @deprecated
     */
    @Deprecated
    private String adUserString() {
        ADUser adUser = AppComponents.pcUserResolver().adUsersSetter();
        return adUser.toString();
    }
}