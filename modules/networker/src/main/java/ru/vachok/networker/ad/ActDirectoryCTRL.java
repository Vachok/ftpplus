package ru.vachok.networker.ad;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;


/**
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActDirectoryCTRL.class.getSimpleName());

    private ADSrv adSrv;

    private static final String USERS_SRTING = "users";

    private static String inputWithInfoFromDB;

    private PhotoConverterSRV photoConverterSRV;
    /*Instances*/
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv, PhotoConverterSRV photoConverterSRV) {
        this.photoConverterSRV = photoConverterSRV;
        this.adSrv = adSrv;
    }

    public static void setInputWithInfoFromDB(String inputWithInfoFromDB) {
        ActDirectoryCTRL.inputWithInfoFromDB = inputWithInfoFromDB;
    }


    @GetMapping("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) throws IOException {
        List<ADUser> adUsers = adSrv.userSetter();
        if (request.getQueryString() != null) return queryStringExists(request.getQueryString(), model);
        else {
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute("photoConverter", photoConverterSRV);
            model.addAttribute("footer", new PageFooter().getFooterUtext());
            model.addAttribute("pcs", new TForms().adPCMap(adComputer.getAdComputers(), true));
            model.addAttribute(USERS_SRTING, new TForms().fromADUsersList(adUsers, true));
        }
        return "ad";
    }

    private String queryStringExists(String queryString, Model model) throws IOException {
        model.addAttribute("title", queryString);
        model.addAttribute(USERS_SRTING, inputWithInfoFromDB);
        model.addAttribute("details", adSrv.getDetails(queryString));
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "aditem";
    }

    @PostMapping("/ad")
    private String adFoto(@ModelAttribute PhotoConverterSRV photoConverterSRV, Model model) {
        this.photoConverterSRV = photoConverterSRV;

        try {
            model.addAttribute("photoConverterSRV", photoConverterSRV);
            model.addAttribute("ok", photoConverterSRV.psCommands());
        } catch (NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "ok";
    }

    private String adUserString() {
        ADUser adUser = AppComponents.pcUserResolver().adPCSetter();
        return adUser.toString();
    }
}