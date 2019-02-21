package ru.vachok.networker.accesscontrol;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.rmi.UnexpectedException;


/**
 <h1>Список-выгрузка с сервера доступа в интернет</h1>

 @since 10.09.2018 (11:49) */
@Service
public class PfListsSrv {

    /**
     SSH-команда.
     <p>
     При инициализации: {@code uname -a;exit}.

     @see PfListsCtr#runCommand(org.springframework.ui.Model, ru.vachok.networker.accesscontrol.PfListsSrv)
     @see #runCom()
     */
    private @NotNull String commandForNatStr = "uname -a;exit";

    private @NotNull MessageToUser messageToUser = new MessageLocal();

    /**
     {@link PfLists}
     */
    private final @NotNull PfLists pfListsInstAW;

    /**
     {@link AppComponents#threadConfig()}
     */
    private final @NotNull ThreadPoolTaskExecutor executor = AppComponents.threadConfig().getTaskExecutor();

    /**
     new {@link SSHFactory.Builder}.
     */
    private final @NotNull SSHFactory.Builder builderInst = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandForNatStr);

    /**
     {@link #commandForNatStr}
     */
    @SuppressWarnings ("WeakerAccess")
    public @NotNull String getCommandForNatStr() {
        return commandForNatStr;
    }

    /**
     @param commandForNatStr {@link #commandForNatStr}
     */
    @SuppressWarnings("unused")
    public void setCommandForNatStr(@NotNull String commandForNatStr) {
        this.commandForNatStr = commandForNatStr;
    }

// --Commented out by Inspection START (20.02.2019 12:13):
//    /**
//     {@link ThreadPoolTaskExecutor}.
//     <p>
//
//     @return {@link #executor}
//     */
//    ThreadPoolTaskExecutor getExecutor() {
//
//        return executor;
//    }
// --Commented out by Inspection STOP (20.02.2019 12:13)

    /**
     {@code this.builderInst}
     <p>
     new {@link SSHFactory.Builder} ({@link ConstantsFor#SRV_NAT} , {@link #commandForNatStr}).

     @param pfLists {@link #pfListsInstAW}
     */
    @Autowired
    public PfListsSrv(@NotNull PfLists pfLists) {
        this.pfListsInstAW = pfLists;
        makeListRunner();
    }

    String runCom() {
        SSHFactory.Builder builder = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandForNatStr);
        return builder.build().call();
    }

    /**
     Формирует списки <b>pf</b>
     <p>
     Если {@link ConstantsFor#thisPC()} contains {@code rups} - {@link #buildCommands()} через {@link #executor} <br>
     Else {@link MessageLocal#warn(java.lang.String)} {@link String} = {@link ConstantsFor#thisPC()}
     */
    void makeListRunner() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            executor.execute(this::buildCommands);

            messageToUser
                .info(this.getClass().getSimpleName(), executor.getThreadNamePrefix() + " executor", executor.getThreadPoolExecutor().getCompletedTaskCount() + " Completed Tasks");
        } else {
            try{
                messageToUser = new MessageToTray();
            }
            catch(ExceptionInInitializerError ignore){
                messageToUser = new MessageLocal();
            }
            messageToUser.info(this.getClass().getSimpleName(), "NOT RUNNING ON RUPS!", ConstantsFor.thisPC() + " buildCommands " + false);
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
    private void buildFactory() throws UnexpectedException {
        SSHFactory build = builderInst.build();
        SSHFactory buildGit = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo /etc/stat.script;exit").build();

        if (!ConstantsFor.isPingOK()) {
            throw new UnexpectedException("No ping to " + ConstantsFor.SRV_GIT + " cancelling execution");
        }
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

        String callToStatScript = buildGit.call();
        messageToUser.info("PfListsSrv.buildFactory", "callToStatScript", callToStatScript);

        pfListsInstAW.setGitStatsUpdatedStampLong(System.currentTimeMillis());
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
        } catch (UnexpectedException e) {
            FileSystemWorker.error("PfListsSrv.buildCommands", e);
        }
    }

    @Override
    public @NotNull String toString() {
        final StringBuilder sb = new StringBuilder("PfListsSrv{");
        sb.append("pfListsInstAW=").append(pfListsInstAW.hashCode());
        sb.append(", commandForNatStr='").append(commandForNatStr).append('\'');
        sb.append(", messageToUser=").append(messageToUser.toString());
        sb.append(", builderInst=").append(builderInst.hashCode());
        sb.append(", executor=").append(executor.getActiveCount());
        sb.append('}');
        return sb.toString();
    }

}
