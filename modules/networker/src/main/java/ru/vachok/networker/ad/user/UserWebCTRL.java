package ru.vachok.networker.ad.user;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 Страница user.html {@link Controller}

 @since 13.02.2019 (15:52) */
@Controller
public class UserWebCTRL {

    private ADUser adUser = new ADUser();

    private ADSrv adSrv;

    /**
     GET /user
     <p>
     {@link GetMapping} для /user <br> 1. Записываем визит. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}.
     <p>
     <b>Аттрибуты модели:</b> <br>
     "aduser" - {@link #adUser} <br> {@link ConstantsFor#ATT_TITLE} - {@link Class#getSimpleName()}<br>

     @param model    {@link Model}
     @param response {@link HttpServletResponse}
     @param request  {@link HttpServletRequest}
     @return user.html
     */
    @GetMapping("/user")
    public String userGet(Model model, HttpServletResponse response, HttpServletRequest request) {
        ConstantsFor.getVis(request);
        model.addAttribute("aduser", adUser);
        model.addAttribute(ConstantsFor.ATT_TITLE, getClass().getSimpleName());
        return "user";
    }

    /**
     {@link PostMapping} /userget
     <p>

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @return user.html
     */
    @PostMapping("/userget")
    public String userPost(Model model, HttpServletRequest request, @ModelAttribute ADUser adUser) {
        this.adUser = adUser;
        this.adSrv = new ADSrv(adUser);
        model.addAttribute("aduser", adSrv.getAdUser());
        model.addAttribute(ConstantsFor.ATT_RESULT, adSrv.getAdUser().toStringBR());
        model.addAttribute(ConstantsFor.ATT_TITLE, adUser.equals(this.adUser));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "user";
    }
}
