package ru.vachok.money.filesys;


import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.config.AppComponents;


/**
 Вебфейс работ с файловой системой.

 @since 29.11.2018 (22:48) */
@Controller
public class FilesCTRL {

    /*Fields*/

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = FilesCTRL.class.getSimpleName();

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    private FilesSRV filesSRV = new AppComponents().filesSRV();

    /**
     GET /files

     @param model {@link Model}
     @return files.html
     */
    @GetMapping ("/files")
    public String filesGet(Model model) {
        LOGGER.info("FilesCTRL.filesGet");
        model.addAttribute("title", "File system works");
        model.addAttribute("head", new PageFooter().getHead());
        model.addAttribute("footer", new PageFooter().getTheFooter());
        model.addAttribute("result", filesSRV.getInfo());
        return "files";
    }
}