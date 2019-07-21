package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.Common2Years25MbytesInfoCollector;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 @since 23.11.2018 (11:55) */
@Controller
public class FileCleanerCTRL {

    private static final String MAPPING_CLEANER = "/cleaner";
    
    private Common2Years25MbytesInfoCollector common2Years25MbytesInfoCollector;

    @Autowired
    public FileCleanerCTRL(Common2Years25MbytesInfoCollector common2Years25MbytesInfoCollector) {
        this.common2Years25MbytesInfoCollector = common2Years25MbytesInfoCollector;
    }

    @GetMapping (MAPPING_CLEANER)
    public String getFilesInfo(Model model, HttpServletResponse response) {
        model.addAttribute(ConstantsFor.ATT_TITLE, "Инфо о файлах");
        model.addAttribute("common2Years25MbytesInfoCollector", common2Years25MbytesInfoCollector);
        return "cleaner";
    }

    @PostMapping (MAPPING_CLEANER)
    public String postFile(Model model, @ModelAttribute Common2Years25MbytesInfoCollector common2Years25MbytesInfoCollector) {
    
        this.common2Years25MbytesInfoCollector = common2Years25MbytesInfoCollector;
        model.addAttribute("common2Years25MbytesInfoCollector", common2Years25MbytesInfoCollector);
        String startPath = common2Years25MbytesInfoCollector.getStartPath();
        model.addAttribute(ConstantsFor.ATT_TITLE, startPath);
        model.addAttribute("call", callMe());
        model.addAttribute("header", new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "cleaner";
    }

    private String callMe() {
        Future<String> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(common2Years25MbytesInfoCollector);
        try {
            return submit.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TForms().fromArray(e, true);
        }
    }
}
