package ru.vachok.money.filesys;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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

    private FilesSRV filesSRV;

    /*Instances*/
    @Autowired
    public FilesCTRL(FilesSRV filesSRV) {
        this.filesSRV = filesSRV;
    }

    /**
     GET /files

     @param model {@link Model}
     @return files.html
     */
    @GetMapping ("/files")
    public String filesGet(Model model) {
        LOGGER.info("FilesCTRL.filesGet");
        model.addAttribute("title", "File system works");
        model.addAttribute("filesSrv", filesSRV);
        model.addAttribute("head", new PageFooter().getHead());
        model.addAttribute("footer", new PageFooter().getTheFooter());
        return "files";
    }

    @PostMapping ("/files")
    public String filesPOST(@ModelAttribute FilesSRV filesSRV, Model model) {
        LOGGER.info("FilesCTRL.filesPOST");
        this.filesSRV = filesSRV;
        model.addAttribute("filesSrv", filesSRV);
        String resStr = filesSRV.getInfo();
        model.addAttribute("result", resStr);
        return "files";
    }
}