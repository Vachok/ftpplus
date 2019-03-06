package ru.vachok.networker.accesscontrol;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.io.File;
import java.util.concurrent.RejectedExecutionException;


/**
 Список-выгрузка с сервера доступа в интернет

 @since 10.09.2018 (11:49) */
@Service
public class PfListsSrv {

    private static final String DEFAULT_CONNECT_SRV = whatSrv();

    /**
     {@link PfLists}
     */
    @SuppressWarnings("CanBeFinal")
    private @NotNull PfLists pfListsInstAW;

    /**
     SSH-команда.
     <p>
     При инициализации: {@code uname -a;exit}.

     @see PfListsCtr#runCommand(org.springframework.ui.Model, ru.vachok.networker.accesscontrol.PfListsSrv)
     @see #runCom()
     */
    private @NotNull String commandForNatStr = "sudo cat /etc/pf/24hrs;exit";

    /**
     new {@link SSHFactory.Builder}.
     */
    @SuppressWarnings("CanBeFinal")
    private @NotNull SSHFactory.Builder builderInst;

    /**
     @return {@link #commandForNatStr}
     */
    @SuppressWarnings("WeakerAccess")
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

    /**
     {@code this.builderInst}
     <p>
     new {@link SSHFactory.Builder} ({@link ConstantsFor#IPADDR_SRVNAT} , {@link #commandForNatStr}).

     @param pfLists {@link #pfListsInstAW}
     */
    @Autowired
    public PfListsSrv(@NotNull PfLists pfLists) {
        this.pfListsInstAW = pfLists;
        AppComponents.threadConfig().thrNameSet("pfsrv");
    }

    private static String whatSrv() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return ConstantsFor.IPADDR_SRVNAT;
        } else {
            return ConstantsFor.IPADDR_SRVGIT;
        }
    }

    /**
     Формирует списки <b>pf</b>
     <p>
     Else {@link MessageToTray#warn(String, String, String)} {@link String} = {@link ConstantsFor#thisPC()}.
     <p>
     {@link ExceptionInInitializerError} : <br>
     {@link MessageLocal#warn(String, String, String)}

     @see PfListsCtr
     */
    void makeListRunner() {
        if (ConstantsNet.IS_RUPS) {
            AppComponents.threadConfig().executeAsThread(this::buildFactory);
        } else {
            @NotNull MessageToUser messageToUser;
            try {
                messageToUser = new MessageToTray();
            } catch (ExceptionInInitializerError ignore) {
                messageToUser = new MessageLocal();
            }
            messageToUser.warn(this.getClass().getSimpleName(), "NOT RUNNING ON RUPS!", ConstantsFor.thisPC() + " buildCommands " + false);
        }
    }

    String runCom() {
        return new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName()).build().call();
    }

    /**
     <b>Заполнение форм списка PF</b>
     <p>
     Тащит информацию с сервера pf. Заполняет поля {@link PfListsSrv#pfListsInstAW}
     <p>
     Списки : <br>
     <i>vipnet</i> <br>
     <i>squid</i> <br>
     <i>tempfull</i> <br>
     <i>squidlimited</i> <br>
     <p>
     Также отдаёт информацию напрямую от firewall <br>
     <i>NAT current</i> <br>
     <i>rules current</i> <br>
     <i>/home/kudr/inet.log</i>
     */
    private void buildFactory() {
        AppComponents.threadConfig().thrNameSet("pfmake");
        this.builderInst = new SSHFactory.Builder(DEFAULT_CONNECT_SRV, commandForNatStr, getClass().getSimpleName());
        SSHFactory build = builderInst.build();
        if (!new File("a161.pem").exists()) {
            throw new RejectedExecutionException("NO CERTIFICATE a161.pem...");
        }

        build.setCommandSSH("sudo cat /etc/pf/vipnet;sudo cat /etc/pf/24hrs;exit");
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

        build.setCommandSSH("sudo cat /home/kudr/inet.log;exit");
        pfListsInstAW.setInetLog(build.call());

        pfListsInstAW.setGitStatsUpdatedStampLong(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PfListsSrv{");
        sb.append("commandForNatStr='").append(commandForNatStr).append('\'');
        sb.append(", pfListsInstAW=").append(pfListsInstAW.toString());
        sb.append('}');
        return sb.toString();
    }
}
