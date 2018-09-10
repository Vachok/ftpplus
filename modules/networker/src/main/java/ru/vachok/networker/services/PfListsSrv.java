package ru.vachok.networker.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import ru.vachok.mysqlandprops.EMailAndDB.SpeedRunActualize;
import ru.vachok.networker.beans.PfLists;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @since 10.09.2018 (11:49)
 */
public class PfListsSrv {

    private PfLists pfLists;

    @Autowired
    public PfListsSrv(PfLists pfLists) {
        this.pfLists = pfLists;
    }

    public static void speedAct() {
        ScheduledExecutorService executorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        executorService.scheduleWithFixedDelay(new SpeedRunActualize(), 10, 300, TimeUnit.SECONDS);
    }

    public Model pfListsUpd(Model model) {
        model.addAttribute("pfLists", pfLists);
        model.addAttribute("vipnet", pfLists.getVipNet());
        model.addAttribute("squid", pfLists.getStdSquid());
        model.addAttribute("squidlimited", pfLists.getLimitSquid());
        model.addAttribute("tempfull", pfLists.getFullSquid());
        model.addAttribute("allowdomain", pfLists.getAllowDomain());
        model.addAttribute("allowurl", pfLists.getAllowURL());
        return model;
    }
}
