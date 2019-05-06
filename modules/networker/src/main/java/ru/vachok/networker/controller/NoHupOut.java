// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.servlet.http.HttpServletRequest;
import java.io.File;


/**
 Class ru.vachok.networker.controller.NoHupOut
 <p>
 
 @since 06.05.2019 (21:32) */
@Controller
public class NoHupOut {
    
    
    private File noHup = new File("nohup.out");
    
    @GetMapping("/nohup")
    public String noHupGet(Model model, HttpServletRequest request) {
        if (!noHup.exists()) {
            throw new UnsupportedOperationException(System.getProperty("os.name"));
        }
        new AppComponents().visitor(request);
        model.addAttribute(ConstantsFor.ATT_TITLE, System.getProperty("os.name"));
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute("ok", FileSystemWorker.readFile(noHup.getAbsolutePath()));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
    
}