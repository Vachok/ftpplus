// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLGeneration;
import ru.vachok.networker.componentsrepo.htmlgen.PageGenerationHelper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ModelAttributeNames;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;


/**
 @see ru.vachok.networker.controller.NoHupOutTest
 @since 06.05.2019 (21:32) */
@RestController
public class NoHupOut {


    private static final int SIZE_TO_SHOW = 500;

    private final HTMLGeneration pageFooter = new PageGenerationHelper();

    private File noHup = new File("nohup.out");

    private File appLog = new File("app.json");

    @GetMapping("/nohup")
    public String noHupGet(@NotNull Model model, HttpServletRequest request, @NotNull HttpServletResponse response) {
        List<String> strings = !noHup.exists() ? getFromAppLog() : FileSystemWorker.readFileToList(noHup.getAbsolutePath());
        Collections.reverse(strings);
        new AppComponents().visitor(request);
        model.addAttribute(ModelAttributeNames.TITLE, System.getProperty("os.name"));
        model.addAttribute(ModelAttributeNames.HEAD, pageFooter.getFooter(ModelAttributeNames.HEAD));

        model.addAttribute("ok", MessageFormat
            .format("Only last {0} strings show<p>{1}", SIZE_TO_SHOW,
                new TForms().fromArray(strings.stream().limit(SIZE_TO_SHOW), true).replace(ConstantsFor.STR_ERROR, "<font color=\"red\">ERROR</font>")
                    .replace("WARN", "<font color=\"yellow\">WARN</font>").replace("INFO", "<font color=\"green\">INFO</font>")
                    .replace("ru.vachok", "<b>ru.vachok</b>")));

        model.addAttribute(ModelAttributeNames.FOOTER, pageFooter.getFooter(ModelAttributeNames.FOOTER));
        response.addHeader(ConstantsFor.HEAD_REFRESH, "60");
        return AbstractForms.fromArray(strings);
    }

    private @NotNull List<String> getFromAppLog() {
        if (!appLog.exists()) {
            throw new UnsupportedOperationException(MessageFormat.format("{0}<p>{1}", System.getProperty("os.name"), new PageGenerationHelper()
                .getAsLink("https://vachok.testquality.com/project/8295", "Test Quality")));
        }
        return FileSystemWorker.readFileToList(appLog.getAbsolutePath());
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