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

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActDirectoryCTRL.class.getSimpleName());

    private ADSrv adSrv;

    private static String inputWithInfoFromDB;

    private PhotoConverterSRV photoConverterSRV;

    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
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

    private String queryStringExists(String queryString, Model model) {
        model.addAttribute("title", queryString);
        model.addAttribute(ConstantsFor.USERS, inputWithInfoFromDB);
        try {
            model.addAttribute("details", adSrv.getDetails(queryString));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        return "aditem";
    }

    @GetMapping("/adphoto")
    private String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model) {
        this.photoConverterSRV = photoConverterSRV;
        try {
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            model.addAttribute("title", "PowerShell. Применить на SRV-MAIL3");
            model.addAttribute("content", photoConverterSRV.psCommands());
            model.addAttribute("alert", ConstantsFor.ALERT_AD_FOTO);
            model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "adphoto";
    }

    private String adUserString() {
        ADUser adUser = AppComponents.pcUserResolver().adUsersSetter();

        return adUser.toString();
    }
}