package ru.vachok.networker.controller;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.common.OldBigFilesInfoCollector;
import ru.vachok.networker.componentsrepo.PageFooter;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 @see ru.vachok.networker.controller.FileCleanerCTRLTest
 @since 23.11.2018 (11:55)
 */
@Controller
public class FileCleanerCTRL {

    private static final String MAPPING_CLEANER = "/cleaner";
    
    private OldBigFilesInfoCollector oldBigFilesInfoCollector;
    
    @Contract(pure = true)
    @Autowired
    public FileCleanerCTRL(OldBigFilesInfoCollector oldBigFilesInfoCollector) {
        this.oldBigFilesInfoCollector = oldBigFilesInfoCollector;
    }

    @GetMapping (MAPPING_CLEANER)
    public String getFilesInfo(@NotNull Model model, HttpServletResponse response) {
        model.addAttribute(ConstantsFor.ATT_TITLE, "Инфо о файлах");
        model.addAttribute(ConstantsFor.ATT_BIGOLDFILES, oldBigFilesInfoCollector);
        return "cleaner";
    }

    @PostMapping (MAPPING_CLEANER)
    public String postFile(@NotNull Model model, @ModelAttribute OldBigFilesInfoCollector oldBigFilesInfoCollector) {
    
        this.oldBigFilesInfoCollector = oldBigFilesInfoCollector;
        model.addAttribute(ConstantsFor.ATT_BIGOLDFILES, oldBigFilesInfoCollector);
        String startPath = oldBigFilesInfoCollector.getStartPath();
        model.addAttribute(ConstantsFor.ATT_TITLE, startPath);
        model.addAttribute("call", callMe());
        model.addAttribute("header", new PageFooter().getHeaderUtext());
        model.addAttribute(ConstantsFor.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "cleaner";
    }

    private String callMe() {
        Future<String> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(oldBigFilesInfoCollector);
        try {
            return submit.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TForms().fromArray(e, true);
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FileCleanerCTRL{");
        sb.append("oldBigFilesInfoCollector=").append(oldBigFilesInfoCollector.toString());
        sb.append('}');
        return sb.toString();
    }
}
