package ru.vachok.networker.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.abstr.SSHFace;
import ru.vachok.networker.componentsrepo.PageFooter;
import ru.vachok.networker.net.enums.ConstantsNet;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


/**
 Class ru.vachok.networker.controller.OKMaker
 <p>

 @since 06.04.2019 (20:49) */
@Controller
public class OKMaker implements SSHFace {

    @GetMapping("/makeok")
    public String makeOk(Model model , HttpServletRequest request) {
        ConstantsFor.getVis(request);
        StringBuilder stringBuilder = new StringBuilder();
        String connectToSrv = "192.168.13.30";
        if(ConstantsFor.thisPC().toLowerCase().contains("home")) connectToSrv = "192.168.13.42";
        try {
            stringBuilder.append(execCommand(connectToSrv , connectToSrv));
        }
        catch (IndexOutOfBoundsException e) {
            stringBuilder.append(e.getMessage());
        }
        model.addAttribute(ConstantsFor.ATT_TITLE , connectToSrv + " at " + new Date());
        model.addAttribute("ok" , stringBuilder.toString().replace("\n" , "<br>"));
        model.addAttribute(ConstantsFor.ATT_FOOTER , new PageFooter().getFooterUtext());
        return "ok";
    }


    @Override public String execCommand(String connectToSrv , String commandToExec) {
        SSHFactory sshFactory = new SSHFactory.Builder(connectToSrv , commandToExec , this.getClass().getSimpleName()).build();
        StringBuilder stringBuilder = new StringBuilder();

        sshFactory.setCommandSSH(ConstantsNet.COM_INITPF.replace("initpf" , "1915initpf"));
        stringBuilder.append("<i>").append(sshFactory.getCommandSSH()).append(" ||| executing:</i><br>");
        stringBuilder.append(sshFactory.call()).append("<p>");

        sshFactory.setCommandSSH("sudo squid -k reconfigure && exit");
        stringBuilder.append("<i>").append(sshFactory.getCommandSSH()).append(" ||| executing:</i><br>");
        stringBuilder.append(sshFactory.call()).append("<br>");

        sshFactory.setCommandSSH("sudo pfctl -s nat;sudo pfctl -s rules;sudo ps ax | grep squid && exit");
        stringBuilder.append("<i>").append(sshFactory.getCommandSSH()).append(" ||| executing:</i><br>");
        stringBuilder.append(sshFactory.call()).append("<br>");
        return stringBuilder.toString();
    }
}