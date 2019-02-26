package ru.vachok.networker.accesscontrol;


import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;

import java.rmi.UnexpectedException;


/**
 Список-выгрузка с сервера доступа в интернет

 @since 10.09.2018 (11:49) */
@Service
public class PfListsSrv {

    /**
     {@link PfLists}
     */
    @SuppressWarnings ("CanBeFinal")
    private @NotNull PfLists pfListsInstAW;

    /**
     SSH-команда.
     <p>
     При инициализации: {@code uname -a;exit}.

     @see PfListsCtr#runCommand(org.springframework.ui.Model, ru.vachok.networker.accesscontrol.PfListsSrv)
     @see #runCom()
     */
    private @NotNull String commandForNatStr = "traceroute 8.8.8.8;exit";

    /**
     new {@link SSHFactory.Builder}.
     */
    @SuppressWarnings ("CanBeFinal")
    private @NotNull SSHFactory.Builder builderInst = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandForNatStr);

    private @NotNull MessageToUser messageToUser = new MessageLocal();

    /**
     {@code this.builderInst}
     <p>
     new {@link SSHFactory.Builder} ({@link ConstantsFor#SRV_NAT} , {@link #commandForNatStr}).

     @param pfLists {@link #pfListsInstAW}
     */
    @Autowired
    public PfListsSrv(@NotNull PfLists pfLists) {
        this.pfListsInstAW = pfLists;
    }

    /**
     Формирует списки <b>pf</b>
     <p>
     Если {@link ConstantsFor#thisPC()} contains {@code rups} - {@link #buildCommands()} через {@link AppComponents#threadConfig()}.
     {@link ThreadConfig#executeAsThread(java.lang.Runnable)} <br>
     Else {@link MessageLocal#warn(java.lang.String)} {@link String} = {@link ConstantsFor#thisPC()}
     */
    void makeListRunner() {
        if(ConstantsFor.thisPC().toLowerCase().contains("rups")){
            AppComponents.threadConfig().getTaskExecutor().submit(this::buildCommands);
        }
        else{
            try{
                messageToUser = new MessageToTray();
            }
            catch(ExceptionInInitializerError ignore){
                messageToUser = new MessageLocal();
            }
            messageToUser.warn(this.getClass().getSimpleName(), "NOT RUNNING ON RUPS!", ConstantsFor.thisPC() + " buildCommands " + false);
        }
    }

    /**
     Строитель команд.
     <p>
     {@link PfListsSrv#buildFactory()} <br>
     <b>{@link Exception}:</b><br>
     {@link FileSystemWorker#recFile(java.lang.String, java.util.List)} - {@code this.getClass().getSimpleName() + ".makeListRunner", stringArrayList}
     */
    private void buildCommands() {
        try{
            buildFactory();
        }
        catch(UnexpectedException e){
            messageToUser.errorAlert("PfListsSrv", "buildCommands", e.getMessage());
            FileSystemWorker.error("PfListsSrv.buildCommands", e);
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
        SSHFactory buildGit = new SSHFactory.Builder(ConstantsFor.SRV_GIT, "sudo /etc/stat.script").build();
        if(!ConstantsFor.isPingOK()){
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

        build.setCommandSSH("pfctl -s rules;traceroute 8.8.8.8;exit");
        pfListsInstAW.setPfRules(build.call());

        String callToStatScript = buildGit.call();
        messageToUser.info("PfListsSrv.buildFactory", "callToStatScript", callToStatScript);

        pfListsInstAW.setGitStatsUpdatedStampLong(System.currentTimeMillis());
    }

    String runCom() {
        SSHFactory.Builder builder = new SSHFactory.Builder(ConstantsFor.SRV_NAT, commandForNatStr);
        return builder.build().call();
    }

    @Override
    public int hashCode() {
        int result = getPfListsInstAW()!=null? getPfListsInstAW().hashCode(): 0;
        result = 31 * result + getCommandForNatStr().hashCode();
        result = 31 * result + builderInst.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null || getClass()!=o.getClass()){
            return false;
        }

        PfListsSrv that = ( PfListsSrv ) o;

        if(getPfListsInstAW()!=null? !getPfListsInstAW().equals(that.getPfListsInstAW()): that.getPfListsInstAW()!=null){
            return false;
        }
        if(!getCommandForNatStr().equals(that.getCommandForNatStr())){
            return false;
        }
        return builderInst.equals(that.builderInst);
    }

    public PfLists getPfListsInstAW() {
        return pfListsInstAW;
    }

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
    @SuppressWarnings ("unused")
    public void setCommandForNatStr(@NotNull String commandForNatStr) {
        this.commandForNatStr = commandForNatStr;
    }

    @Override
    public @NotNull String toString() {
        final StringBuilder sb = new StringBuilder("PfListsSrv{");
        sb.append("pfListsInstAW=").append(pfListsInstAW.hashCode());
        sb.append(", commandForNatStr='").append(commandForNatStr).append('\'');
        sb.append(ConstantsFor.TOSTRING_MESSAGE_TO_USER).append(messageToUser.toString());
        sb.append(", builderInst=").append(builderInst.hashCode());
        sb.append('}');
        return sb.toString();
    }

}
