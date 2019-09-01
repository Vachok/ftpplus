// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.ssh;


import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.inet.InternetUse;
import ru.vachok.networker.componentsrepo.NameOrIPChecker;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsFor;
import ru.vachok.networker.componentsrepo.data.enums.ConstantsNet;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.restapi.MessageToUser;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 @see ru.vachok.networker.ssh.TemporaryFullInternetTest
 @since 28.02.2019 (11:52) */
@Service
public class TemporaryFullInternet implements Runnable, Callable<String> {
    
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.DB, TemporaryFullInternet.class.getSimpleName());
    
    private static final Queue<String> MINI_LOGGER = new ArrayDeque<>();
    
    private static final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();
    
    private static final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();
    
    private static final Pattern PAT_FILEEXT_LIST = Pattern.compile(".list", Pattern.LITERAL);
    
    private static final Pattern PAT_BR_N = Pattern.compile("<br>\n");
    
    private static final Pattern PAT_SHARP = Pattern.compile(" #");
    
    @SuppressWarnings("CanBeFinal")
    private String userInputIpOrHostName;
    
    private long delStamp;
    
    private String optionToDo;
    
    private long initStamp = System.currentTimeMillis();
    
    public TemporaryFullInternet() {
        this.userInputIpOrHostName = "10.200.213.254";
        this.delStamp = System.currentTimeMillis();
        this.optionToDo = "check";
        
        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + optionToDo
            .toUpperCase() + " " + userInputIpOrHostName + " full internet access before: " + new Date(delStamp));
    }
    
    public TemporaryFullInternet(String input, long hoursToOpenInet, @NotNull String option) {
        this.userInputIpOrHostName = input;
        this.delStamp = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hoursToOpenInet);
        this.optionToDo = option;
        
        MINI_LOGGER.add(getClass().getSimpleName() + "() starting... " + option.toUpperCase() + " " + input + " full internet access before: " + new Date(delStamp));
    }
    
    public TemporaryFullInternet(long timeStampOff) {
        this.userInputIpOrHostName = "10.200.213.85";
        this.delStamp = timeStampOff;
    }
    
    @Override
    public String call() {
        return doAdd();
    }
    
    @SuppressWarnings("FeatureEnvy")
    private @NotNull String doAdd() {
        SSH_FACTORY.setConnectToSrv(new AppComponents().sshActs().whatSrvNeed());
        NameOrIPChecker nameOrIPChecker = new NameOrIPChecker(userInputIpOrHostName);
        StringBuilder retBuilder = new StringBuilder();
        String sshIP = String.valueOf(nameOrIPChecker.resolveInetAddress()).split("/")[1];
        String tempString24HRSFile = sshCall();
        Map<String, String> inetUniqMap = InternetUse.get24hrsTempInetList();
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
                String listWhere = inetUniqMap.get(PAT_FILEEXT_LIST.matcher(sshIP).replaceAll(Matcher.quoteReplacement("")));
                
                retBuilder.append("<h2>").append(sshIP).append(" in regular list: ").append(listWhere).append("</h2>");
                retBuilder.append(addFromExistList(sshIP, listWhere));
            }
            else {
                String sshCommand = new StringBuilder()
                    .append(SshActs.SUDO_ECHO)
                    .append("\"").append(sshIP).append(" #")
                    .append(delStamp).append("\"").append(ConstantsFor.SSHCOM_24HRS).append(ConstantsNet.COM_INITPF).toString();
                SSH_FACTORY.setCommandSSH(sshCommand);
                retBuilder.append(SSH_FACTORY.call());
            }
        }
        MINI_LOGGER.add("doAdd(): " + retBuilder);
        return retBuilder.toString();
    }
    
    private @NotNull String sshCall() {
        StringBuilder tempString24HRSBuilder = new StringBuilder();
        try {
            SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
            tempString24HRSBuilder.append(SSH_FACTORY.call());
        }
        catch (ArrayIndexOutOfBoundsException | UnknownFormatConversionException e) {
            tempString24HRSBuilder.append(new TForms().fromArray(e, true));
        }
        return tempString24HRSBuilder.toString();
    }
    
    @SuppressWarnings("FeatureEnvy")
    private String addFromExistList(String sshIP, String listWhere) {
        
        listWhere = PAT_FILEEXT_LIST.matcher(listWhere).replaceAll(Matcher.quoteReplacement(""));
        
        StringBuilder comSSHBuilder = new StringBuilder();
        comSSHBuilder.append(SshActs.SSH_SUDO_GREP_V);
        comSSHBuilder.append(sshIP).append("'");
        comSSHBuilder.append(SshActs.SSH_ETCPF).append(listWhere).append(" >").append(SshActs.SSH_ETCPF).append(listWhere).append("_tmp;");
        
        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        String copyPermanentToTmp = SSH_FACTORY.call();
        messageToUser.info(copyPermanentToTmp);
        
        comSSHBuilder = getSSHCommandBuider(listWhere);
        
        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        String copyTmpToPermanent = SSH_FACTORY.call();
        messageToUser.info(copyPermanentToTmp);
        
        comSSHBuilder = new StringBuilder();
        comSSHBuilder.append(SshActs.SUDO_ECHO).append("\"");
        comSSHBuilder.append(sshIP).append(" #").append(delStamp).append(" #");
        comSSHBuilder.append(listWhere).append("\"").append(ConstantsFor.SSHCOM_24HRS).append(ConstantsNet.COM_INITPF);
        
        SSH_FACTORY.setCommandSSH(comSSHBuilder.toString());
        String initNewConfig = SSH_FACTORY.call();
        messageToUser.info(initNewConfig);
        
        return initNewConfig;
    }
    
    private @NotNull StringBuilder getSSHCommandBuider(String listWhere) {
        StringBuilder comSSHBuilder = new StringBuilder();
        comSSHBuilder.append("sudo cp /etc/pf/");
        comSSHBuilder.append(listWhere);
        comSSHBuilder.append("_tmp /etc/pf/").append(listWhere).append(";");
        return comSSHBuilder;
    }
    
    @Override
    public void run() {
        SSH_FACTORY.setConnectToSrv(new AppComponents().sshActs().whatSrvNeed());
        if (optionToDo != null && optionToDo.equals("add")) {
            System.out.println("doAdd() = " + doAdd());
        }
        execOldMeth();
    }
    
    private void execOldMeth() {
        boolean isExecByThreadConfig = AppComponents.threadConfig().execByThreadConfig(this::sshChecker);
        
        Date nextStart = new Date(UsefulUtilities.getAtomicTime() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY));
        String fromArray = new TForms().fromArray(SSH_CHECKER_MAP, false);
        
        MINI_LOGGER.add(MessageFormat.format("{2} is exec Old Meth: {0} {1}", userInputIpOrHostName, fromArray, isExecByThreadConfig));
        MINI_LOGGER.add(nextStart.toString());
        writeLog();
    }
    
    private void sshChecker() {
        SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
        String fromSSH24HrsList = SSH_FACTORY.call();
        MINI_LOGGER.add(fromSSH24HrsList);
        
        if (fromSSH24HrsList.isEmpty()) {
            MINI_LOGGER.add("fromSSH24HrsList.isEmpty()");
            writeLog();
            throw new InvokeIllegalException(getClass().getSimpleName() + " fromSSH24HrsList.isEmpty()");
        }
        else {
            String[] strings = PAT_BR_N.split(fromSSH24HrsList);
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(this::parseString);
        }
        long atomicTimeLong = UsefulUtilities.getAtomicTime();
        for (Map.Entry<String, Long> entry : SSH_CHECKER_MAP.entrySet()) {
            String x = entry.getKey();
            Long y = entry.getValue();
            mapEntryParse(x, y, atomicTimeLong);
        }
        ConstantsNet.setSshMapStr(new TForms().sshCheckerMapWithDates(SSH_CHECKER_MAP, true));
        messageToUser.info(getClass().getSimpleName() + ".sshChecker", "ConstantsNet.getSshMapStr()", " = " + ConstantsNet.getSshMapStr()
            .replaceAll(ConstantsFor.STR_BR, ConstantsFor.STR_N));
        
    }
    
    private void writeLog() {
        File miniLog = new File(getClass().getSimpleName() + ".mini");
        boolean writeFile = FileSystemWorker.writeFile(miniLog.getName(), MINI_LOGGER.stream());
        FileSystemWorker.copyOrDelFile(miniLog, Paths
            .get(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + ConstantsFor.FILESUF_SSHACTIONS + ConstantsFor.FILESYSTEM_SEPARATOR + miniLog.getName())
            .toAbsolutePath().normalize(), true);
        
        if (writeFile) {
            MINI_LOGGER.clear();
        }
        else {
            messageToUser.info(new TForms().fromArray(MINI_LOGGER));
        }
    }
    
    private void parseString(String x) {
        if (PAT_SHARP.split(x).length > 2) {
            chkWithList(PAT_SHARP.split(x));
        }
        try {
            Long ifAbsent = SSH_CHECKER_MAP.putIfAbsent(PAT_SHARP.split(x)[0].trim(), Long.valueOf(PAT_SHARP.split(x)[1]));
            MINI_LOGGER.add("Added to map = " + x + " " + ifAbsent);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            messageToUser.errorAlert("TemporaryFullInternet", "sshChecker", e.getMessage());
            MINI_LOGGER.add(e.getMessage());
        }
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
    
    private void chkWithList(@NotNull String[] x) {
        this.delStamp = Long.parseLong(x[1]);
        if (delStamp < UsefulUtilities.getAtomicTime()) {
            doDelete(x[0]);
            System.out.println(addBackToList(x[0], x[2]));
        }
    }
    
    private static boolean doDelete(String delDomainName) {
        String sshC = new StringBuilder()
            .append(SshActs.SSH_SUDO_GREP_V).append(delDomainName)
            .append("' /etc/pf/24hrs > /etc/pf/24hrs_tmp;").append("sudo cp /etc/pf/24hrs_tmp /etc/pf/24hrs;")
            .append(ConstantsNet.COM_INITPF).toString();
        SSH_FACTORY.setCommandSSH(sshC);
        String sshCommand = SSH_FACTORY.call();
        Long aLong = SSH_CHECKER_MAP.remove(delDomainName);
        if (!(aLong == null)) {
            MINI_LOGGER.add(new Date(aLong) + ", doDelete: " + sshCommand);
        }
        return SSH_CHECKER_MAP.containsKey(delDomainName);
    }
    
    private static String addBackToList(String ip, String accList) {
        StringBuilder sshBuilder = new StringBuilder();
        sshBuilder.append(SshActs.SUDO_ECHO).append("\"").append(ip).append(" #").append(new Date()).append("\"")
            .append(" >> /etc/pf/").append(accList).append(";").append(ConstantsNet.COM_INITPF);
        SSH_FACTORY.setCommandSSH(sshBuilder.toString());
        return SSH_FACTORY.call();
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
    
}
