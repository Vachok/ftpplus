package ru.vachok.networker.accesscontrol;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.AppComponents;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 Разрешить интернет до конца суток

 @since 28.02.2019 (11:52) */
public class TemporaryFullInternet {

    private static final String INIT_PING_EXIT_STR = "sudo /etc/initpf.fw;ping -c 4 10.200.200.1 > /dev/null;exit";

    private static final MessageToUser messageToUser = new MessageLocal();

    private static final String STR_SSH_COMMAND = "sshCommand";

    @SuppressWarnings ("CanBeFinal")
    private String userInput;

    public TemporaryFullInternet(String userInput) {
        if (userInput == null) {
            sshChecker();
        }
        this.userInput = userInput;
    }

    String doDidDoes() {
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInput);
        String sshIP = String.valueOf(nameOrIPChecker.resolveIP()).split("/")[1];
        String classMeth = "TemporaryFullInternet.doDidDoes";
        String sshCommand = new StringBuilder()
            .append(SshActs.SUDO_ECHO)
            .append("\"").append(sshIP).append(" #")
            .append(ConstantsFor.getAtomicTime()).append("\"").append(" >> /etc/pf/24hrs;").append(INIT_PING_EXIT_STR).toString();
        messageToUser.info(classMeth, STR_SSH_COMMAND, " = " + sshCommand);
        sshCommand = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVNAT, sshCommand).build().call();
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor().scheduleWithFixedDelay(this::sshChecker, 1, 1, TimeUnit.HOURS);
        return sshCommand;
    }

    public static void main(String[] args) {
        new TemporaryFullInternet(null);
    }

    private void sshChecker() {
        String tempFile = new SSHFactory.Builder(ConstantsFor.IPADDR_SRVNAT, "cat /etc/pf/24hrs").build().call();
        Map<String, Long> ipTime = new HashMap<>();
        if (tempFile.isEmpty()) {
            throw new IllegalComponentStateException("File is empty");
        } else {
            String[] strings = tempFile.split("\n");
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x -> {
                try {
                    ipTime.put(x.split(" #")[0].trim(), Long.valueOf(x.split(" #")[1]));
                } catch (ArrayIndexOutOfBoundsException ignore) {
                    //
                }
            });
        }
        messageToUser.info("TemporaryFullInternet.sshChecker", "new TForms().fromArray(ipTime, false)", " = " + new TForms().fromArray(ipTime, false));
        ipTime.forEach((x, y) -> {
            if (y + TimeUnit.DAYS.toMillis(1) < ConstantsFor.getAtomicTime()) {
                doDidDoes(x);
            }
        });
    }

    private void doDidDoes(String x) {
        String sshCommand = new StringBuilder()
            .append(SshActs.SUDO_GREP_V).append(x).append("' /etc/pf/24hrs > /etc/pf/24hrs_tmp;")
            .append("sudo cp /etc/pf/24hrs_tmp /etc/pf/24hrs;").append(INIT_PING_EXIT_STR)
            .toString();
        messageToUser.info("TemporaryFullInternet.doDidDoes", STR_SSH_COMMAND, " = " + sshCommand);
    }

}
