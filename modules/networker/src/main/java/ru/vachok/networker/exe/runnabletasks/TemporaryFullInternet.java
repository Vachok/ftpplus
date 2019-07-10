// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.springframework.stereotype.Service;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.componentsrepo.IllegalInvokeEx;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetListKeeper;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.services.DBMessenger;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 Разрешить интернет до конца суток
 
 @since 28.02.2019 (11:52) */
@Service
public class TemporaryFullInternet implements Runnable, Callable<String> {
    
    
    private static final MessageToUser messageToUser = new DBMessenger(TemporaryFullInternet.class.getSimpleName());
    
    private static final Queue<String> MINI_LOGGER = new ArrayDeque<>();
    
    private static final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();
    
    private static final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();
    
    private static final Pattern COMPILE = Pattern.compile(".list", Pattern.LITERAL);
    
    private static final Pattern PATTERN = Pattern.compile(".list", Pattern.LITERAL);
    
    private static final Pattern COMPILE1 = Pattern.compile("<br>\n");
    
    private static final Pattern COMPILE2 = Pattern.compile(" #");
    
    private static final Pattern COMPILE3 = Pattern.compile(" #");
    
    private static final Pattern COMPILE4 = Pattern.compile(" #");
    
    private static final Pattern COMPILE5 = Pattern.compile(" #");
    
    @SuppressWarnings("CanBeFinal")
    private String userInputIpOrHostName;
    
    private NetListKeeper listKeeper = AppComponents.netKeeper();
    
    private long delStamp;
    
    private String optionToDo;
    
    private long initStamp = System.currentTimeMillis();
    
    public TemporaryFullInternet() {
        this.userInputIpOrHostName = "10.200.213.254";
        this.delStamp = System.currentTimeMillis();
        this.optionToDo = "check";
    
        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + optionToDo.toUpperCase() + " " + userInputIpOrHostName + " full internet access before: " + new Date(delStamp));
    }
    
    public TemporaryFullInternet(String input, long hoursToOpenInet, String option) {
        this.userInputIpOrHostName = input;
        this.delStamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hoursToOpenInet);
        this.optionToDo = option;
    
        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + option.toUpperCase() + " " + input + " full internet access before: " + new Date(delStamp));
    }
    
    TemporaryFullInternet(long timeStampOff) {
        this.userInputIpOrHostName = "10.200.213.85";
        this.delStamp = timeStampOff;
    
        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + optionToDo.toUpperCase() + " " + userInputIpOrHostName + " full internet access before: " + new Date(delStamp));
    }
    
    @Override
    public String call() {
        return doAdd();
    }
    
    @Override
    public void run() {
        SSH_FACTORY.setConnectToSrv(new AppComponents().sshActs().whatSrvNeed());
        if (optionToDo != null & optionToDo.equals("add")) {
            System.out.println("doAdd() = " + doAdd());
        }
        execOldMeth();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TemporaryFullInternet{");
        sb.append("delStamp=").append(delStamp);
        sb.append(", initStamp=").append(initStamp);
        sb.append('}');
        sb.append("<p>\n").append(new TForms().fromArray(MINI_LOGGER, true));
        return sb.toString();
    }
    
    @SuppressWarnings("FeatureEnvy")
    private String doAdd() {
        SSH_FACTORY.setConnectToSrv(new AppComponents().sshActs().whatSrvNeed());
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInputIpOrHostName);
        StringBuilder retBuilder = new StringBuilder();
        String tempString24HRSFile;
        String sshIP;
        Map<String, String> inetUniqMap = listKeeper.getInetUniqMap();
        
        try {
            sshIP = String.valueOf(nameOrIPChecker.resolveIP()).split("/")[1];
            SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
            tempString24HRSFile = SSH_FACTORY.call();
        }
        catch (ArrayIndexOutOfBoundsException | UnknownFormatConversionException | UnknownHostException e) {
            sshIP = new TForms().fromArray(e, true);
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
        else {
            if (inetUniqMap.containsKey(sshIP) && !inetUniqMap.get(sshIP).equalsIgnoreCase("10.200.213.85")) {
                String listWhere = inetUniqMap.get(COMPILE.matcher(sshIP).replaceAll(Matcher.quoteReplacement("")));
    
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
        }
        listKeeper.setInetUniqMap(inetUniqMap);
        MINI_LOGGER.add("doAdd(): " + retBuilder);
        return retBuilder.toString();
    }
    
    @SuppressWarnings("FeatureEnvy")
    private String addFromExistList(String sshIP, String listWhere) {
        String etcPf = " /etc/pf/";
        listWhere = PATTERN.matcher(listWhere).replaceAll(Matcher.quoteReplacement(""));
        
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
    
    private void execOldMeth() {
        try {
            boolean threadConfig = AppComponents.threadConfig().execByThreadConfig(this::sshChecker);
            messageToUser.info(getClass().getSimpleName() + ".execOldMeth", "threadConfig", " = " + threadConfig);
        }
        catch (Exception e) {
            messageToUser.error(e.getMessage());
        }
        Map<String, Long> stringLongMap = SSH_CHECKER_MAP;
        Date nextStart = new Date(ConstantsFor.getAtomicTime() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        String fromArray = new TForms().fromArray(stringLongMap, false);
        
        MINI_LOGGER.add("execOldMeth: " + userInputIpOrHostName + " " + fromArray);
        MINI_LOGGER.add(nextStart.toString());
        writeLog();
    }
    
    private void writeLog() {
        File miniLog = new File(getClass().getSimpleName() + ".mini");
        boolean writeFile = FileSystemWorker.writeFile(miniLog.getName(), MINI_LOGGER.stream());
        FileSystemWorker.copyOrDelFile(miniLog, Paths.get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "sshactions" + ConstantsFor.FILESYSTEM_SEPARATOR + miniLog.getName())
            .toAbsolutePath().normalize(), true);
    
        if (writeFile) {
            MINI_LOGGER.clear();
        }
        else {
            messageToUser.info(new TForms().fromArray(MINI_LOGGER));
        }
    }
    
    private static boolean doDelete(String x) {
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
        Map<String, Long> sshCheckerMap = SSH_CHECKER_MAP;
        
        if (tempFile.isEmpty()) {
            MINI_LOGGER.add("tempFile.isEmpty()");
            writeLog();
            throw new IllegalInvokeEx(getClass().getSimpleName() + " tempFile.isEmpty()");
        }
        else {
            String[] strings = COMPILE1.split(tempFile);
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x->{
                if (COMPILE2.split(x).length > 2) {
                    chkWithList(COMPILE3.split(x));
                }
                try {
                    Long ifAbsent = sshCheckerMap.putIfAbsent(COMPILE4.split(x)[0].trim(), Long.valueOf(COMPILE5.split(x)[1]));
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
        ConstantsNet.setSshMapStr(new TForms().sshCheckerMapWithDates(sshCheckerMap, true));
        messageToUser.info(getClass().getSimpleName() + ".sshChecker", "ConstantsNet.getSshMapStr()", " = " + ConstantsNet.getSshMapStr());
        
    }
    
    private void chkWithList(String[] x) {
        this.delStamp = Long.parseLong(x[1]);
        if (delStamp < ConstantsFor.getAtomicTime()) {
            doDelete(x[0]);
            System.out.println(addBackToList(x[0], x[2]));
        }
    }
    
    private static String addBackToList(String ip, String accList) {
        StringBuilder sshBuilder = new StringBuilder();
        sshBuilder.append(SshActs.SUDO_ECHO).append("\"").append(ip).append(" #").append(new Date()).append("\"")
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
            MINI_LOGGER.add("sshChecker(SSH_CHECKER_MAP.forEach): time is " + true + "\n" + x + " is delete = " + isDelete);
            MINI_LOGGER.add("delStamp = " + delStamp);
            MINI_LOGGER.add("ConstantsFor.getAtomicTime()-delStamp = " + (atomicTimeLong - delStamp));
        }
        else {
            MINI_LOGGER.add("IP" + " = " + x + " time: " + y + " (" + new Date(y) + ")");
        }
    }
    
}
