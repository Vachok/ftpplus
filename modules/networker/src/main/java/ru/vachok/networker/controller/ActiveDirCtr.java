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
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.ADUser;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.PhotoConverterSRV;
import ru.vachok.networker.services.VisitorSrv;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;


/**
 * @since 30.08.2018 (10:10)
 */
@Controller
public class ActiveDirCtr {

    private static AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();

    private VisitorSrv visitorSrv = ctx.getBean(VisitorSrv.class);

    private static final Logger LOGGER = AppComponents.getLogger();

    private ADUser adUser = ctx.getBean(ADUser.class);

    private PhotoConverterSRV photoConverter;

    @GetMapping("/ad")
    public String initUser(Model model, HttpServletRequest request) {
        this.photoConverter = new PhotoConverterSRV();
        visitorSrv.makeVisit(request);
        model.addAttribute("title", visitorSrv.toString());
        model.addAttribute("photoConverter", photoConverter);

        return "ad";
    }

    @PostMapping("/ad")
    public String setFotoPath(@ModelAttribute("photoConverter") PhotoConverterSRV photoConverter, BindingResult result, Model model) throws NullPointerException {
        this.photoConverter = photoConverter;
        try {
            model.addAttribute("title", "RESULT");
            model.addAttribute("photoConverter", photoConverter);
            model.addAttribute("pscommands", new TForms().fromArray(photoConverter.psCommands()));
            photoShow(model);
        } catch (NullPointerException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            model.addAttribute("error", "Не верно указан путь до папки с файлами.");
            return "error";
        }
        return "ad";
    }

    private Model photoShow(Model model) {
        BufferedImage userPhoto = adUser.getUserPhoto();
        try {
            File output = new File("C:\\Users\\ikudryashov\\IdeaProjects\\spring\\modules\\networker\\src\\main\\resources\\static\\images\\" + new Date().getTime() + ".jpg");
            ImageIO.write(userPhoto, "jpg", output);
            model.addAttribute("userimage", "images/" + output.getName());
            return model;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            model.addAttribute("userimage", e.getMessage());
            return model;
        }
    }
}
