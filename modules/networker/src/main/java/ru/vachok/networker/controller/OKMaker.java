// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.controller;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.enums.ConstantsNet;
import ru.vachok.networker.enums.ModelAttributeNames;
import ru.vachok.networker.enums.UsefulUtilites;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


/**
 Class ru.vachok.networker.controller.OKMaker
 <p>

 @since 06.04.2019 (20:49) */
@Controller
public class OKMaker {
    
    
    private static final String STR_BR = " ||| executing:</i><br>";
    
    @GetMapping("/makeok")
    public String makeOk(Model model , HttpServletRequest request) {
        UsefulUtilites.getVis(request);
        StringBuilder stringBuilder = new StringBuilder();
        String connectToSrv = "192.168.13.30";
        if (!UsefulUtilites.thisPC().toLowerCase().contains("rups")) {
            connectToSrv = "192.168.13.42";
        }
        try {
            stringBuilder.append(execCommand(connectToSrv , connectToSrv));
        }
        catch (IndexOutOfBoundsException e) {
            stringBuilder.append(e.getMessage());
        }
        model.addAttribute(ModelAttributeNames.ATT_TITLE, connectToSrv + " at " + new Date());
        model.addAttribute("ok" , stringBuilder.toString().replace("\n" , "<br>"));
        model.addAttribute(ModelAttributeNames.ATT_FOOTER, new PageFooter().getFooterUtext());
        return "ok";
    }
    
    private @NotNull String execCommand(String connectToSrv, String commandToExec) {
        SSHFactory sshFactory = new SSHFactory.Builder(connectToSrv , commandToExec , this.getClass().getSimpleName()).build();
        StringBuilder stringBuilder = new StringBuilder();

        sshFactory.setCommandSSH(ConstantsNet.COM_INITPF.replace("initpf" , "1915initpf"));
        stringBuilder.append("<p><i>").append(sshFactory.getCommandSSH()).append(STR_BR);
        stringBuilder.append(sshFactory.call());
    
        sshFactory.setCommandSSH("sudo squid && exit");
        stringBuilder.append("<p><i>").append(sshFactory.getCommandSSH()).append(STR_BR);
        stringBuilder.append(sshFactory.call());
        
        sshFactory.setCommandSSH("sudo squid -k reconfigure && exit");
        stringBuilder.append("<p><i>").append(sshFactory.getCommandSSH()).append(STR_BR);
        stringBuilder.append(sshFactory.call());

        sshFactory.setCommandSSH("sudo pfctl -s nat;sudo pfctl -s rules;sudo ps ax | grep squid && exit");
        stringBuilder.append("<p><i>").append(sshFactory.getCommandSSH()).append(STR_BR);
        stringBuilder.append(sshFactory.call());
        return stringBuilder.toString();
    }
}