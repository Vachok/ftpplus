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
import java.util.List;


/**
 @since 02.10.2018 (23:06) */
@Controller
public class ActDirectoryCTRL {

    private static ADComputer adComputer;

    /*Instances*/
    @Autowired
    public ActDirectoryCTRL(ADComputer adComputer) {
        ActDirectoryCTRL.adComputer = adComputer;
    }

    @GetMapping ("/ad")
    public String adUsersComps(HttpServletRequest request, Model model) {
        if(ConstantsFor.getPcAuth(request)){
            model.addAttribute("footer", new PageFooter().getFooterUtext());
            model.addAttribute("pcs", new TForms().adPCMap(adComputer.getAdComputers(), true));
            model.addAttribute("users", adUserString());
        }
        else{
            return adFoto(model);
        }
        return "ok";
    }

    private String adFoto(Model model) {
        ADSrv adSrv = AppComponents.adSrv();
        adSrv.run();
        List<ADComputer> adComputers = adSrv.getAdComputer().getAdComputers();
        List<ADUser> adUsers = adSrv.getAdUser().getAdUsers();
        StringBuilder stringBuilder = new StringBuilder();
        model.addAttribute("pcs", new TForms().adPCMap(adComputers, false));
        model.addAttribute("users", new TForms().adUsersMap(adUsers, false));
        adComputers.forEach((x -> stringBuilder.append(x.toString())));
        stringBuilder.append("<br>");
        return "ok";
    }

    private String adUserString() {
        ADUser adUser = AppComponents.pcUserResolver().adPCSetter();
        return adUser.toString();
    }

}