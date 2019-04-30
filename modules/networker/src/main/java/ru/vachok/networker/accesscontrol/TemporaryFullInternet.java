// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetListKeeper;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.MessageLocal;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.*;


/**
 Разрешить интернет до конца суток
 
 @since 28.02.2019 (11:52) */
@Service
public class TemporaryFullInternet implements Runnable {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(TemporaryFullInternet.class.getSimpleName());
    
    private static final String SERVER_TO_CONNECT = whatServerNow();
    
    private static final String STR_SSH_COMMAND = "sshCommand";
    
    private static final Queue<String> MINI_LOGGER = new ArrayDeque<>();
    
    private static final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();
    
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
    
    public TemporaryFullInternet(String userInput, long timeToApply) {
        this.userInput = userInput;
        this.delStamp = ConstantsFor.getAtomicTime() + TimeUnit.HOURS.toMillis(timeToApply);
        MINI_LOGGER.add("TemporaryFullInternet: " + userInput + " " + delStamp + "(" + new Date(delStamp) + ")");
    }
    
    public TemporaryFullInternet(long timeStampOff) {
        this.userInput = "10.200.213.85";
        this.delStamp = timeStampOff;
    }
    
    
    TemporaryFullInternet(String userInput, String numOfHoursStr) {
        this.userInput = userInput;
        this.delStamp = ConstantsFor.getAtomicTime() + TimeUnit.HOURS.toMillis(Long.parseLong(numOfHoursStr));
        MINI_LOGGER.add("TemporaryFullInternet: " + userInput + " " + delStamp + "(" + new Date(delStamp) + ")");
    }
    
    public String doAdd() {
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInput);
        StringBuilder retBuilder = new StringBuilder();
        String tempString24HRSFile;
        String sshIP;
        try {
            sshIP = String.valueOf(nameOrIPChecker.resolveIP()).split("/")[1];
            SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
            tempString24HRSFile = SSH_FACTORY.call();
        }
        catch (ArrayIndexOutOfBoundsException | UnknownFormatConversionException e) {
            sshIP = new TForms().fromArray(e, true);
            tempString24HRSFile = null;
            return sshIP;
        }
        if (tempString24HRSFile.contains(sshIP)) {
            retBuilder.append("<h2>")
                .append(getClass().getSimpleName())
                .append(" doAdd: ")
                .append(sshIP)
                .append(" is exist!</h2><br>")
                .append(new TForms().fromArray(SSH_CHECKER_MAP, true));
        }
        else if (NetListKeeper.getI().getInetUniqMap().containsKey(sshIP) && !NetListKeeper.getI().getInetUniqMap().get(sshIP).equalsIgnoreCase("10.200.213.85")) {
            String listWhere = NetListKeeper.getI().getInetUniqMap().get(sshIP.replace(".list", ""));
    
            retBuilder.append("<h2>").append(sshIP).append(" in regular list: ").append(listWhere).append("</h2>");
            retBuilder.append(addFromExistList(sshIP, listWhere));
        }
        else {
            String sshCommand = new StringBuilder()
                .append(SshActs.SUDO_ECHO)
                .append("\"").append(sshIP).append(" #")
                .append(delStamp).append("\"").append(" >> /etc/pf/24hrs;").append(ConstantsNet.COM_INITPF).toString();
            SSH_FACTORY.setCommandSSH(sshCommand);
            retBuilder.append(SSH_FACTORY.call());
        }
        MINI_LOGGER.add("doAdd(): " + retBuilder);
        return retBuilder.toString();
    }
    
    private String addFromExistList(String sshIP, String listWhere) {
        String etcPf = " /etc/pf/";
        listWhere = listWhere.replace(".list", "");
        
        StringBuilder comSSHBuilder = new StringBuilder();
        comSSHBuilder.append(SshActs.SSH_SUDO_GREP_V);
        comSSHBuilder.append(sshIP).append("'");
        comSSHBuilder.append(etcPf).append(listWhere).append(" >").append(etcPf).append(listWhere).append("_tmp;");
        
        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        messageToUser.info(getClass().getSimpleName() + ".addFromExistList", "comSSHBuilder", " = " + SSH_FACTORY.call());
        
        comSSHBuilder = new StringBuilder();
        comSSHBuilder.append("sudo cp /etc/pf/").append(listWhere).append("_tmp /etc/pf/").append(listWhere).append(";");
        
        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        SSH_FACTORY.call();
        
        comSSHBuilder = new StringBuilder();
        comSSHBuilder.append(SshActs.SUDO_ECHO).append("\"").append(sshIP).append(" #").append(delStamp).append(" #").append(listWhere).append("\"").append(" >> /etc/pf/24hrs;")
            .append(ConstantsNet.COM_INITPF);
        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        return SSH_FACTORY.call();
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
    
    private static String whatServerNow() {
        if (ConstantsFor.thisPC().toLowerCase().contains("rups")) {
            return ConstantsFor.IPADDR_SRVNAT;
        }
        else {
            return ConstantsFor.IPADDR_SRVGIT;
        }
    }
    
    private void execOldMeth() {
        AppComponents.threadConfig().execByThreadConfig(this::sshChecker);
        Map<String, Long> stringLongMap = SSH_CHECKER_MAP;
        File miniLog = new File(getClass().getSimpleName() + ".mini");
        String fromArray = new TForms().fromArray(stringLongMap, false);
        
        MINI_LOGGER.add("execOldMeth: " + userInput + " " + fromArray);
        Date nextStart = new Date(ConstantsFor.getAtomicTime() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        MINI_LOGGER.add(nextStart.toString());
        boolean writeFile = FileSystemWorker.writeFile(miniLog.getName(), MINI_LOGGER.stream());
        FileSystemWorker.copyOrDelFile(miniLog, ".\\ssh\\" + miniLog.getName(), true);
        if (writeFile) {
            MINI_LOGGER.clear();
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
        Long aLong = SSH_CHECKER_MAP.remove(x);
        if (!(aLong == null)) {
            MINI_LOGGER.add(new Date(aLong) + ", doDelete: " + sshCommand);
        }
        return SSH_CHECKER_MAP.containsKey(x);
    }
    
    private void sshChecker() {
        SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
        String tempFile = SSH_FACTORY.call();
        MINI_LOGGER.add(tempFile);
        String classMeth = "TemporaryFullInternet.sshChecker";
        Map<String, Long> sshCheckerMap = SSH_CHECKER_MAP;
        
        if (tempFile.isEmpty()) {
            throw new IllegalComponentStateException("File is empty");
        }
        else {
            String[] strings = tempFile.split("<br>\n");
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x->{
                if (x.split(" #").length > 2) {
                    chkWithList(x.split(" #"));
                }
                try {
                    Long ifAbsent = sshCheckerMap.putIfAbsent(x.split(" #")[0].trim(), Long.valueOf(x.split(" #")[1]));
                    MINI_LOGGER.add("Added to map = " + x + " " + ifAbsent);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    messageToUser.errorAlert("TemporaryFullInternet", "sshChecker", e.getMessage());
                    MINI_LOGGER.add(e.getMessage());
                }
            });
        }
        long atomicTimeLong = ConstantsFor.getAtomicTime();
        for (Map.Entry<String, Long> entry : sshCheckerMap.entrySet()) {
            String x = entry.getKey();
            Long y = entry.getValue();
            mapEntryParse(x, y, atomicTimeLong);
        }
        Future<?> setMapAsStringHTMLFuture = AppComponents.threadConfig().getTaskExecutor().getThreadPoolExecutor()
            .submit(()->ConstantsNet.setSshMapStr(new TForms().sshCheckerMapWithDates(sshCheckerMap, true)));
        try {
            setMapAsStringHTMLFuture.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
            messageToUser.info(getClass().getSimpleName() + ".sshChecker", "ConstantsNet.getSshMapStr()", " = " + ConstantsNet.getSshMapStr());
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".sshChecker is interrupted.\n", e));
            Thread.currentThread().interrupt();
        }
    }
    
    private void chkWithList(String[] x) {
        this.delStamp = Long.parseLong(x[1]);
        if (delStamp < ConstantsFor.getAtomicTime()) {
            doDelete(x[0]);
            addBackToList(x[0], x[2]);
        }
    }
    
    private String addBackToList(String ip, String accList) {
        StringBuilder sshBuilder = new StringBuilder();
        sshBuilder.append(SshActs.SUDO_ECHO).append("\"").append(ip).append(" #").append(new java.util.Date()).append("\"")
            .append(" >> /etc/pf/").append(accList).append(";").append(ConstantsNet.COM_INITPF);
        SSH_FACTORY.setCommandSSH(sshBuilder.toString());
        return SSH_FACTORY.call();
    }
    
    private void mapEntryParse(String x, Long y, long atomicTimeLong) {
        String willBeDel = x + " will be deleted at " + LocalDateTime.ofEpochSecond(delStamp / 1000, 0, ZoneOffset.ofHours(3));
        MINI_LOGGER.add(willBeDel);
        this.delStamp = y;
        if (delStamp < atomicTimeLong) {
            boolean isDelete = doDelete(x);
            MINI_LOGGER.add("sshChecker(SSH_CHECKER_MAP.forEach): time is" + true + "\n" + x + " is delete = " + isDelete);
            MINI_LOGGER.add("delStamp = " + delStamp);
            MINI_LOGGER.add("ConstantsFor.getAtomicTime()-delStamp = " + (atomicTimeLong - delStamp));
        }
        else {
            MINI_LOGGER.add("IP" + " = " + x + " time: " + y + " (" + new Date(y) + ")");
        }
    }
}
