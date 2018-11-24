package ru.vachok.money.ctrls;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.money.components.AppFooter;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.config.ThreadConfig;
import ru.vachok.money.services.FilesCleaner;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 @since 27.09.2018 (0:01) */
@Controller
public class SysInfoCtrl {

    @GetMapping ("/sysinfo")
    public String gettingInfo(Model model, HttpServletRequest request) {
        AppVersion appVersion = new AppVersion();

        model.addAttribute("title", appVersion.getAppVBuild());
        model.addAttribute("result", appVersion.toString());
        model.addAttribute("ok", getFilesToClean());
        model.addAttribute("footer", new AppFooter().getTheFooter());
        return "ok";
    }

    private String getFilesToClean() {
        ThreadConfig t = new ThreadConfig();
        Future<String> submit = t.getDefaultExecutor().submit(new FilesCleaner());
        try{
            return submit.get();
        }
        catch(ExecutionException | InterruptedException e){
            Thread.currentThread().interrupt();
            return e.getMessage();
        }
    }
}