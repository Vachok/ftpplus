package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.ADComputer;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.services.ADSrv;

import javax.servlet.http.HttpServletRequest;
import java.net.UnknownServiceException;
import java.util.List;


/**
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {

    private static ADSrv adSrv;

    private static final String USERS_SRTING = "users";

    private static String inputWithInfoFromDB;

    /*Instances*/
    @Autowired
    public ActDirectoryCTRL(ADSrv adSrv) {
        ActDirectoryCTRL.adSrv = adSrv;
    }

    public static void setInputWithInfoFromDB(String inputWithInfoFromDB) {
        ActDirectoryCTRL.inputWithInfoFromDB = inputWithInfoFromDB;
    }

    @GetMapping("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) throws UnknownServiceException {
        if (request.getQueryString() != null) return queryStringExists(request.getQueryString(), model);
        else if (ConstantsFor.getPcAuth(request)) {
            ADComputer adComputer = adSrv.getAdComputer();
            model.addAttribute("footer", new PageFooter().getFooterUtext());
            model.addAttribute("pcs", new TForms().adPCMap(adComputer.getAdComputers(), true));
            model.addAttribute(USERS_SRTING, adUserString());
        } else {
            throw new UnknownServiceException();
        }
        return "ok";
    }

    private String queryStringExists(String queryString, Model model) {
        model.addAttribute("title", queryString);
        model.addAttribute(USERS_SRTING, inputWithInfoFromDB);
        model.addAttribute("details", adSrv.getDetails());
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "aditem";
    }

    private String adFoto(Model model) {
        adSrv.run();
        List<ADComputer> adComputers = adSrv.getAdComputer().getAdComputers();
        List<ADUser> adUsers = adSrv.getAdUser().getAdUsers();
        StringBuilder stringBuilder = new StringBuilder();
        model.addAttribute("pcs", new TForms().adPCMap(adComputers, false));
        model.addAttribute(USERS_SRTING, new TForms().adUsersMap(adUsers, false));
        adComputers.forEach((x -> stringBuilder.append(x.toString())));
        stringBuilder.append("<br>");
        return "ok";
    }

    private String adUserString() {
        ADUser adUser = AppComponents.pcUserResolver().adPCSetter();
        return adUser.toString();
    }
}