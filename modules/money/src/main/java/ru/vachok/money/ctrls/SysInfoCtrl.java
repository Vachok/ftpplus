package ru.vachok.money.ctrls;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.components.PageFooter;
import ru.vachok.money.config.ThreadConfig;
import ru.vachok.money.services.DBMessage;
import ru.vachok.money.services.FilesCleaner;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 @since 27.09.2018 (0:01) */
@Controller
public class SysInfoCtrl {

    private ThreadConfig t = new ThreadConfig();

    @GetMapping ("/sysinfo")
    public String gettingInfo(Model model, HttpServletRequest request) {
        AppVersion appVersion = new AppVersion();

        model.addAttribute("title", appVersion.getAppVBuild());
        model.addAttribute("result", appVersion.toString());
        model.addAttribute("ok",
            "<a href=\"/cleandir\">home</a><br>");
        model.addAttribute("footer", new PageFooter().getTheFooter());
        //        t.getDefaultExecutor().submit(new FilesCleaner());
        return "ok";
    }

    @GetMapping ("/cleandir")
    public String cleanDirectory(Model model) {
        FilesCleaner filesCleanerHome = new FilesCleaner("\\\\10.10.111.1\\Torrents-FTP\\home\\", true);
        String toClean = getFilesToClean(filesCleanerHome);

        model.addAttribute("title", "Cleaning: " + filesCleanerHome.getStartDir());
        model.addAttribute("result", toClean);
        model.addAttribute("footer", new PageFooter().getTheFooter());

        MessageToUser messageToUser = new DBMessage();
        messageToUser.infoNoTitles(toClean + "\n\n");

        return "ok";
    }

    private String getFilesToClean(FilesCleaner filesCleaner) {
        Future<String> submit = t.getDefaultExecutor().submit(filesCleaner);
        try{
            return submit.get();
        }
        catch(ExecutionException | InterruptedException e){
            Thread.currentThread().interrupt();
            return e.getMessage();
        }
    }
}