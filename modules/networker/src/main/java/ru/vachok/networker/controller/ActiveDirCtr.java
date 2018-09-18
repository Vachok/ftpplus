package ru.vachok.networker.controller;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.PhotoConverter;
import ru.vachok.networker.services.VisitorSrv;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * @since 30.08.2018 (10:10)
 */
@Controller
public class ActiveDirCtr {

    private static AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();

    private VisitorSrv visitorSrv = ctx.getBean(VisitorSrv.class);

    private static final Logger LOGGER = AppComponents.getLogger();

    private PhotoConverter photoConverter;

    @GetMapping("/ad")
    public String initUser(Model model, HttpServletRequest request) {
        this.photoConverter = new PhotoConverter();
        visitorSrv.makeVisit(request);
        model.addAttribute("title", visitorSrv.toString());
        model.addAttribute("photoConverter", photoConverter);
        return "ad";
    }

    @PostMapping("/ad")
    public String setFotoPath(@ModelAttribute("photoConverter") PhotoConverter photoConverter, BindingResult result, Model model) throws NullPointerException {
        this.photoConverter = photoConverter;
        try {
            model.addAttribute("title", "RESULT");
            model.addAttribute("photoConverter", photoConverter);
            model.addAttribute("pscommands", photoConverter.psCommands());
        } catch (NullPointerException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            model.addAttribute("error", "Не верно указан путь до папки с png-файлами.");
            return "error";
        }
        return "ad";
    }
}
