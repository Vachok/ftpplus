// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.NotNull;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.data.NetKeeper;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;


/**
 @see ru.vachok.networker.net.scanner.CheckerIpHTMLTest
 @since 12.07.2019 (14:36) */
class CheckerIpHTML {
    
    
    private MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, this.getClass().getSimpleName());
    
    private PrintStream printStream;
    
    private Map<String, String> netListKeeperOffLines = NetKeeper.editOffLines();
    
    private ConcurrentMap<String, String> onLinesResolve = NetKeeper.getOnLinesResolve();
    
    private String hostAddress;
    
    CheckerIpHTML(String hostAddress, PrintStream printStream) {
        this.printStream = printStream;
        this.hostAddress = hostAddress;
    }
    
    public boolean isReach(@NotNull InetAddress inetAddress) {
        this.hostAddress = inetAddress.getHostAddress();
        return checkIP();
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
        NetKeeper.setOffLines(netListKeeperOffLines);
        return xReachable;
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
    
    private void xIsReachable() {
        printStream.println(hostAddress + " <font color=\"green\">online</font>.");
        
        String ifAbsent = onLinesResolve.putIfAbsent(hostAddress, LocalTime.now().toString());
        String removeOffline = netListKeeperOffLines.remove(hostAddress);
        if (!(removeOffline == null)) {
            messageToUser.info(hostAddress, ScanOnline.STR_ONLINE, MessageFormat.format("{0} gets online!", removeOffline));
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CheckerIpHTML{");
        sb.append("printStream=").append(printStream);
        sb.append(", onLinesResolve=").append(onLinesResolve);
        sb.append(", netListKeeperOffLines=").append(netListKeeperOffLines);
        sb.append(", messageToUser=").append(messageToUser);
        sb.append(", hostAddress='").append(hostAddress).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
