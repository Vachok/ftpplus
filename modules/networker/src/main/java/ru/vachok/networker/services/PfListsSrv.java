package ru.vachok.networker.services;


import org.slf4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.IntoApplication;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.componentsrepo.PfLists;
import ru.vachok.networker.logic.ssh.SSHFactory;

import java.util.Date;


/**
 * @since 10.09.2018 (11:49)
 */
@Service
public class PfListsSrv {


    private static float buildFactoryMetrics;

    private static AnnotationConfigApplicationContext ctx = IntoApplication.getAppCtx();

    private static final Logger LOGGER = AppComponents.getLogger();

    private static Date endDate;

    private PfListsSrv() {
    }

    public static float getBuildFactoryMetrics() {
        return buildFactoryMetrics;
    }

    public static Date getEndDate() {
        return endDate;
    }

    /*Instances*/


    public static void buildFactory() {
        long startMeth = System.currentTimeMillis();

        PfLists pfLists = ctx.getBean(PfLists.class);
        SSHFactory build = new SSHFactory.Builder(ConstantsFor.SRV_NAT, "uname -a;exit").build();
        pfLists.setuName(build.call());

        build.setCommandSSH("sudo cat /etc/pf/vipnet;exit");
        pfLists.setVipNet(build.call());

        build.setCommandSSH("sudo cat /etc/pf/squid;exit");
        pfLists.setStdSquid(build.call());

        build.setCommandSSH("sudo cat /etc/pf/tempfull;exit");
        pfLists.setFullSquid(build.call());

        build.setCommandSSH("sudo cat /etc/pf/squidlimited;exit");
        pfLists.setLimitSquid(build.call());

        build.setCommandSSH("pfctl -s nat;exit");
        pfLists.setPfNat(build.call());

        build.setCommandSSH("pfctl -s rules;exit");
        pfLists.setPfRules(build.call());
        SSHFactory buildGit = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo /etc/stat.script;exit").build();
        long endMeth = System.currentTimeMillis();
        buildGit.call();
        pfLists.setGitStats(new Date(endMeth) + " время обновления\n" +
            new TForms().fromArray(ctx.getBeanDefinitionNames()));
        endDate = new Date(endMeth);
        buildFactoryMetrics = (float) (endMeth - startMeth) / 1000f / 60f;
        String msg = buildFactoryMetrics + " min elapsed";
        LOGGER.info(msg);
        Thread.currentThread().interrupt();
    }
}
