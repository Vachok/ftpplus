// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad;


import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;


/**
 @see ru.vachok.networker.ad.UserWebCTRLTest
 @since 13.02.2019 (15:52) */
@SuppressWarnings("SameReturnValue")
@Controller
public class UserWebCTRL {
    
    private static final HTMLGeneration PAGE_FOOTER = new PageGenerationHelper();
    
    @GetMapping("/user")
    public String userGet(@NotNull Model model, HttpServletRequest request) {
        UsefulUtilities.getVis(request);
        
        model.addAttribute(ModelAttributeNames.TITLE, getClass().getSimpleName());
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
        return "user";
    }
    
    @PostMapping("/userget")
    public String userPost(@NotNull Model model, HttpServletRequest request, @ModelAttribute ADUser adUser) {
        ADSrv adSrv = adSrvForUser(adUser);
        model.addAttribute(ModelAttributeNames.ADUSER, adUser);
        model.addAttribute(ModelAttributeNames.ATT_RESULT, adSrv.toString());
        model.addAttribute(ModelAttributeNames.TITLE, ModelAttributeNames.USERWEB);
        model.addAttribute(ModelAttributeNames.FOOTER, PAGE_FOOTER.getFooter(ModelAttributeNames.FOOTER));
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
        sb.append('}');
        return sb.toString();
    }
}
