// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.TForms;
import ru.vachok.networker.restapi.message.DBMessenger;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 @see ru.vachok.networker.net.scanner.CheckerIpTest
 @since 12.07.2019 (14:36) */
class CheckerIp {
    
    
    private NetLists netLists = NetLists.getI();
    
    private MessageToUser messageToUser = new DBMessenger(this.getClass().getSimpleName());
    
    private PrintStream printStream;
    
    private Map<String, String> netListKeeperOffLines = netLists.editOffLines();
    
    private ConcurrentMap<String, String> onLinesResolve = netLists.getOnLinesResolve();
    
    private String hostAddress;
    
    CheckerIp(String hostAddress, PrintStream printStream) {
        this.printStream = printStream;
        this.hostAddress = hostAddress;
    }
    
    public boolean checkIP() {
        
        boolean xReachable = false;
        byte[] addressBytes;
        try {
            addressBytes = InetAddress.getByName(hostAddress.split(" ")[0]).getAddress();
        }
        catch (UnknownHostException | NullPointerException e) {
            addressBytes = InetAddress.getLoopbackAddress().getAddress();
        }
    
        InetAddress inetAddress = makeInetAddress(addressBytes);
    
        if (!inetAddress.equals(InetAddress.getLoopbackAddress())) {
            xReachable = true;
        }
        
        if (!xReachable) {
            xNotReachable();
        }
        else {
            xIsReachable();
        }
        netLists.setOffLines(netListKeeperOffLines);
        return xReachable;
    }
    
    public boolean isReach(InetAddress inetAddress) {
        this.hostAddress = inetAddress.getHostAddress();
        return checkIP();
    }
    
    private InetAddress makeInetAddress(byte[] addressBytes) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByAddress(addressBytes);
        }
        catch (UnknownHostException e) {
            inetAddress = InetAddress.getLoopbackAddress();
        }
        return inetAddress;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckerIp{");
        sb.append("Offline pc is <font color=\"red\"><b>").append(netLists.editOffLines().size()).append(":</b></font><br>");
        sb.append("Online  pc is<font color=\"#00ff69\"> <b>").append(onLinesResolve.size()).append(":</b><br>");
        sb.append(new TForms().fromArray(onLinesResolve, true)).append("</font><br>");
        sb.append('}');
        return sb.toString();
    }
    
    private void xIsReachable() {
        printStream.println(hostAddress + " <font color=\"green\">online</font>.");
    
        String ifAbsent = onLinesResolve.putIfAbsent(hostAddress, LocalTime.now().toString());
        String removeOffline = netListKeeperOffLines.remove(hostAddress);
        if (!(removeOffline == null)) {
            messageToUser.info(hostAddress, ScanOnline.STR_ONLINE, MessageFormat.format("{0} gets online!", removeOffline));
        }
    }
    
    private void xNotReachable() {
        printStream.println(new StringBuilder().append(hostAddress).append(" <font color=\"red\">offline</font>."));
        String removeOnline = onLinesResolve.remove(hostAddress);
        
        if (!(removeOnline == null)) {
            netListKeeperOffLines.putIfAbsent(hostAddress, new Date().toString());
            messageToUser.info(MessageFormat.format("Map<String, String> offLines size = {0} items", netListKeeperOffLines.size()));
        }
        else {
            messageToUser
                .info(MessageFormat.format("String removeOnline is NULL! Size onLinesResolve Map is {0}. Tried del: {1}", onLinesResolve.size(), hostAddress));
        }
    }
}
