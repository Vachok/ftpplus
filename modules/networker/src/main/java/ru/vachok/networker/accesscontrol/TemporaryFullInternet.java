package ru.vachok.networker.accesscontrol;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 Разрешить интернет до конца суток

 @since 28.02.2019 (11:52) */
@Service
public class TemporaryFullInternet implements Runnable {

    private static final String INIT_PING_EXIT_STR = "sudo /etc/initpf.fw;exit";

    private static final MessageToUser messageToUser = new MessageLocal();

    private static final String SERVER_TO_CONNECT = ConstantsFor.IPADDR_SRVNAT;

    private static final String STR_SSH_COMMAND = "sshCommand";

    private static final Deque<String> MINI_LOGGER = new ArrayDeque<>();

    @SuppressWarnings ("CanBeFinal")
    private String userInput;

    private long delStamp;

    private long initStamp = System.currentTimeMillis();

    public TemporaryFullInternet(String userInput, String numOfHoursStr) {
        this.userInput = userInput;
        this.delStamp = ConstantsFor.getAtomicTime() + TimeUnit.HOURS.toMillis(Long.parseLong(numOfHoursStr));
        MINI_LOGGER.add("TemporaryFullInternet: " + userInput + " " + delStamp + "(" + new Date(delStamp) + ")");

    }

    public TemporaryFullInternet() {
        this.userInput = "10.200.213.254";
        this.delStamp = System.currentTimeMillis();
        MINI_LOGGER.add("TemporaryFullInternet(): " + this.userInput + " " + delStamp + "(" + new Date(delStamp) + ")");
    }

    String doAdd() {
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInput);
        SSHFactory tempFileFactory = new SSHFactory.Builder(SERVER_TO_CONNECT, "cat /etc/pf/24hrs").build();
        String sshCommand;
        String sshIP = String.valueOf(nameOrIPChecker.resolveIP()).split("/")[1];
        String tempFile = tempFileFactory.call();
        if(tempFile.contains(sshIP)){
            messageToUser.warn(getClass().getSimpleName(), "doAdd", sshIP + " is exist!");
            sshCommand = new TForms().fromArray(sshChecker(), true);
        }
        else{
            sshCommand = new StringBuilder()
                .append(SshActs.SUDO_ECHO)
                .append("\"").append(sshIP).append(" #")
                .append(delStamp).append("\"").append(" >> /etc/pf/24hrs;").append(INIT_PING_EXIT_STR).toString();
            SSHFactory sshFactory = new SSHFactory.Builder(SERVER_TO_CONNECT, sshCommand).build();
            sshCommand = sshFactory.call() + "<p>" + tempFileFactory.call();
        }
        MINI_LOGGER.add("doAdd(): " + sshCommand);
        return sshCommand;
    }

    private Map<String, Long> sshChecker() {
        SSHFactory tempFileFact = new SSHFactory.Builder(SERVER_TO_CONNECT, "cat /etc/pf/24hrs").build();
        String tempFile = tempFileFact.call();
        if(tempFile.isEmpty()){
            throw new IllegalComponentStateException("File is empty");
        }
        else{
            String[] strings = tempFile.split("\n");
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x -> {
                try{
                    ConstantsNet.getSshCheckerMap().put(x.split(" #")[0].trim(), Long.valueOf(x.split(" #")[1]));
                    MINI_LOGGER.add("sshChecker(): ipTime.put(): " + x);
                }
                catch(Exception e){
                    messageToUser.errorAlert("TemporaryFullInternet", "sshChecker", e.getMessage());
                    MINI_LOGGER.add("sshChecker(): " + e.getMessage());
                    FileSystemWorker.error("TemporaryFullInternet.sshChecker", e);
                    FileSystemWorker.recFile(getClass().getSimpleName() + ".mini", MINI_LOGGER.stream());
                }
            });
        }
        ConstantsNet.getSshCheckerMap().forEach((x, y) -> {
            this.delStamp = y;
            messageToUser.info(x, "will be deleted at ", LocalDateTime.ofEpochSecond(delStamp / 1000, 0, ZoneOffset.ofHours(3)).toString());
            if(delStamp < ConstantsFor.getAtomicTime()){
                messageToUser.warn(getClass().getSimpleName(), x, String.valueOf(doDelete(x)));
                MINI_LOGGER.add("sshChecker(SSH_CHECKER_MAP.forEach): time is" + true + "\n" + x);
                messageToUser.warn("TemporaryFullInternet.sshChecker", "delStamp", " = " + delStamp);
                messageToUser.warn("TemporaryFullInternet.sshChecker", "ConstantsFor.getAtomicTime()", " = " + ConstantsFor.getAtomicTime());
                messageToUser.error("TemporaryFullInternet.sshChecker", "ConstantsFor.getAtomicTime()-delStamp",
                    " = " + (ConstantsFor.getAtomicTime() - delStamp));
            }
            else{
                messageToUser.info("TemporaryFullInternet.sshChecker", "x", " = " + x);
                messageToUser.info("TemporaryFullInternet.sshChecker", "y", " = " + y);
            }
        });
        return ConstantsNet.getSshCheckerMap();
    }

    private boolean doDelete(String x) {
        String sshCommand = new StringBuilder()
            .append(SshActs.SUDO_GREP_V).append(x).append("' /etc/pf/24hrs > /etc/pf/24hrs_tmp;")
            .append("sudo cp /etc/pf/24hrs_tmp /etc/pf/24hrs;").append(INIT_PING_EXIT_STR).toString();
        SSHFactory sshFactory = new SSHFactory.Builder(SERVER_TO_CONNECT, sshCommand).build();
        sshCommand = sshFactory.call();
        messageToUser.info("TemporaryFullInternet.doDelete", STR_SSH_COMMAND, " = " + sshCommand);
        Long aLong = ConstantsNet.getSshCheckerMap().remove(x);
        MINI_LOGGER.add(new Date(aLong).toString() + ", doDelete(): " + sshCommand);
        return ConstantsNet.getSshCheckerMap().containsKey(x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userInput);
    }

    @Override
    public boolean equals(Object o) {
        if(this==o){
            return true;
        }
        if(o==null || getClass()!=o.getClass()){
            return false;
        }
        TemporaryFullInternet that = ( TemporaryFullInternet ) o;
        return Objects.equals(userInput, that.userInput);
    }

    @Override
    public void run() {
        String fromArray = new TForms().fromArray(sshChecker(), false);
        messageToUser.info(getClass().getSimpleName(), userInput, fromArray);
        MINI_LOGGER.add("run(): " + userInput + " " + fromArray);
        FileSystemWorker.recFile(getClass().getSimpleName() + ".mini", MINI_LOGGER.stream());
        new MessageSwing().infoTimer(30, toString());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TemporaryFullInternet{");
        sb.append("delStamp=").append(delStamp);
        sb.append(", initStamp=").append(initStamp);
        sb.append(", userInput='").append(userInput).append('\'');
        sb.append('}');
        sb.append("<p>\n").append(new TForms().fromArray(MINI_LOGGER, true));
        return sb.toString();
    }
}
