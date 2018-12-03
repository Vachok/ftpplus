package ru.vachok.networker.accesscontrol;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PageFooter;

/**
 SSH-actions class

 @since 29.11.2018 (13:01) */
@Service("sshActs")
public class SshActs {

    private static final String PC_NAME = AppComponents.adSrv().getAdComputer().toString();

    private String tempFull;

    @SuppressWarnings("WeakerAccess")
    public String getTempFull() {
        return tempFull;
    }

    public void setTempFull(String tempFull) {
        this.tempFull = tempFull;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SshActs{");
        sb.append("PC_NAME='").append(PC_NAME).append('\'');
        sb.append(", tempFull=").append(tempFull);
        sb.append('}');
        return sb.toString();
    }

    /**
     {@link Controller}, для работы с SSH

     @since 01.12.2018 (9:58)
     */
    @Controller
    public class SshActsCTRL {

        private SshActs sshActs;

        @Autowired
        public SshActsCTRL(SshActs sshActs) {
            this.sshActs = sshActs;
        }

        @PostMapping("/sshacts")
        public String sshActsPOST(@ModelAttribute SshActs sshActs, Model model) {
            this.sshActs = sshActs;
            model.addAttribute("sshActs", sshActs);
            model.addAttribute("tempFull", sshActs.getTempFull());
            model.addAttribute("sshdetail", sshActs.toString());
            return "sshworks";
        }

        @GetMapping("/sshacts")
        public String sshActsGET(Model model) {
            model.addAttribute(ConstantsFor.TITLE, "SSH Works");
            model.addAttribute(ConstantsFor.FOOTER, new PageFooter().getFooterUtext());
            model.addAttribute("sshActs", sshActs);
            model.addAttribute("sshdetail", sshActs.toString());
            return "sshworks";
        }
    }
}
