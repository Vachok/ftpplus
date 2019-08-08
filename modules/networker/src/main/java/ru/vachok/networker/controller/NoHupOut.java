// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.info.InformationFactory;
import ru.vachok.networker.info.PageFooter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;


/**
 Class ru.vachok.networker.controller.NoHupOut
 <p>
 
 @since 06.05.2019 (21:32) */
@Controller
public class NoHupOut {
    
    
    private static final int SIZE_TO_SHOW = 500;
    
    private final InformationFactory pageFooter = new PageFooter();
    
    private File noHup = new File("nohup.out");
    
    @GetMapping("/nohup")
    public String noHupGet(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (!noHup.exists()) {
            throw new UnsupportedOperationException(System.getProperty("os.name"));
        }
        List<String> strings = FileSystemWorker.readFileToList(noHup.getAbsolutePath());
        Collections.reverse(strings);
        new AppComponents().visitor(request);
        model.addAttribute(ModelAttributeNames.ATT_TITLE, System.getProperty("os.name"));
        model.addAttribute(ModelAttributeNames.ATT_HEAD, pageFooter.getInfoAbout(ModelAttributeNames.ATT_HEAD));
    
        model.addAttribute("ok", MessageFormat
            .format("Only last {0} strings show<p>{1}", SIZE_TO_SHOW,
                new TForms().fromArray(strings.stream().limit(SIZE_TO_SHOW), true).replace(ConstantsFor.STR_ERROR, "<font color=\"red\">ERROR</font>")
            .replace("WARN", "<font color=\"yellow\">WARN</font>").replace("INFO", "<font color=\"green\">INFO</font>")
                    .replace("ru.vachok", "<b>ru.vachok</b>")));
    
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, pageFooter.getInfoAbout(ModelAttributeNames.ATT_FOOTER));
        response.addHeader(ConstantsFor.HEAD_REFRESH, "15");
        return "ok";
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NoHupOut{");
        sb.append("pageFooter=").append(pageFooter);
        sb.append(", noHup=").append(noHup);
        sb.append('}');
        return sb.toString();
    }
}