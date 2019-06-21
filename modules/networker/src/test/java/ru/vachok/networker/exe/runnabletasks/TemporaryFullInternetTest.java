package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.accesscontrol.sshactions.SshActs;
import ru.vachok.networker.net.enums.ConstantsNet;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


@SuppressWarnings("ALL") public class TemporaryFullInternetTest {
    
    
    @Test
    public void testRunCheck() {
        new TemporaryFullInternet().run();
    }
    
    @Test
    public void testRunAdd() {
        new TemporaryFullInternet("8.8.8.8", System.currentTimeMillis(), "add").run();
    }
    
    /**
     @see TemporaryFullInternet#sshChecker()
     */
    @Test
    public void sshCheckerCopy() {
        final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();
        final Queue<String> MINI_LOGGER = new ArrayDeque<>();
        final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();
        final Pattern COMPILE = Pattern.compile(".list", Pattern.LITERAL);
        final Pattern PATTERN = Pattern.compile(".list", Pattern.LITERAL);
        final Pattern COMPILE1 = Pattern.compile("<br>\n");
        final Pattern COMPILE2 = Pattern.compile(" #");
        
        SSH_FACTORY.setCommandSSH(ConstantsNet.COM_CAT24HRSLIST);
        String tempFile = SSH_FACTORY.call();
        MINI_LOGGER.add(tempFile);
        Map<String, Long> sshCheckerMap = SSH_CHECKER_MAP;
        
        if (tempFile.isEmpty()) {
            throw new IllegalComponentStateException("File is empty");
        }
        else {
            String[] strings = COMPILE1.split(tempFile);
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x->{
                if (COMPILE2.split(x).length > 2) {
                    chkWithList(COMPILE2.split(x), MINI_LOGGER, SSH_CHECKER_MAP);
                }
                try {
                    Long ifAbsent = sshCheckerMap.putIfAbsent(COMPILE2.split(x)[0].trim(), Long.valueOf(COMPILE2.split(x)[1]));
                    MINI_LOGGER.add("Added to map = " + x + " " + ifAbsent);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    Assert.assertNull(e, e.getMessage());
                    MINI_LOGGER.add(e.getMessage());
                }
            });
        }
        long atomicTimeLong = ConstantsFor.getAtomicTime();
        for (Map.Entry<String, Long> entry : sshCheckerMap.entrySet()) {
            String x = entry.getKey();
            Long y = entry.getValue();
            mapEntryParse(x, y, atomicTimeLong, MINI_LOGGER, SSH_CHECKER_MAP);
        }
        ConstantsNet.setSshMapStr(new TForms().sshCheckerMapWithDates(sshCheckerMap, true));
        String mapStr = ConstantsNet.getSshMapStr();
        Assert.assertTrue(mapStr.contains("8.8.8.8"), mapStr);
    }
    
    private void chkWithList(String[] x, Queue<String> MINI_LOGGER, Map<String, Long> SSH_CHECKER_MAP) {
        long delStamp = Long.parseLong(x[1]);
        if (delStamp < ConstantsFor.getAtomicTime()) {
            doDelete(x[0], SSH_CHECKER_MAP, MINI_LOGGER);
            System.out.println(addBackToList(x[0], x[2]));
        }
    }
    
    private void mapEntryParse(String x, Long y, long atomicTimeLong, Queue<String> MINI_LOGGER, Map<String, Long> SSH_CHECKER_MAP) {
        String willBeDel = x + " will be deleted at " + LocalDateTime.ofEpochSecond(y / 1000, 0, ZoneOffset.ofHours(3));
        MINI_LOGGER.add(willBeDel);
        if (y < atomicTimeLong) {
            boolean isDelete = doDelete(x, SSH_CHECKER_MAP, MINI_LOGGER);
            MINI_LOGGER.add("sshChecker(SSH_CHECKER_MAP.forEach): time is " + true + "\n" + x + " is delete = " + isDelete);
            MINI_LOGGER.add("delStamp = " + y);
            MINI_LOGGER.add("ConstantsFor.getAtomicTime()-delStamp = " + (atomicTimeLong - y));
        }
        else {
            MINI_LOGGER.add("IP" + " = " + x + " time: " + y + " (" + new Date(y) + ")");
        }
    }
    
    private static String addBackToList(String ip, String accList) {
        final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();
        
        StringBuilder sshBuilder = new StringBuilder();
        sshBuilder.append(SshActs.SUDO_ECHO).append("\"").append(ip).append(" #").append(new Date()).append("\"")
            .append(" >> /etc/pf/").append(accList).append(";").append(ConstantsNet.COM_INITPF);
        SSH_FACTORY.setCommandSSH(sshBuilder.toString());
        return SSH_FACTORY.call();
    }
    
    private static boolean doDelete(String x, Map<String, Long> SSH_CHECKER_MAP, Queue<String> MINI_LOGGER) {
        final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();
        
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
    
    
}