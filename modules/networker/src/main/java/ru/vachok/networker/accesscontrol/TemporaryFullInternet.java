package ru.vachok.networker.accesscontrol;


import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.SSHFace;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 Разрешить интернет до конца суток
 
 @since 28.02.2019 (11:52) */
@Service
public class TemporaryFullInternet implements Runnable {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(TemporaryFullInternet.class.getSimpleName());
    
    private static final String SERVER_TO_CONNECT = whatServerNow();
    
    private static final String STR_SSH_COMMAND = "sshCommand";
    
    private static final Queue<String> MINI_LOGGER = new ArrayDeque<>();
    
    private static final SSHFactory SSH_FACTORY = new SSHFactory.Builder(SERVER_TO_CONNECT, "ls", TemporaryFullInternet.class.getSimpleName()).build();
    
    private static final String TEMPORARY_FULL_INTERNET_RUN = "TemporaryFullInternet.run";
    
    @SuppressWarnings("CanBeFinal")
    private String userInput;
    
    private long delStamp;
    
    private long initStamp = System.currentTimeMillis();
    
    
    public TemporaryFullInternet() {
        this.userInput = "10.200.213.254";
        this.delStamp = System.currentTimeMillis();
        MINI_LOGGER.add("TemporaryFullInternet(): " + this.userInput + " " + delStamp + "(" + new Date(delStamp) + ")");
    }
    
    TemporaryFullInternet(String userInput, long timeToApply) {
        this.userInput = userInput;
        this.delStamp = ConstantsFor.getAtomicTime() + TimeUnit.HOURS.toMillis(timeToApply);
        MINI_LOGGER.add("TemporaryFullInternet: " + userInput + " " + delStamp + "(" + new Date(delStamp) + ")");
    }
    
    TemporaryFullInternet(String userInput, String numOfHoursStr) {
        this.userInput = userInput;
        this.delStamp = ConstantsFor.getAtomicTime() + TimeUnit.HOURS.toMillis(Long.parseLong(numOfHoursStr));
        MINI_LOGGER.add("TemporaryFullInternet: " + userInput + " " + delStamp + "(" + new Date(delStamp) + ")");
    }
    
    public void execNewMeth() {
        SSHFace face = new SSHHelper("sudo cat /etc/pf/24hrs");
        try {
            String doCommand = face.execCommand("192.168.13.30", "ls");
            System.out.println("doCommand = " + doCommand);
        }
        catch (JSchException | IOException e) {
            messageToUser.error(e.getMessage());
        }
    }
    
    @Override
    public void run() {
        execOldMeth();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TemporaryFullInternet that = (TemporaryFullInternet) o;
        return Objects.equals(userInput, that.userInput);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userInput);
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
    
    String doAdd() {
        AppComponents.threadConfig().thrNameSet("addSSH");
        SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInput);
        String tempString24HRSFile = SSH_FACTORY.call();
        StringBuilder retBuilder = new StringBuilder();
        String sshIP;
        try {
            sshIP = String.valueOf(nameOrIPChecker.resolveIP()).split("/")[1];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            sshIP = e.getMessage();
        }
        if (tempString24HRSFile.contains(sshIP)) {
            retBuilder
                .append(getClass().getSimpleName())
                .append(" doAdd ")
                .append(sshIP)
                .append(" is exist!<br>")
                .append(new TForms().fromArray(ConstantsNet.getSshCheckerMap(), true));
        }
        else {
            String sshCommand = new StringBuilder()
                .append(SshActs.SUDO_ECHO)
                .append("\"").append(sshIP).append(" #")
                .append(delStamp).append("\"").append(" >> /etc/pf/24hrs;").append(ConstantsNet.COM_INITPF).toString();
            SSH_FACTORY.setCommandSSH(sshCommand);
            retBuilder.append(SSH_FACTORY.call().replaceAll("\n", "<br>"));
        }
        MINI_LOGGER.add("doAdd(): " + retBuilder);
        return retBuilder.toString();
    }
    
    private void execOldMeth() {
        AppComponents.threadConfig().execByThreadConfig(this::sshChecker);
        Map<String, Long> stringLongMap = ConstantsNet.getSshCheckerMap();
        File miniLog = new File(getClass().getSimpleName() + ".mini");
        String fromArray = new TForms().fromArray(stringLongMap, false);
    
        MINI_LOGGER.add("execOldMeth: " + userInput + " " + fromArray);
        Date nextStart = new Date(ConstantsFor.getAtomicTime() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        MINI_LOGGER.add(nextStart.toString());
        boolean writeFile = FileSystemWorker.writeFile(miniLog.getName(), MINI_LOGGER.stream());
        FileSystemWorker.copyOrDelFile(miniLog, ".\\ssh\\" + miniLog.getName(), true);
        if (writeFile) MINI_LOGGER.clear();
    }
    
    private static String whatServerNow() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return ConstantsFor.IPADDR_SRVNAT;
        }
        else {
            return ConstantsFor.IPADDR_SRVGIT;
        }
    }
    
    private boolean doDelete(String x) {
        AppComponents.threadConfig().thrNameSet("delSSH");
    
        String sshC = new StringBuilder()
            .append(SshActs.SSH_SUDO_GREP_V).append(x)
            .append("' /etc/pf/24hrs > /etc/pf/24hrs_tmp;").append("sudo cp /etc/pf/24hrs_tmp /etc/pf/24hrs;")
            .append(ConstantsNet.COM_INITPF).toString();
        SSH_FACTORY.setCommandSSH(sshC);
        String sshCommand = SSH_FACTORY.call();
        Long aLong = ConstantsNet.getSshCheckerMap().remove(x);
        MINI_LOGGER.add(new Date(aLong) + ", doDelete: " + sshCommand);
        return ConstantsNet.getSshCheckerMap().containsKey(x);
    }
    
    private void sshChecker() {
        AppComponents.threadConfig().thrNameSet("chkSSH");
        SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
        String tempFile = SSH_FACTORY.call();
    
        MINI_LOGGER.add(tempFile);
    
        String classMeth = "TemporaryFullInternet.sshChecker";
        Map<String, Long> sshCheckerMap = ConstantsNet.getSshCheckerMap();
        
        if (tempFile.isEmpty()) {
            throw new IllegalComponentStateException("File is empty");
        }
        else {
            String[] strings = tempFile.split("\n");
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x->{
                try {
                    Long ifAbsent = sshCheckerMap.putIfAbsent(x.split(" #")[0].trim(), Long.valueOf(x.split(" #")[1]));
                    MINI_LOGGER.add("Added to map = " + x + " " + ifAbsent);
                }
                catch (Exception e) {
                    messageToUser.errorAlert("TemporaryFullInternet", "sshChecker", e.getMessage());
                    MINI_LOGGER.add(e.getMessage());
                }
            });
        }
        for (Map.Entry<String, Long> entry : sshCheckerMap.entrySet()) {
            String x = entry.getKey();
            Long y = entry.getValue();
            String willBeDel = x + " will be deleted at " + LocalDateTime.ofEpochSecond(delStamp / 1000, 0, ZoneOffset.ofHours(3));
            MINI_LOGGER.add(willBeDel);
            this.delStamp = y;
            if (delStamp < ConstantsFor.getAtomicTime()) {
                boolean isDelete = doDelete(x);
                MINI_LOGGER.add("sshChecker(SSH_CHECKER_MAP.forEach): time is" + true + "\n" + x + " is delete = " + isDelete);
                MINI_LOGGER.add("delStamp = " + delStamp);
                MINI_LOGGER.add("ConstantsFor.getAtomicTime() = " + ConstantsFor.getAtomicTime());
                MINI_LOGGER.add("ConstantsFor.getAtomicTime()-delStamp = " + (ConstantsFor.getAtomicTime() - delStamp));
            }
            else {
                MINI_LOGGER.add("IP" + " = " + x + " time: " + y + " (" + new Date(y) + ")");
            }
        }
        Future<?> future =
            AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor()
                .submit(()->ConstantsNet.setSSHMapStr(new TForms().sshCheckerMapWithDates(sshCheckerMap, true)));
        try {
            future.get(25, TimeUnit.SECONDS);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".sshChecker", e));
        }
        messageToUser.info("TemporaryFullInternet.sshChecker", "future.isDone()", " = " + future.isDone());
        messageToUser.warn("TemporaryFullInternet.sshChecker", "future.isDone()", " = " + future.isCancelled());
    }
    
    class SSHHelper implements SSHFace {
        
        String commandSSH;
        
        public SSHHelper(String commandSSH) {
            this.commandSSH = commandSSH;
        }
        
        public String getCommandSSH() {
            return commandSSH;
        }
        
        public void setCommandSSH(String commandSSH) {
            this.commandSSH = commandSSH;
        }
    
        @Override public String execCommand(String srv, String commandSSH) throws JSchException, IOException {
            execCh().setCommand(commandSSH);
            StringBuilder stringBuilder = new StringBuilder();
            byte[] bytes = new byte[ConstantsFor.KBYTE];
            while (true) {
                execCh().connect();
                InputStream inputStream = execCh().getInputStream();
                OutputStream outputStream = new FileOutputStream("com.ssh");
                byte[] buf = new byte[1024];
                while (true) {
                    int readBuf = inputStream.read(buf, 0, buf.length);
                    if (readBuf <= 0) {
                        break;
                    }
                    outputStream.write(buf, 0, readBuf);
                }
                outputStream.close();
                inputStream.close();
            }
        }
        
        private ChannelExec execCh() throws JSchException {
            Session session = null;
            session = sessionWithJsch();
            session.setConfig(new DBRegProperties("general-jsch").getProps());
            session.connect();
            if (session.isConnected()) {
                return (ChannelExec) session.openChannel("exec");
            }
            else {
                throw new JSchException();
            }
        }
        
        private Session sessionWithJsch() throws JSchException {
            JSch jSch = new JSch();
            try {
                jSch.addIdentity("a161.pem");
            }
            catch (JSchException e) {
                messageToUser.error(e.getMessage());
            }
            return jSch.getSession("ITDept", ConstantsFor.IPADDR_SRVNAT);
        }
    }
}
