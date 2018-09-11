package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.beans.AppComponents;
import ru.vachok.networker.beans.PfLists;
import ru.vachok.networker.config.AppCtx;
import ru.vachok.networker.logic.ssh.ListInternetUsers;
import ru.vachok.networker.logic.ssh.SSHFactory;

import java.util.List;

/**
 * @since 10.09.2018 (11:49)
 */
@Service("pflists")
public class PfListsSrv {

    private static final Logger LOGGER = AppComponents.getLogger();

    private static PfLists pfLists = AppCtx.getConfigApplicationContext().getBean(PfLists.class);

    private static List<String> sshCom = new ListInternetUsers().getCommands();

    @Autowired
    public PfListsSrv() {
        getInfoFromSSH();
    }

    public static PfLists getPfLists() {
        return pfLists;
    }

    public static void setPfLists(PfLists pfLists) {
        PfListsSrv.pfLists = pfLists;
    }

    private static String buildFactory(String comSSH) {
        SSHFactory build = new SSHFactory.Builder(ConstantsFor.SRV_NAT, comSSH).build();
        return build.call();
    }

    private void getInfoFromSSH() {
        pfLists.setStdSquid(buildFactory("sudo cat /etc/pf/vipnet"));
    }

}
