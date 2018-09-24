package ru.vachok.networker.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.componentsrepo.PfLists;
import ru.vachok.networker.controller.PfListsCtr;
import ru.vachok.networker.logic.SSHFactory;

import javax.naming.TimeLimitExceededException;
import java.rmi.UnexpectedException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 <h1>Список-выгрузка с сервера доступа в интернет</h1>

 @since 10.09.2018 (11:49) */
@Service
public class PfListsSrv {

    /**
     {@link PfLists}
     */
    private PfLists pfLists;

    /**
     {@link SSHFactory.Builder}
     */
    private SSHFactory.Builder builder;

    /**
     @param pfLists {@link #pfLists}
     */
    @Autowired
    public PfListsSrv(PfLists pfLists) {
        this.builder = new SSHFactory.Builder(ConstantsFor.SRV_NAT, "uname -a;exit");
        this.pfLists = pfLists;
    }

    /**
     <b>Заполнение форм списка PF</b>
     <p>
     Тащит информацию с сервера pf.
     <p>
     Списки : <br>
     <i>vipnet</i> <br>
     <i>squid</i> <br>
     <i>tempfull</i> <br>
     <i>squidlimited</i> <br>
     <p>
     Также отдаёт информацию напрямую от firewall <br>
     <i>NAT current</i> <br>
     <i>rules current</i>
     <p>

     @see SSHFactory
     @throws UnexpectedException если нет связи с srv-git. Проверка сети.
     */
    public void buildFactory() throws UnexpectedException, TimeLimitExceededException {
        if (!ConstantsFor.isPingOK()) {
            throw new UnexpectedException("No ping");
        }
        InitProperties initProperties =
            new DBRegProperties(ConstantsFor.APP_NAME + PfListsCtr.class.getSimpleName());
        Properties properties = initProperties.getProps();
        long pfscanTime = Long.parseLong(properties.getProperty("pfscan")) + TimeUnit.MINUTES.toMillis(15);
        if(pfscanTime > System.currentTimeMillis()){
            throw new TimeLimitExceededException(
                ( float ) (TimeUnit.MILLISECONDS.toSeconds(pfscanTime - System.currentTimeMillis()))
                    / ConstantsFor.ONE_HOUR_IN_MIN + " min"
            );
        }
        SSHFactory build = builder.build();
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
        pfLists.setTimeUpd(endMeth);
        buildGit.call();
        pfLists.setGitStats(new Date(endMeth).getTime());
        Thread.currentThread().interrupt();
    }
}
