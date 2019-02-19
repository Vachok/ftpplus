package ru.vachok.networker.accesscontrol;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 <h1>Список-выгрузка с сервера доступа в интернет</h1>

 @since 10.09.2018 (11:49) */
@Service
public class PfListsSrv {

    /**
     {@link PfLists}
     */
    private PfLists pfListsInstAW;

    /**
     SSH-команда.
     <p>
     При инициализации: {@code uname -a;exit}.

     @see PfListsCtr#runCommand(org.springframework.ui.Model, ru.vachok.networker.accesscontrol.PfListsSrv)
     @see #runCom()
     */
    private String commandForNat = "uname -a;exit";

    /**
     new {@link SSHFactory.Builder}.
     */
    private SSHFactory.Builder builderInst;

    private ThreadPoolTaskExecutor executor;

    /**
     @return {@link #}
     */
    @SuppressWarnings("WeakerAccess")
    public String getCommandForNat() {
        return commandForNat;
    }

    /**
     @param commandForNat {@link #commandForNat}
     */
    public void setCommandForNat(String commandForNat) {
        this.commandForNat = commandForNat;
    }

    /**
     {@link ThreadPoolTaskExecutor}.
     <p>
     @return {@link #executor}
     */
    ThreadPoolTaskExecutor getExecutor() {
        makeListRunner();
        return executor;
    }

    /**
     {@code this.builderInst}
     <p>
     new {@link SSHFactory.Builder} ({@link ConstantsFor#SRV_NAT} , {@link #commandForNat}).

     @param pfLists {@link #pfLists}
     */
    @Autowired
    public PfListsSrv(PfLists pfLists) {
        this.builderInst = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandForNat);
        this.pfListsInstAW = pfLists;
        makeListRunner();

    }

    String runCom() {
        SSHFactory.Builder builder = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandForNat);
        return builder.build().call();
    }

    /**
     Формирует списки <b>pf</b>
     <p>
     Если {@link ConstantsFor#thisPC()} contains {@code rups} - {@link #buildCommands()} <br>
     Else {@link MessageLocal#warn(java.lang.String)} {@link String} = {@link ConstantsFor#thisPC()}
     */
    private void makeListRunner() {
        ThreadConfig threadConfig = AppComponents.threadConfig();
        executor = threadConfig.threadPoolTaskExecutor();
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            executor.execute(this::buildCommands);
        } else {
            new MessageLocal().warn(ConstantsFor.thisPC());
        }
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

     @throws UnexpectedException если нет связи с srv-git. Проверка сети. <i>e: No ping</i>
     @see SSHFactory
     */
    private void buildFactory() throws UnexpectedException, NullPointerException {
        if (!ConstantsFor.isPingOK()) {
            throw new UnexpectedException("No ping");
        }
        SSHFactory build = builderInst.build();
        pfListsInstAW.setuName(build.call());

        build.setCommandSSH("sudo cat /etc/pf/vipnet;exit");
        pfListsInstAW.setVipNet(build.call());

        build.setCommandSSH("sudo cat /etc/pf/squid;exit");
        pfListsInstAW.setStdSquid(build.call());

        build.setCommandSSH("sudo cat /etc/pf/tempfull;exit");
        pfListsInstAW.setFullSquid(build.call());

        build.setCommandSSH("sudo cat /etc/pf/squidlimited;exit");
        pfListsInstAW.setLimitSquid(build.call());

        build.setCommandSSH("pfctl -s nat;exit");
        pfListsInstAW.setPfNat(build.call());

        build.setCommandSSH("pfctl -s rules;exit");
        pfListsInstAW.setPfRules(build.call());
        SSHFactory buildGit = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo /etc/stat.script;exit").build();
        long endMeth = System.currentTimeMillis();
        pfListsInstAW.setTimeUpd(endMeth);
        buildGit.call();
        pfListsInstAW.setGitStats(new Date(endMeth).getTime());
        Thread.currentThread().interrupt();
    }

    /**
     Строитель команд.
     <p>
     {@link PfListsSrv#buildFactory()} <br>
     <b>{@link Exception}:</b><br>
     {@link FileSystemWorker#recFile(java.lang.String, java.util.List)} - {@code this.getClass().getSimpleName() + ".makeListRunner", stringArrayList}
     */
    private void buildCommands() {
        try {
            buildFactory();
        } catch (Exception e) {
            List<String> stringArrayList = new ArrayList<>();
            stringArrayList.add("Line 150 threw: ");
            stringArrayList.add(e.getMessage());
            stringArrayList.add(new TForms().fromArray(e, false));
            FileSystemWorker.recFile(this.getClass().getSimpleName() + ".makeListRunner", stringArrayList);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfListsSrv{");
        sb.append("pfLists=").append(pfListsInstAW.hashCode());
        sb.append(", commandForNat='").append(commandForNat).append('\'');
        sb.append(", builderInst=").append(builderInst);
        sb.append(", executor=").append(executor);
        sb.append('}');
        return sb.toString();
    }
}
