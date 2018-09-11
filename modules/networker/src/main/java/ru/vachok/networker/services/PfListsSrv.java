package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.beans.AppComponents;
import ru.vachok.networker.beans.PfLists;
import ru.vachok.networker.logic.ssh.SSHFactory;

import java.util.Date;


/**
 * @since 10.09.2018 (11:49)
 */
@Service("pflists")
public class PfListsSrv {

    /*Fields*/
    private static final String SOURCE_CLASS = PfListsSrv.class.getSimpleName();

    private static float buildFactoryMetrics;

    private static AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();
    private static final Logger LOGGER = AppComponents.getLogger();

    private static Date endDate;

    public static float getBuildFactoryMetrics() {
        return buildFactoryMetrics;
    }

    public static Date getEndDate() {
        return endDate;
    }

    /*Instances*/
    public PfListsSrv() {
        buildFactory();
    }

    public void buildFactory() {
        long startMeth = System.currentTimeMillis();
        PfLists pfLists = ctx.getBean(PfLists.class);

        SSHFactory build = new SSHFactory.Builder(ConstantsFor.SRV_NAT, "uname -a").build();
        pfLists.setuName(build.call());

        build.setCommandSSH("sudo cat /etc/pf/vipnet");
        pfLists.setVipNet(build.call());

        build.setCommandSSH("sudo cat /etc/pf/squid");
        pfLists.setStdSquid(build.call());

        build.setCommandSSH("sudo cat /etc/pf/tempfull");
        pfLists.setFullSquid(build.call());

        build.setCommandSSH("sudo cat /etc/pf/squidlimited");
        pfLists.setLimitSquid(build.call());

        build.setCommandSSH("pfctl -s nat");
        pfLists.setPfNat(build.call());

        build.setCommandSSH("pfctl -s rules");
        pfLists.setPfRules(build.call());
        long endMeth = System.currentTimeMillis();
        endDate = new Date(endMeth);
        buildFactoryMetrics = ( float ) (endMeth - startMeth) / 1000f / 60f;
        String msg = buildFactoryMetrics + " min elapsed";
        LOGGER.info(msg);
    }
}
