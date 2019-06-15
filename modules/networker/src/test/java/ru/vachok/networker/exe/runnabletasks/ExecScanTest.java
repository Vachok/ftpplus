// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.net.NetListKeeper;
import ru.vachok.networker.net.NetScanFileWorker;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;


/**
 @since 09.06.2019 (23:54) */
@SuppressWarnings("ALL") public class ExecScanTest {
    
    
    @Test
    public void testRun() {
        throw new IllegalComponentStateException("15.06.2019 (17:36)");
    }
    
    @Test
    public void oneIPScanTest() {
        File vlanFile = new File("lan_11v" + "srv.txt");
        try {
            OutputStream outputStream = new FileOutputStream(vlanFile);
            PrintStream printStream = new PrintStream(outputStream, true);
            for (int i = 0; i < 3; i++) {
                System.out.println(oneIpScanAndPrintToFile(200, i, printStream));
            }
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage());
        }
    }
    
    private String oneIpScanAndPrintToFile(int iThree, int jFour, PrintStream printStream) throws IOException {
        final String FILENAME_SERVTXT = "srv.txt";
        final ThreadConfig threadConfig = AppComponents.threadConfig();
        final String FONT_BR_CLOSE = "</font><br>";
        final String PAT_IS_ONLINE = " is online";
        final File vlanFile = new File("lan_11v" + FILENAME_SERVTXT);
        String whatVlan = "10.200.";
        
        threadConfig.thrNameSet(String.valueOf(iThree));
        
        int timeOutMSec = (int) ConstantsFor.DELAY;
        byte[] aBytes = InetAddress.getByName(whatVlan + iThree + "." + jFour).getAddress();
        StringBuilder stringBuilder = new StringBuilder();
        InetAddress byAddress = InetAddress.getByAddress(aBytes);
        String hostName = byAddress.getHostName();
        String hostAddress = byAddress.getHostAddress();
        
        if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
            timeOutMSec = (int) (ConstantsFor.DELAY * 2);
            NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
        }
        
        boolean isReachable = byAddress.isReachable(timeOutMSec);
        if (isReachable) {
            NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
            
            getAllDevLocalDeq().add("<font color=\"green\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName).append(PAT_IS_ONLINE);
        }
        else {
            NetListKeeper.getI().getOffLines().put(byAddress.getHostAddress(), hostName);
            
            getAllDevLocalDeq().add("<font color=\"red\">" + hostName + FONT_BR_CLOSE);
            stringBuilder.append(hostAddress).append(" ").append(hostName);
        }
        if (stringBuilder.toString().contains(PAT_IS_ONLINE)) {
            {
                printStream.println(hostAddress + " " + hostName);
                System.out.println((getClass().getSimpleName() + ".oneIpScanAndPrintToFile ip online " + whatVlan + iThree + "." + jFour + vlanFile.getName() + " = " + vlanFile
                    .length() + ConstantsFor.STR_BYTES));
                
            }
        }
        return stringBuilder.toString();
    }
    
    private Collection<String> getAllDevLocalDeq() {
        final int MAX_IN_ONE_VLAN = 255;
        final int IPS_IN_VELKOM_VLAN = Integer.parseInt(AppComponents.getProps().getProperty(ConstantsFor.PR_VLANNUM, "59")) * MAX_IN_ONE_VLAN;
        final BlockingDeque<String> ALL_DEVICES = new LinkedBlockingDeque<>(IPS_IN_VELKOM_VLAN);
        
        int vlanNum = IPS_IN_VELKOM_VLAN / MAX_IN_ONE_VLAN;
        AppComponents.getProps().setProperty(ConstantsFor.PR_VLANNUM, String.valueOf(vlanNum));
        return ALL_DEVICES;
    }
}