package ru.vachok.networker.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.services.ArchivesSorter;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 @since 23.11.2018 (11:55) */
@Controller
public class FileCleanerCTRL {

    private ArchivesSorter archivesSorter;

    @Autowired
    public FileCleanerCTRL(ArchivesSorter archivesSorter) {
        this.archivesSorter = archivesSorter;
    }

    @GetMapping("/cleaner")
    public String getFilesInfo(Model model, HttpServletResponse response) {
        Thread.currentThread().setName(getClass().getSimpleName() + "GET");
        model.addAttribute("title", "Инфо о файлах");
        model.addAttribute("archivesSorter", archivesSorter);
        return "cleaner";
    }

    @PostMapping("/cleaner")
    public String postFile(Model model, @ModelAttribute ArchivesSorter archivesSorter) {
        Thread.currentThread().setName(getClass().getSimpleName() + "POST");
        this.archivesSorter = archivesSorter;
        model.addAttribute("archivesSorter", archivesSorter);
        String startPath = archivesSorter.getStartPath();
        model.addAttribute("title", startPath);
        model.addAttribute("call", callMe());
        model.addAttribute("header", new PageFooter().getHeaderUtext());
        model.addAttribute("footer", new PageFooter().getFooterUtext());
        return "cleaner";
    }

    private String callMe() {
        Thread.currentThread().setName(getClass().getSimpleName() + "CALL");
        Future<String> submit = Executors.unconfigurableExecutorService(Executors.newSingleThreadExecutor()).submit(archivesSorter);
        try {
            return submit.get();
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return new TForms().fromArray(e, true);
        }
    }
}
