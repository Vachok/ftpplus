package ru.vachok.networker.ad.user;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletRequest;


/**
 Страница user.html {@link Controller}

 @since 13.02.2019 (15:52) */
@Controller
public class UserWebCTRL {

    /**
     {@link MessageSwing}
     */
    private static MessageToUser messageToUser = new MessageSwing();

    /**
     {@link ADSrv#getAdUser()}
     */
    private ADUser adUser = AppComponents.adSrv().getAdUser();

    /**
     GET /user
     <p>
     {@link GetMapping} для /user <br>
     1. Записываем визит. {@link ConstantsFor#getVis(javax.servlet.http.HttpServletRequest)}.
     <p>
     <b>Аттрибуты модели:</b> <br>
     {@link ConstantsFor#ATT_ADUSER} - {@link #adUser} <br>
     {@link ConstantsFor#ATT_TITLE} - {@link Class#getSimpleName()}<br>
     {@link ConstantsFor#ATT_FOOTER} - {@link PageFooter#getFooterUtext()}

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @return user.html
     */
    @GetMapping("/user")
    public String userGet(Model model, HttpServletRequest request) {
        ConstantsFor.getVis(request);
        model.addAttribute(ConstantsFor.ATT_ADUSER, adUser);
        model.addAttribute(ConstantsFor.ATT_TITLE, getClass().getSimpleName());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "user";
    }

    /**
     {@link PostMapping} /userget
     <p>

     <b>Атрибуты {@link Model}</b>:<br>
     {@link ConstantsFor#ATT_ADUSER} - {@link ADUser} <br>
     {@link ConstantsFor#ATT_TITLE} - {@link ConstantsFor#getMemoryInfo()} <br>
     {@link ConstantsFor#ATT_RESULT} - {@code adUsersEquals}+ {@link ADUser#toStringBR()}.
     Выведем {@code adUsersEquals} через {@link MessageToUser#infoTimer(int, java.lang.String)} <br>
     {@link ConstantsFor#ATT_FOOTER} - new {@link PageFooter#getFooterUtext()}
     <p>

     {@link ModelAttribute} - {@link ADUser}. прилетающий из компонентов.

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @param adUser  {@link ADUser}
     @return user.html
     */
    @PostMapping("/userget")
    public String userPost(Model model, HttpServletRequest request, @ModelAttribute ADUser adUser) {
        this.adUser = adUser;
        ADSrv adSrv = AppComponents.adSrv(adUser);
        String adUsersEquals = "ModelAttribute adUser.equals(this.adUser): " + adUser.equals(this.adUser) + "<br> adSrv.getAdUser.equals this.adUser: "
            + adSrv.getAdUser().equals(this.adUser) + "<p>";
        messageToUser.infoTimer(60, adUsersEquals);

        model.addAttribute(ConstantsFor.ATT_ADUSER, adSrv.getAdUser());
        model.addAttribute(ConstantsFor.ATT_RESULT, adUsersEquals + adUser.toString());
        model.addAttribute(ConstantsFor.ATT_TITLE, ConstantsFor.getMemoryInfo());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "user";
    }
}
