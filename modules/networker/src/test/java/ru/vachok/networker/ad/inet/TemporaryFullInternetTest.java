// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.inet;


import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
import ru.vachok.networker.AbstractForms;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.SSHFactory;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.configuretests.TestConfigure;
import ru.vachok.networker.configuretests.TestConfigureThreadsLogMaker;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.ConstantsNet;
import ru.vachok.networker.data.enums.PropertiesNames;
import ru.vachok.networker.sysinfo.AppConfigurationLocal;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;


/**
 @see TemporaryFullInternet */
public class TemporaryFullInternetTest {


    private final TestConfigure testConfigureThreadsLogMaker = new TestConfigureThreadsLogMaker(getClass().getSimpleName(), System.nanoTime());

    @BeforeClass
    public void setUp() {
        Thread.currentThread().setName(getClass().getSimpleName().substring(0, 6));
        testConfigureThreadsLogMaker.before();
    }

    @AfterClass
    public void tearDown() {
        testConfigureThreadsLogMaker.after();
    }

    @Test
    public void testRunCheck() {
        try {
            Runnable internet = new TemporaryFullInternet();
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(internet);
            submit.get(30, TimeUnit.SECONDS);
        }
        catch (ExecutionException | InterruptedException | TimeoutException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + AbstractForms.fromArray(e));
        }
    }

    @Test
    public void testRunAdd() {
        TemporaryFullInternet fullInternet = new TemporaryFullInternet("8.8.8.8", System.currentTimeMillis(), "add", new MockHttpServletRequest().getRemoteAddr());
        String getStr = AppConfigurationLocal.getInstance().submitAsString(fullInternet::toString, 15);
        Assert.assertTrue(getStr.contains("8.8.8.8"), getStr);
    }

    /**
     @see TemporaryFullInternet#sshChecker()
     */
    @Test
    @Ignore
    public void sshChecker$$Copy() {
        final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();
        final Queue<String> MINI_LOGGER = new ArrayDeque<>();
        final Map<String, Long> SSH_CHECKER_MAP = new ConcurrentHashMap<>();
        final Pattern PAT_LIST = Pattern.compile(".list", Pattern.LITERAL);
        final Pattern PAT_BR_N = Pattern.compile("<br>\n");
        final Pattern PAT_SHARP = Pattern.compile(" #");

        SSH_FACTORY.setCommandSSH(ConstantsFor.SSH_CAT24HRSLIST);
        String tempFile = SSH_FACTORY.call();
        MINI_LOGGER.add(tempFile);

        if (tempFile.isEmpty()) {
            throw new InvokeIllegalException("File is empty");
        }
        else {
            String[] strings = PAT_BR_N.split(tempFile);
            List<String> stringList = Arrays.asList(strings);
            stringList.forEach(x->{
                if (PAT_SHARP.split(x).length > 2) {
                    chkWithList(PAT_SHARP.split(x), MINI_LOGGER, SSH_CHECKER_MAP);
                }
                try {
                    Long ifAbsent = SSH_CHECKER_MAP.putIfAbsent(PAT_SHARP.split(x)[0].trim(), Long.valueOf(PAT_SHARP.split(x)[1]));
                    MINI_LOGGER.add("Added to map = " + x + " " + ifAbsent);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    Assert.assertNull(e, e.getMessage());
                    MINI_LOGGER.add(e.getMessage());
                }
            });
        }
        long atomicTimeLong = UsefulUtilities.getAtomicTime();
        for (Map.Entry<String, Long> entry : SSH_CHECKER_MAP.entrySet()) {
            String x = entry.getKey();
            Long y = entry.getValue();
            mapEntryParse(x, y, atomicTimeLong, MINI_LOGGER, SSH_CHECKER_MAP);
        }
        ConstantsNet.setSshMapStr(new TForms().sshCheckerMapWithDates(SSH_CHECKER_MAP, true));
        String mapStr = ConstantsNet.getSshMapStr();
        Assert.assertTrue(mapStr.contains("8.8.8.8"), mapStr);
    }

    private void chkWithList(String[] x, Queue<String> MINI_LOGGER, Map<String, Long> SSH_CHECKER_MAP) {
        long delStamp = Long.parseLong(x[1]);
        if (delStamp < UsefulUtilities.getAtomicTime()) {
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

    @Test
    public void testRunFromMonitors() {
        new TemporaryFullInternet(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)).run();
        File miniLog = new File(ConstantsFor.ROOT_PATH_WITH_SEPARATOR + "ssh" + System.getProperty(PropertiesNames.SYS_SEPARATOR) + "TemporaryFullInternet.mini");
        Assert.assertTrue(miniLog.exists());
    }

    private static boolean doDelete(String x, Map<String, Long> SSH_CHECKER_MAP, Queue<String> MINI_LOGGER) {
        final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();

        String sshC = new StringBuilder()
                .append(ConstantsFor.SSH_SUDO_GREP_V).append(x)
                .append("' /etc/pf/24hrs > /etc/pf/24hrs_tmp;").append("sudo cp /etc/pf/24hrs_tmp /etc/pf/24hrs;")
                .append(ConstantsFor.SSH_INITPF).toString();
        SSH_FACTORY.setCommandSSH(sshC);
        String sshCommand = SSH_FACTORY.call();
        Long aLong = SSH_CHECKER_MAP.remove(x);
        if (!(aLong == null)) {
            MINI_LOGGER.add(new Date(aLong) + ", doDelete: " + sshCommand);
        }
        return SSH_CHECKER_MAP.containsKey(x);
    }

    private static String addBackToList(String ip, String accList) {
        final SSHFactory SSH_FACTORY = new SSHFactory.Builder("192.168.13.42", "ls", TemporaryFullInternet.class.getSimpleName()).build();

        StringBuilder sshBuilder = new StringBuilder();
        sshBuilder.append(ConstantsFor.SSH_SUDO_ECHO).append("\"").append(ip).append(" #").append(new Date()).append("\"")
                .append(" >> /etc/pf/").append(accList).append(";").append(ConstantsFor.SSH_INITPF);
        SSH_FACTORY.setCommandSSH(sshBuilder.toString());
        return SSH_FACTORY.call();
    }


}