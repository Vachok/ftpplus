package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
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
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.UsefulUtilites;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageFooter;

import javax.servlet.http.HttpServletRequest;


/**
 Страница user.html {@link Controller}
 
 @since 13.02.2019 (15:52) */
@SuppressWarnings("SameReturnValue")
@Controller
public class UserWebCTRL {
    
    
    private static final InformationFactory PAGE_FOOTER = new PageFooter();
    
    /**
     {@link ADSrv#getAdUser()}
     */
    private ADUser adUser = AppComponents.adSrv().getAdUser();
    
    /**
     GET /user
     <p>
     {@link GetMapping} для /user <br>
     1. Записываем визит. {@link UsefulUtilites#getVis(HttpServletRequest)}.
     <p>
     <b>Аттрибуты модели:</b> <br>
     {@link ModelAttributeNames#ATT_ADUSER} - {@link #adUser} <br>
     {@link ModelAttributeNames#ATT_TITLE} - {@link Class#getSimpleName()}<br>
     {@link ModelAttributeNames#ATT_FOOTER} - {@link PageFooter#getFooterUtext()}
     
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @return user.html
     */
    @GetMapping("/user")
    public String userGet(@NotNull Model model, HttpServletRequest request) {
        UsefulUtilites.getVis(request);
        model.addAttribute(ModelAttributeNames.ATT_ADUSER, adUser);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, getClass().getSimpleName());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        return "user";
    }
    
    /**
     {@link PostMapping} /userget
     <p>
     
     <b>Атрибуты {@link Model}</b>:<br>
     {@link ModelAttributeNames#ATT_ADUSER} - {@link ADUser} <br>
     {@link ModelAttributeNames#ATT_TITLE} - {@link UsefulUtilites#getMemoryInfo()} <br>
     {@link ModelAttributeNames#ATT_RESULT} - {@code adUsersEquals}+ {@link ADUser#toString()}.
     Выведем {@code adUsersEquals} через {@link MessageToUser#infoTimer(int, java.lang.String)} <br>
     {@link ModelAttributeNames#ATT_FOOTER} - new {@link PageFooter#getFooterUtext()}
     <p>
     <p>
     {@link ModelAttribute} - {@link ADUser}. прилетающий из компонентов.
     
     @param model {@link Model}
     @param request {@link HttpServletRequest}
     @param adUser {@link ADUser}
     @return user.html
     */
    @PostMapping("/userget")
    public String userPost(@NotNull Model model, HttpServletRequest request, @ModelAttribute ADUser adUser) {
        this.adUser = adUser;
        ADSrv adSrv = adSrvForUser(adUser);
        model.addAttribute(ModelAttributeNames.ATT_ADUSER, adUser);
        model.addAttribute(ModelAttributeNames.ATT_RESULT, adSrv.toString());
        model.addAttribute(ModelAttributeNames.ATT_TITLE, UsefulUtilites.getMemoryInfo());
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, PAGE_FOOTER.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        return "user";
    }
    
    @Scope(ConstantsFor.SINGLETON)
    @Bean
    public static @NotNull ADSrv adSrvForUser(ADUser adUser) {
        ADSrv adSrv = new ADSrv(adUser);
        adSrv.setUserInputRaw(adUser.getInputName());
        return adSrv;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserWebCTRL{");
        sb.append("adUser=").append(adUser.toString());
        sb.append('}');
        return sb.toString();
    }
}
