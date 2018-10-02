package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.*;
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

}