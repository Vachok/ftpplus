package ru.vachok.networker.controller;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ad.ADSrv;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;


/**
 Страница user.html {@link Controller}

 @since 13.02.2019 (15:52) */
@SuppressWarnings ("SameReturnValue")
@Controller
public class UserWebCTRL {

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
     {@link ModelAttributeNames#ATT_ADUSER} - {@link #adUser} <br>
     {@link ModelAttributeNames#ATT_TITLE} - {@link Class#getSimpleName()}<br>
     {@link ModelAttributeNames#ATT_FOOTER} - {@link PageFooter#getFooterUtext()}

     @param model   {@link Model}
     @param request {@link HttpServletRequest}
     @return user.html
     */
    @GetMapping("/user")
    public String userGet(Model model, HttpServletRequest request) {
        ConstantsFor.getVis(request);
        model.addAttribute(ModelAttributeNames.ATT_ADUSER, adUser);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, getClass().getSimpleName());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "user";
    }

    /**
     {@link PostMapping} /userget
     <p>

     <b>Атрибуты {@link Model}</b>:<br>
     {@link ModelAttributeNames#ATT_ADUSER} - {@link ADUser} <br>
     {@link ModelAttributeNames#ATT_TITLE} - {@link ConstantsFor#getMemoryInfo()} <br>
     {@link ModelAttributeNames#ATT_RESULT} - {@code adUsersEquals}+ {@link ADUser#toString()}.
     Выведем {@code adUsersEquals} через {@link MessageToUser#infoTimer(int, java.lang.String)} <br>
     {@link ModelAttributeNames#ATT_FOOTER} - new {@link PageFooter#getFooterUtext()}
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
        ADSrv adSrv = adSrvForUser(adUser);
        model.addAttribute(ModelAttributeNames.ATT_ADUSER, adUser);
        model.addAttribute(ModelAttributeNames.ATT_RESULT, adSrv.toString());
        model.addAttribute(ModelAttributeNames.ATT_TITLE, ConstantsFor.getMemoryInfo());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "user";
    }

    @Bean
    @Scope (ConstantsFor.SINGLETON)
    public static ADSrv adSrvForUser(ADUser adUser) {
        ADSrv adSrv = new ADSrv(adUser);
        adSrv.setUserInputRaw(adUser.getInputName());
        return adSrv;
    }
}
