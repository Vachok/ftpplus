package ru.vachok.money.filesys;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.services.TForms;

import java.io.File;
import java.util.List;


/**
 Вебфейс работ с файловой системой.

 @since 29.11.2018 (22:48) */
@Controller
public class FilesCTRL {

    /**
     {@link AppComponents#getLogger()}
     */
    private static final Logger LOGGER = AppComponents.getLogger();

    /**
     {@link AppComponents#filesSRV()}
     */
    private FilesSRV filesSRV;

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
        List<String> stringList = FileSysWorker.readFileAsList(new File("\\\\10.10.111.1\\Torrents-FTP\\velkom_pcuserauto.txt"));
        LOGGER.info("FilesCTRL.filesGet");
        model.addAttribute(ConstantsFor.TITLE, "File system works");
        model.addAttribute(ConstantsFor.ATT_FILES_SRV, filesSRV);
        model.addAttribute(ConstantsFor.RESULT, new TForms().toStringFromArray(stringList, true));
        model.addAttribute("head", new PageFooter().getHead());
        model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getTheFooter());
        return "files";
    }

    /**
     POST /files
     <p>

     @param filesSRV {@link FilesSRV}
     @param model    {@link Model}
     @return files.html
     */
    @PostMapping ("/files")
    public String filesPOST(@ModelAttribute FilesSRV filesSRV, Model model) {
        LOGGER.info("FilesCTRL.filesPOST");
        this.filesSRV = filesSRV;
        model.addAttribute(ConstantsFor.ATT_FILES_SRV, filesSRV);
        String resStr = filesSRV.getInfo();
        model.addAttribute(ConstantsFor.RESULT, resStr);
        return "files";
    }
}