// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.fileworks.FileSystemWorker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Collections;
import java.util.List;


/**
 Class ru.vachok.networker.controller.NoHupOut
 <p>
 
 @since 06.05.2019 (21:32) */
@Controller
public class NoHupOut {
    
    
    private File noHup = new File("nohup.out");
    
    @GetMapping("/nohup")
    public String noHupGet(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (!noHup.exists()) {
            throw new UnsupportedOperationException(System.getProperty("os.name"));
        }
        List<String> strings = FileSystemWorker.readFileToList(noHup.getAbsolutePath());
        Collections.reverse(strings);
        new AppComponents().visitor(request);
        model.addAttribute(ConstantsFor.ATT_TITLE, System.getProperty("os.name"));
        model.addAttribute(ConstantsFor.ATT_HEAD, new PageFooter().getHeaderUtext());
        model.addAttribute("ok", new TForms().fromArray(strings, true).replace(ConstantsFor.STR_ERROR, "<font color=\"red\">ERROR</font>")
            .replace("WARN", "<font color=\"yellow\">WARN</font>").replace("INFO", "<font color=\"green\">INFO</font>")
            .replace("ru.vachok", "<b>ru.vachok</b>"));
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        response.addHeader(ConstantsFor.HEAD_REFRESH, "15");
        return "ok";
    }
    
}